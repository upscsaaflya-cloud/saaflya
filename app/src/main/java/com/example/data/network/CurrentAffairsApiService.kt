package com.example.data.network

import android.util.Log
import com.example.data.model.CurrentAffairsCategory
import com.example.data.model.CurrentAffairsItem
import com.example.data.model.ExamCategory
import com.example.data.model.ExamSubject
import com.example.data.model.McqQuestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * Retrofit interface for Wikimedia Daily Featured Current Events & On-This-Day Public API.
 * Base URL: https://api.wikimedia.org/
 */
interface WikimediaFeedPublicApi {
    @GET("feed/v1/wikipedia/en/featured/{year}/{month}/{day}")
    suspend fun getDailyFeaturedFeed(
        @Path("year") year: String,
        @Path("month") month: String,
        @Path("day") day: String
    ): ResponseBody
}

/**
 * Retrofit interface for Spaceflight & Science News Public API v4 (No API key required).
 * Base URL: https://api.spaceflightnewsapi.net/
 */
interface SpaceflightNewsPublicApi {
    @GET("v4/articles/")
    suspend fun getLatestScienceArticles(
        @Query("limit") limit: Int = 6,
        @Query("search") search: String? = null
    ): ResponseBody
}

/**
 * Fetches daily exam-relevant news from public REST APIs (Wikimedia Current Events + Spaceflight Science API)
 * and transforms them into syllabus-aligned CurrentAffairsItem cards with categorized tags, ISO dates,
 * and interactive Prelims practice MCQs.
 */
class CurrentAffairsApiService {

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                    .header(
                        "User-Agent",
                        "GPSCAI-Android-ExamApp/1.0 (https://gpsc.gujarat.gov.in; contact@saaflyagpsc.in)"
                    )
                    .header("Accept", "application/json")
                    .build()
                chain.proceed(req)
            }
            .build()
    }

    private val wikimediaApi: WikimediaFeedPublicApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.wikimedia.org/")
            .client(okHttpClient)
            .build()
            .create(WikimediaFeedPublicApi::class.java)
    }

    private val spaceflightApi: SpaceflightNewsPublicApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.spaceflightnewsapi.net/")
            .client(okHttpClient)
            .build()
            .create(SpaceflightNewsPublicApi::class.java)
    }

    /**
     * Fetches daily exam-relevant news concurrently from Wikimedia Featured Current Events
     * and Spaceflight Science & Tech Public APIs.
     */
    suspend fun fetchDailyExamNews(
        targetIsoDate: String? = null,
        topicQuery: String? = null
    ): List<CurrentAffairsItem> = withContext(Dispatchers.IO) {
        val (year, month, day, resolvedIsoDate) = resolveDateParts(targetIsoDate)

        supervisorScope {
            val wikimediaDeferred = async {
                runCatching {
                    fetchFromWikimediaApi(year, month, day, resolvedIsoDate)
                }.onFailure { e ->
                    Log.w(TAG, "Wikimedia public API fetch warning: ${e.localizedMessage}")
                }.getOrDefault(emptyList())
            }

            val scienceDeferred = async {
                runCatching {
                    fetchFromSpaceflightApi(topicQuery?.takeIf { it.isNotBlank() }, resolvedIsoDate)
                }.onFailure { e ->
                    Log.w(TAG, "Spaceflight public API fetch warning: ${e.localizedMessage}")
                }.getOrDefault(emptyList())
            }

            val combined = wikimediaDeferred.await() + scienceDeferred.await()
            combined.distinctBy { it.titleEn.lowercase(Locale.ROOT) }
        }
    }

    private suspend fun fetchFromWikimediaApi(
        year: String,
        month: String,
        day: String,
        isoDate: String
    ): List<CurrentAffairsItem> {
        val rawBody = wikimediaApi.getDailyFeaturedFeed(year, month, day).string()
        val root = JSONObject(rawBody)
        val items = mutableListOf<CurrentAffairsItem>()

        // 1. Parse live "news" array (Current Events in the News)
        val newsArray = root.optJSONArray("news")
        if (newsArray != null) {
            for (i in 0 until minOf(newsArray.length(), 5)) {
                val newsObj = newsArray.optJSONObject(i) ?: continue
                val rawStory = stripHtmlTags(newsObj.optString("story", "")).trim()
                val linksArr = newsObj.optJSONArray("links")
                val primaryLink = linksArr?.optJSONObject(0) ?: continue

                val topicTitle = primaryLink.optJSONObject("titles")?.optString("normalized")
                    ?: primaryLink.optString("title", "Global Current Affairs Update")
                val extract = primaryLink.optString("extract", rawStory).trim()
                val pageUrl = primaryLink
                    .optJSONObject("content_urls")
                    ?.optJSONObject("desktop")
                    ?.optString("page")

                if (topicTitle.isNotBlank() && (extract.isNotBlank() || rawStory.isNotBlank())) {
                    val headline = if (rawStory.length in 20..140) rawStory else "$topicTitle: Current Affairs Brief"
                    val summary = if (extract.isNotBlank() && extract != rawStory) {
                        "$rawStory\n\nBackground Context: $extract"
                    } else {
                        extract.ifBlank { rawStory }
                    }
                    val classified = classifyNewsTopic("$headline $summary")
                    items.add(
                        buildPublicApiAffairsItem(
                            id = "pub_wiki_news_${isoDate}_$i",
                            headlineEn = headline,
                            focusConceptEn = topicTitle,
                            summaryEn = summary,
                            isoDate = isoDate,
                            category = classified.category,
                            examCategory = classified.examCategory,
                            subject = classified.subject,
                            gsCode = classified.gsCode,
                            sourceName = "Wikimedia Current Events API",
                            sourceUrl = pageUrl
                        )
                    )
                }
            }
        }

        // 2. Parse "tfa" (Today's Featured Article — high-yield static/current linkage)
        val tfaObj = root.optJSONObject("tfa")
        if (tfaObj != null) {
            val tfaTitle = tfaObj.optJSONObject("titles")?.optString("normalized")
                ?: tfaObj.optString("title", "")
            val tfaExtract = tfaObj.optString("extract", "").trim()
            val tfaDesc = tfaObj.optString("description", "Featured Topic").trim()
            val tfaUrl = tfaObj.optJSONObject("content_urls")
                ?.optJSONObject("desktop")
                ?.optString("page")

            if (tfaTitle.isNotBlank() && tfaExtract.isNotBlank()) {
                val classified = classifyNewsTopic("$tfaTitle $tfaDesc $tfaExtract")
                items.add(
                    buildPublicApiAffairsItem(
                        id = "pub_wiki_tfa_$isoDate",
                        headlineEn = "$tfaTitle — $tfaDesc",
                        focusConceptEn = tfaTitle,
                        summaryEn = tfaExtract,
                        isoDate = isoDate,
                        category = classified.category,
                        examCategory = classified.examCategory,
                        subject = classified.subject,
                        gsCode = classified.gsCode,
                        sourceName = "Wikimedia Daily Featured API",
                        sourceUrl = tfaUrl
                    )
                )
            }
        }

        // 3. Parse top "onthisday" historical milestones for GS-I History & Heritage
        val otdArray = root.optJSONArray("onthisday")
        if (otdArray != null) {
            for (i in 0 until minOf(otdArray.length(), 2)) {
                val otdObj = otdArray.optJSONObject(i) ?: continue
                val eventYear = otdObj.optInt("year", 0)
                val eventText = otdObj.optString("text", "").trim()
                val firstPage = otdObj.optJSONArray("pages")?.optJSONObject(0)
                val pageTitle = firstPage?.optJSONObject("titles")?.optString("normalized")
                    ?: firstPage?.optString("title", "Historical Milestone ($eventYear)")
                    ?: "Historical Milestone ($eventYear)"
                val pageExtract = firstPage?.optString("extract", "").orEmpty().trim()
                val pageUrl = firstPage?.optJSONObject("content_urls")
                    ?.optJSONObject("desktop")
                    ?.optString("page")

                if (eventText.isNotBlank()) {
                    val fullSummary = if (pageExtract.isNotBlank()) {
                        "On This Day ($eventYear): $eventText\n\nHistorical Context: $pageExtract"
                    } else {
                        "On This Day ($eventYear): $eventText"
                    }
                    items.add(
                        buildPublicApiAffairsItem(
                            id = "pub_wiki_otd_${isoDate}_$i",
                            headlineEn = "On This Day ($eventYear): $eventText",
                            focusConceptEn = pageTitle,
                            summaryEn = fullSummary,
                            isoDate = isoDate,
                            category = CurrentAffairsCategory.HISTORY_CULTURE,
                            examCategory = ExamCategory.UPSC,
                            subject = ExamSubject.GUJARAT_HISTORY,
                            gsCode = "GS-I History",
                            sourceName = "Wikimedia On-This-Day API",
                            sourceUrl = pageUrl
                        )
                    )
                }
            }
        }

        return items
    }

    private suspend fun fetchFromSpaceflightApi(
        searchQuery: String?,
        fallbackIsoDate: String
    ): List<CurrentAffairsItem> {
        val rawBody = spaceflightApi.getLatestScienceArticles(limit = 5, search = searchQuery).string()
        val root = JSONObject(rawBody)
        val results = root.optJSONArray("results") ?: return emptyList()
        val items = mutableListOf<CurrentAffairsItem>()

        for (i in 0 until results.length()) {
            val obj = results.optJSONObject(i) ?: continue
            val articleId = obj.optInt("id", i)
            val title = obj.optString("title", "").trim()
            val summary = obj.optString("summary", "").trim()
            val newsSite = obj.optString("news_site", "Spaceflight Science Feed").trim()
            val url = obj.optString("url", "").takeIf { it.isNotBlank() }
            val publishedAt = obj.optString("published_at", "")
            val articleDate = if (publishedAt.length >= 10) {
                publishedAt.substring(0, 10)
            } else {
                fallbackIsoDate
            }

            if (title.isNotBlank() && summary.isNotBlank()) {
                val isIndiaOrIsro = title.contains("ISRO", ignoreCase = true) ||
                    summary.contains("ISRO", ignoreCase = true) ||
                    title.contains("India", ignoreCase = true)

                items.add(
                    buildPublicApiAffairsItem(
                        id = "pub_space_$articleId",
                        headlineEn = title,
                        focusConceptEn = "$newsSite Space & Tech Update",
                        summaryEn = summary,
                        isoDate = articleDate,
                        category = CurrentAffairsCategory.SCIENCE_TECH,
                        examCategory = if (isIndiaOrIsro) ExamCategory.GPSC else ExamCategory.UPSC,
                        subject = ExamSubject.SCIENCE,
                        gsCode = "GS-III Science & Tech",
                        sourceName = "Public API • $newsSite",
                        sourceUrl = url
                    )
                )
            }
        }

        return items
    }

    private data class TopicClassification(
        val category: CurrentAffairsCategory,
        val examCategory: ExamCategory,
        val subject: ExamSubject,
        val gsCode: String
    )

    private fun classifyNewsTopic(text: String): TopicClassification {
        val lower = text.lowercase(Locale.ROOT)
        return when {
            lower.contains("gujarat") || lower.contains("ahmedabad") || lower.contains("gandhinagar") ||
                lower.contains("kutch") || lower.contains("saurashtra") || lower.contains("dholera") ||
                lower.contains("sabarmati") || lower.contains("narmada") -> {
                TopicClassification(
                    category = CurrentAffairsCategory.GUJARAT_SPECIAL,
                    examCategory = ExamCategory.GPSC,
                    subject = ExamSubject.GUJARAT_HISTORY,
                    gsCode = "GPSC GS-I/II/III"
                )
            }
            lower.contains("space") || lower.contains("satellite") || lower.contains("orbit") ||
                lower.contains("nasa") || lower.contains("isro") || lower.contains("quantum") ||
                lower.contains("vaccine") || lower.contains("virus") || lower.contains("technology") ||
                lower.contains("artificial intelligence") || lower.contains("telescope") -> {
                TopicClassification(
                    category = CurrentAffairsCategory.SCIENCE_TECH,
                    examCategory = ExamCategory.UPSC,
                    subject = ExamSubject.SCIENCE,
                    gsCode = "GS-III Science & Tech"
                )
            }
            lower.contains("climate") || lower.contains("cyclone") || lower.contains("earthquake") ||
                lower.contains("volcano") || lower.contains("glacier") || lower.contains("biodiversity") ||
                lower.contains("river") || lower.contains("ocean") || lower.contains("environment") ||
                lower.contains("forest") || lower.contains("flood") -> {
                TopicClassification(
                    category = CurrentAffairsCategory.ENVIRONMENT_GEO,
                    examCategory = ExamCategory.UPSC,
                    subject = ExamSubject.GEOGRAPHY,
                    gsCode = "GS-I/III Geography & Ecology"
                )
            }
            lower.contains("economy") || lower.contains("bank") || lower.contains("inflation") ||
                lower.contains("trade") || lower.contains("gdp") || lower.contains("tax") ||
                lower.contains("budget") || lower.contains("market") || lower.contains("energy") ||
                lower.contains("semiconductor") || lower.contains("export") -> {
                TopicClassification(
                    category = CurrentAffairsCategory.ECONOMY_POLICY,
                    examCategory = ExamCategory.UPSC,
                    subject = ExamSubject.ECONOMY,
                    gsCode = "GS-III Economy & Policy"
                )
            }
            lower.contains("heritage") || lower.contains("unesco") || lower.contains("archaeolog") ||
                lower.contains("dynasty") || lower.contains("century") || lower.contains("empire") ||
                lower.contains("ancient") || lower.contains("museum") || lower.contains("festival") -> {
                TopicClassification(
                    category = CurrentAffairsCategory.HISTORY_CULTURE,
                    examCategory = ExamCategory.UPSC,
                    subject = ExamSubject.GUJARAT_HISTORY,
                    gsCode = "GS-I History & Heritage"
                )
            }
            else -> {
                TopicClassification(
                    category = CurrentAffairsCategory.POLITY_GOVERNANCE,
                    examCategory = ExamCategory.UPSC,
                    subject = ExamSubject.POLITY,
                    gsCode = "GS-II Polity & IR"
                )
            }
        }
    }

    private fun buildPublicApiAffairsItem(
        id: String,
        headlineEn: String,
        focusConceptEn: String,
        summaryEn: String,
        isoDate: String,
        category: CurrentAffairsCategory,
        examCategory: ExamCategory,
        subject: ExamSubject,
        gsCode: String,
        sourceName: String,
        sourceUrl: String?
    ): CurrentAffairsItem {
        val gujaratiPrefix = "[લાઇવ પબ્લિક ન્યૂઝ API • ${category.labelGu}] "
        val syllabusEn = "${examCategory.titleEn} ($gsCode) • ${subject.titleEn}: $focusConceptEn"
        val syllabusGu = "${examCategory.titleGu} ($gsCode) • ${subject.titleGu}: $focusConceptEn"

        val correctCategoryIndex = when (category) {
            CurrentAffairsCategory.POLITY_GOVERNANCE -> 0
            CurrentAffairsCategory.ECONOMY_POLICY -> 1
            CurrentAffairsCategory.SCIENCE_TECH -> 2
            CurrentAffairsCategory.ENVIRONMENT_GEO,
            CurrentAffairsCategory.HISTORY_CULTURE,
            CurrentAffairsCategory.GUJARAT_SPECIAL,
            CurrentAffairsCategory.ALL -> 3
        }

        val practiceMcq = McqQuestion(
            id = "mcq_$id",
            examCategory = examCategory.id,
            subject = subject.id,
            questionTextEn = "With reference to recent daily current affairs regarding \"$focusConceptEn\", under which General Studies syllabus domain is this development primarily examined?",
            questionTextGu = "તાજેતરના દૈનિક કરંટ અફેર્સમાં ચર્ચાયેલ \"$focusConceptEn\" ના સંદર્ભમાં, આ મુદ્દો મુખ્યત્વે જનરલ સ્ટડીઝ (GS) ના કયા અભ્યાસક્રમ હેઠળ આવરી લેવામાં આવે છે?",
            optionsEn = listOf(
                "GS-II: Constitutional Governance, Polity & International Relations",
                "GS-III: Economic Development, Fiscal Policy & Industrial Infrastructure",
                "GS-III: Science, Space Technology, Defence & Innovation",
                "GS-I / GS-III: Geography, Ecology, Heritage & Regional Developments"
            ),
            optionsGu = listOf(
                "GS-II: બંધારણીય શાસન, રાજવ્યવસ્થા અને આંતરરાષ્ટ્રીય સંબંધો",
                "GS-III: આર્થિક વિકાસ, રાજકોષીય નીતિ અને માળખાગત સુવિધા",
                "GS-III: વિજ્ઞાન, અવકાશ ટેકનોલોજી, સંરક્ષણ અને નવીનતા",
                "GS-I / GS-III: ભૂગોળ, પર્યાવરણ, સાંસ્કૃતિક વારસો અને પ્રાદેશિક વિકાસ"
            ),
            correctOptionIndex = correctCategoryIndex,
            explanationEn = "Summary & Exam Context: $summaryEn\n\nSyllabus Linkage: $syllabusEn.",
            explanationGu = "પરીક્ષા લક્ષી સારાંશ ($syllabusGu): $summaryEn",
            referenceNote = "$sourceName • $isoDate",
            topicTagEn = category.labelEn,
            topicTagGu = category.labelGu,
            difficulty = "Current Affairs MCQ",
            keyTakeawaysEn = listOf(
                "Topic: $focusConceptEn ($isoDate)",
                "Syllabus Mapping: $syllabusEn"
            ),
            keyTakeawaysGu = listOf(
                "મુખ્ય વિષય: $focusConceptEn ($isoDate)",
                "અભ્યાસક્રમ જોડાણ: $syllabusGu"
            )
        )

        return CurrentAffairsItem(
            id = id,
            examCategory = examCategory.id,
            subject = subject.id,
            tagEn = category.labelEn,
            tagGu = category.labelGu,
            dateLabel = "$isoDate • $gsCode",
            titleEn = headlineEn,
            titleGu = "$gujaratiPrefix$headlineEn",
            summaryEn = summaryEn,
            summaryGu = "$summaryEn\n\n(નોંધ: આ લાઇવ પબ્લિક API સમાચાર માટે સંપૂર્ણ ગુજરાતી AI વિશ્લેષણ અને નવો MCQ મેળવવા નીચેના 'AI ગુજરાતી વિશ્લેષણ' બટન પર ક્લિક કરો.)",
            syllabusLinkageEn = syllabusEn,
            syllabusLinkageGu = syllabusGu,
            practiceMcq = practiceMcq,
            isoDate = isoDate,
            category = category,
            sourceName = sourceName,
            sourceUrl = sourceUrl,
            isFromPublicApi = true
        )
    }

    private fun stripHtmlTags(html: String): String {
        return html
            .replace(Regex("<[^>]*>"), "")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace(Regex("\\s+"), " ")
    }

    private data class DateParts(
        val year: String,
        val month: String,
        val day: String,
        val isoDate: String
    )

    private fun resolveDateParts(targetIsoDate: String?): DateParts {
        if (!targetIsoDate.isNullOrBlank() && Regex("\\d{4}-\\d{2}-\\d{2}").matches(targetIsoDate.trim())) {
            val parts = targetIsoDate.trim().split("-")
            return DateParts(parts[0], parts[1], parts[2], targetIsoDate.trim())
        }
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val year = String.format(Locale.US, "%04d", cal.get(Calendar.YEAR))
        val month = String.format(Locale.US, "%02d", cal.get(Calendar.MONTH) + 1)
        val day = String.format(Locale.US, "%02d", cal.get(Calendar.DAY_OF_MONTH))
        return DateParts(year, month, day, "$year-$month-$day")
    }

    companion object {
        private const val TAG = "CurrentAffairsApi"

        fun todayIsoDate(daysOffset: Int = 0): String {
            val cal = Calendar.getInstance()
            if (daysOffset != 0) {
                cal.add(Calendar.DAY_OF_YEAR, daysOffset)
            }
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            return fmt.format(cal.time)
        }
    }
}
