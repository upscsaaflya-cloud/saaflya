package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CurriculumSeedData
import com.example.data.model.AppLanguage
import com.example.data.model.Bookmark
import com.example.data.model.CurrentAffairsCategory
import com.example.data.model.CurrentAffairsItem
import com.example.data.model.DateFilterPreset
import com.example.data.model.ExamCategory
import com.example.data.model.ExamSubject
import com.example.data.model.ForumReply
import com.example.data.model.ForumTopic
import com.example.data.model.LearningDashboardUiState
import com.example.data.model.MainsEvaluation
import com.example.data.model.McqQuestion
import com.example.data.model.McqQuizSessionUiState
import com.example.data.model.QuizViewMode
import com.example.data.model.SubjectProgressMetric
import com.example.data.model.SyllabusProgress
import com.example.data.network.CurrentAffairsApiService
import com.example.data.repository.GpscRepository
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.util.UUID

sealed interface CloudDataState<out T> {
    data object Loading : CloudDataState<Nothing>
    data class Success<T>(val data: T) : CloudDataState<T>
    data class Error(val message: String) : CloudDataState<Nothing>
}

sealed interface AiTaskState<out T> {
    data object Idle : AiTaskState<Nothing>
    data object Loading : AiTaskState<Nothing>
    data class Success<T>(val result: T) : AiTaskState<T>
    data class Error(val message: String) : AiTaskState<Nothing>
}

@OptIn(ExperimentalCoroutinesApi::class)
class GpscViewModel(
    private val repository: GpscRepository,
    private val currentUserId: String
) : ViewModel() {

    // Global Preferences (Language & Exam Category)
    private val _language = MutableStateFlow(AppLanguage.GUJARATI)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _selectedCategory = MutableStateFlow(ExamCategory.GPSC)
    val selectedCategory: StateFlow<ExamCategory> = _selectedCategory.asStateFlow()

    private val _selectedSubject = MutableStateFlow(ExamSubject.POLITY)
    val selectedSubject: StateFlow<ExamSubject> = _selectedSubject.asStateFlow()

    // Interactive Prelims MCQ Quiz State
    private val _aiGeneratedMcqs = MutableStateFlow<List<McqQuestion>>(emptyList())
    val aiGeneratedMcqs: StateFlow<List<McqQuestion>> = _aiGeneratedMcqs.asStateFlow()

    // Map of questionId -> selected option index
    private val _selectedAnswers = MutableStateFlow<Map<String, Int>>(emptyMap())
    val selectedAnswers: StateFlow<Map<String, Int>> = _selectedAnswers.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _quizViewMode = MutableStateFlow(QuizViewMode.ALL_QUESTIONS)
    val quizViewMode: StateFlow<QuizViewMode> = _quizViewMode.asStateFlow()

    val activeQuizSessionState: StateFlow<McqQuizSessionUiState> = combine(
        _selectedCategory,
        _selectedSubject,
        _aiGeneratedMcqs,
        _selectedAnswers,
        combine(_currentQuestionIndex, _quizViewMode) { idx, mode -> idx to mode }
    ) { category, subject, aiMcqs, answers, (qIdx, mode) ->
        val aiMatches = aiMcqs.filter {
            it.examCategory == category.id && it.subject == subject.id
        }
        val seedMatches = CurriculumSeedData.curatedMcqs.filter {
            it.examCategory == category.id && it.subject == subject.id
        }
        val crossCategorySupplements = CurriculumSeedData.curatedMcqs.filter {
            it.examCategory != category.id && it.subject == subject.id
        }
        val combinedQuestions = aiMatches + seedMatches + crossCategorySupplements
        val safeIdx = if (combinedQuestions.isEmpty()) 0 else qIdx.coerceIn(0, combinedQuestions.lastIndex)

        McqQuizSessionUiState(
            category = category,
            subject = subject,
            questions = combinedQuestions,
            selectedAnswers = answers,
            currentQuestionIndex = safeIdx,
            viewMode = mode
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = McqQuizSessionUiState(
            category = ExamCategory.GPSC,
            subject = ExamSubject.POLITY,
            questions = CurriculumSeedData.curatedMcqs.filter { it.subject == ExamSubject.POLITY.id }
        )
    )

    private val _aiQuizGenerationState = MutableStateFlow<AiTaskState<List<McqQuestion>>>(AiTaskState.Idle)
    val aiQuizGenerationState: StateFlow<AiTaskState<List<McqQuestion>>> = _aiQuizGenerationState.asStateFlow()

    // Mains Evaluation AI State
    private val _mainsEvaluationState = MutableStateFlow<AiTaskState<MainsEvaluation>>(AiTaskState.Idle)
    val mainsEvaluationState: StateFlow<AiTaskState<MainsEvaluation>> = _mainsEvaluationState.asStateFlow()

    // Current Affairs AI + Public REST API State
    private val currentAffairsApiService = CurrentAffairsApiService()

    private val _aiCurrentAffairs = MutableStateFlow<List<CurrentAffairsItem>>(emptyList())
    val aiCurrentAffairs: StateFlow<List<CurrentAffairsItem>> = _aiCurrentAffairs.asStateFlow()

    private val _aiAffairsState = MutableStateFlow<AiTaskState<CurrentAffairsItem>>(AiTaskState.Idle)
    val aiAffairsState: StateFlow<AiTaskState<CurrentAffairsItem>> = _aiAffairsState.asStateFlow()

    private val _publicApiAffairs = MutableStateFlow<List<CurrentAffairsItem>>(emptyList())
    val publicApiAffairs: StateFlow<List<CurrentAffairsItem>> = _publicApiAffairs.asStateFlow()

    private val _publicApiSyncState = MutableStateFlow<AiTaskState<Int>>(AiTaskState.Idle)
    val publicApiSyncState: StateFlow<AiTaskState<Int>> = _publicApiSyncState.asStateFlow()

    private val _affairsSearchQuery = MutableStateFlow("")
    val affairsSearchQuery: StateFlow<String> = _affairsSearchQuery.asStateFlow()

    private val _selectedAffairsCategory = MutableStateFlow(CurrentAffairsCategory.ALL)
    val selectedAffairsCategory: StateFlow<CurrentAffairsCategory> = _selectedAffairsCategory.asStateFlow()

    private val _selectedDatePreset = MutableStateFlow(DateFilterPreset.ALL_DATES)
    val selectedDatePreset: StateFlow<DateFilterPreset> = _selectedDatePreset.asStateFlow()

    private val allCombinedAffairs: StateFlow<List<CurrentAffairsItem>> = combine(
        _aiCurrentAffairs,
        _publicApiAffairs
    ) { aiItems, publicItems ->
        (aiItems + publicItems + CurriculumSeedData.currentAffairsItems)
            .distinctBy { it.id }
            .sortedByDescending { it.isoDate }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = CurriculumSeedData.currentAffairsItems
    )

    val affairsCategoryCounts: StateFlow<Map<CurrentAffairsCategory, Int>> = allCombinedAffairs
        .map { items ->
            CurrentAffairsCategory.entries.associateWith { cat ->
                if (cat == CurrentAffairsCategory.ALL) {
                    items.size
                } else {
                    items.count { it.category == cat }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyMap()
        )

    val filteredCurrentAffairs: StateFlow<List<CurrentAffairsItem>> = combine(
        allCombinedAffairs,
        _selectedAffairsCategory,
        _selectedDatePreset,
        _affairsSearchQuery
    ) { items, category, datePreset, rawQuery ->
        val query = rawQuery.trim()
        val todayStr = CurrentAffairsApiService.todayIsoDate(0)
        val yesterdayStr = CurrentAffairsApiService.todayIsoDate(-1)
        val sevenDaysAgoStr = CurrentAffairsApiService.todayIsoDate(-7)

        items.filter { item ->
            val matchesCategory = category == CurrentAffairsCategory.ALL || item.category == category
            val matchesDatePreset = when (datePreset) {
                DateFilterPreset.ALL_DATES -> true
                DateFilterPreset.TODAY -> item.isoDate == todayStr
                DateFilterPreset.YESTERDAY -> item.isoDate == yesterdayStr
                DateFilterPreset.LAST_7_DAYS -> item.isoDate >= sevenDaysAgoStr
            }
            val matchesSearch = if (query.isBlank()) {
                true
            } else {
                item.isoDate.contains(query, ignoreCase = true) ||
                    item.dateLabel.contains(query, ignoreCase = true) ||
                    item.titleEn.contains(query, ignoreCase = true) ||
                    item.titleGu.contains(query, ignoreCase = true) ||
                    item.summaryEn.contains(query, ignoreCase = true) ||
                    item.summaryGu.contains(query, ignoreCase = true) ||
                    item.tagEn.contains(query, ignoreCase = true) ||
                    item.tagGu.contains(query, ignoreCase = true) ||
                    item.syllabusLinkageEn.contains(query, ignoreCase = true) ||
                    item.syllabusLinkageGu.contains(query, ignoreCase = true) ||
                    item.sourceName.contains(query, ignoreCase = true) ||
                    item.category.labelEn.contains(query, ignoreCase = true) ||
                    item.category.labelGu.contains(query, ignoreCase = true)
            }
            matchesCategory && matchesDatePreset && matchesSearch
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = CurriculumSeedData.currentAffairsItems
    )

    init {
        refreshPublicCurrentAffairs()
    }

    // Community Discussion Forum State
    private val _selectedForumTopicId = MutableStateFlow<String?>(null)
    val selectedForumTopicId: StateFlow<String?> = _selectedForumTopicId.asStateFlow()

    // Action Banner / Toast feedback
    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    // Real-time Firestore Flows (Two-Tier Error Handling)
    val bookmarksState: StateFlow<CloudDataState<List<Bookmark>>> = repository.observeBookmarks(currentUserId)
        .map<List<Bookmark>, CloudDataState<List<Bookmark>>> { list ->
            val sorted = list.sortedByDescending { it.createdAt?.seconds ?: 0L }
            CloudDataState.Success(sorted)
        }
        .catch { error ->
            Log.w(TAG, "Error observing bookmarks", error)
            emit(CloudDataState.Error(error.localizedMessage ?: "Failed to load bookmarks"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = CloudDataState.Loading
        )

    val mainsHistoryState: StateFlow<CloudDataState<List<MainsEvaluation>>> = repository.observeMainsEvaluations(currentUserId)
        .map<List<MainsEvaluation>, CloudDataState<List<MainsEvaluation>>> { list ->
            val sorted = list.sortedByDescending { it.createdAt?.seconds ?: 0L }
            CloudDataState.Success(sorted)
        }
        .catch { error ->
            Log.w(TAG, "Error observing mains evaluations", error)
            emit(CloudDataState.Error(error.localizedMessage ?: "Failed to load evaluation history"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = CloudDataState.Loading
        )

    val syllabusProgressState: StateFlow<CloudDataState<List<SyllabusProgress>>> = repository.observeSyllabusProgress(currentUserId)
        .map<List<SyllabusProgress>, CloudDataState<List<SyllabusProgress>>> { list ->
            CloudDataState.Success(list)
        }
        .catch { error ->
            Log.w(TAG, "Error observing syllabus progress", error)
            emit(CloudDataState.Error(error.localizedMessage ?: "Failed to load syllabus tracker"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = CloudDataState.Loading
        )

    val learningDashboardState: StateFlow<LearningDashboardUiState> = combine(
        _aiGeneratedMcqs,
        _selectedAnswers,
        syllabusProgressState
    ) { aiMcqs, answersMap, syllabusCloudState ->
        val allQuestions = aiMcqs + CurriculumSeedData.curatedMcqs
        val persistedList = (syllabusCloudState as? CloudDataState.Success)?.data.orEmpty()

        val subjectMetrics = ExamSubject.entries.map { subject ->
            val subjectQuestions = allQuestions.filter { it.subject == subject.id }
            val totalQ = subjectQuestions.size.coerceAtLeast(1)
            val sessionAttempted = subjectQuestions.count { answersMap.containsKey(it.id) }
            val sessionCorrect = subjectQuestions.count { q -> answersMap[q.id] == q.correctOptionIndex }

            val subjectPersisted = persistedList.filter { it.subject == subject.id }
            val persistedAttempted = subjectPersisted.sumOf { it.questionsAttempted }
            val persistedCorrect = subjectPersisted.sumOf { it.questionsCorrect }

            val effectiveAttempted = maxOf(sessionAttempted, persistedAttempted.coerceAtMost(totalQ))
            val effectiveCorrect = maxOf(sessionCorrect, persistedCorrect.coerceAtMost(effectiveAttempted))

            val syllabusTemplates = CurriculumSeedData.syllabusTopics.filter { it.subject == subject.id }
            val totalSyl = syllabusTemplates.size.coerceAtLeast(1)
            val completedSyl = syllabusTemplates.count { tmpl ->
                persistedList.any { it.topicKey == tmpl.topicKey && it.completed }
            }

            val quizCompletionRatio = (effectiveAttempted.toFloat() / totalQ).coerceIn(0f, 1f)
            val syllabusCompletionRatio = (completedSyl.toFloat() / totalSyl).coerceIn(0f, 1f)
            val combinedCompletion = ((quizCompletionRatio * 0.65f + syllabusCompletionRatio * 0.35f) * 100f)
                .toInt()
                .coerceIn(0, 100)

            val accuracyPct = if (effectiveAttempted > 0) {
                ((effectiveCorrect * 100f) / effectiveAttempted).toInt().coerceIn(0, 100)
            } else if (completedSyl > 0) {
                100
            } else {
                0
            }

            val mastery = ((combinedCompletion * 0.6f) + (accuracyPct * 0.4f)).toInt().coerceIn(0, 100)

            val (shortEn, shortGu) = when (subject) {
                ExamSubject.POLITY -> "Polity" to "બંધારણ"
                ExamSubject.GUJARAT_HISTORY -> "History" to "ઇતિહાસ"
                ExamSubject.GEOGRAPHY -> "Geography" to "ભૂગોળ"
                ExamSubject.ECONOMY -> "Economy" to "અર્થતંત્ર"
                ExamSubject.SCIENCE -> "Science" to "વિજ્ઞાન"
            }

            SubjectProgressMetric(
                subject = subject,
                shortLabelEn = shortEn,
                shortLabelGu = shortGu,
                totalQuestions = subjectQuestions.size,
                attemptedQuestions = effectiveAttempted,
                correctQuestions = effectiveCorrect,
                syllabusTopicsTotal = syllabusTemplates.size,
                syllabusTopicsCompleted = completedSyl,
                completionPercentage = combinedCompletion,
                accuracyPercentage = accuracyPct,
                masteryScore = mastery
            )
        }

        val totalQAvail = subjectMetrics.sumOf { it.totalQuestions }
        val totalQAttempted = subjectMetrics.sumOf { it.attemptedQuestions }
        val totalQCorrect = subjectMetrics.sumOf { it.correctQuestions }
        val totalSylAvail = CurriculumSeedData.syllabusTopics.size
        val totalSylDone = subjectMetrics.sumOf { it.syllabusTopicsCompleted }

        val overallComp = if (subjectMetrics.isNotEmpty()) {
            subjectMetrics.sumOf { it.completionPercentage } / subjectMetrics.size
        } else {
            0
        }
        val overallAcc = if (totalQAttempted > 0) {
            ((totalQCorrect * 100f) / totalQAttempted).toInt().coerceIn(0, 100)
        } else {
            0
        }

        LearningDashboardUiState(
            overallCompletionPercent = overallComp,
            overallAccuracyPercent = overallAcc,
            totalQuestionsAttempted = totalQAttempted,
            totalQuestionsAvailable = totalQAvail,
            totalSyllabusCompleted = totalSylDone,
            totalSyllabusTopics = totalSylAvail,
            subjectMetrics = subjectMetrics
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = LearningDashboardUiState()
    )

    val forumTopicsState: StateFlow<CloudDataState<List<ForumTopic>>> = _selectedCategory
        .flatMapLatest { category ->
            repository.observeForumTopics(category.id)
                .map<List<ForumTopic>, CloudDataState<List<ForumTopic>>> { list ->
                    val sorted = list.sortedWith(
                        compareByDescending<ForumTopic> { it.upvoteCount }
                            .thenByDescending { it.createdAt?.seconds ?: 0L }
                    )
                    CloudDataState.Success(sorted)
                }
                .catch { error ->
                    Log.w(TAG, "Error observing forum topics", error)
                    emit(CloudDataState.Error(error.localizedMessage ?: "Failed to load discussion forum"))
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = CloudDataState.Loading
        )

    val forumRepliesState: StateFlow<CloudDataState<List<ForumReply>>> = _selectedForumTopicId
        .flatMapLatest { topicId ->
            if (topicId.isNullOrBlank()) {
                flowOf(CloudDataState.Success(emptyList()))
            } else {
                val categoryId = _selectedCategory.value.id
                repository.observeForumReplies(topicId, categoryId)
                    .map<List<ForumReply>, CloudDataState<List<ForumReply>>> { list ->
                        val sorted = list.sortedWith(
                            compareByDescending<ForumReply> { it.upvoteCount }
                                .thenBy { it.createdAt?.seconds ?: 0L }
                        )
                        CloudDataState.Success(sorted)
                    }
                    .catch { error ->
                        Log.w(TAG, "Error observing forum replies", error)
                        emit(CloudDataState.Error(error.localizedMessage ?: "Failed to load replies"))
                    }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = CloudDataState.Success(emptyList())
        )

    // --- LAZY GEMINI MODELS WITH STRICT JSON SCHEMAS ---

    private val mcqQuizSchema = Schema.obj(
        properties = mapOf(
            "questions" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "questionTextEn" to Schema.string(description = "Prelims MCQ question in English"),
                        "questionTextGu" to Schema.string(description = "Prelims MCQ question in Gujarati script"),
                        "optionsEn" to Schema.array(
                            items = Schema.string(),
                            description = "Exactly 4 options in English"
                        ),
                        "optionsGu" to Schema.array(
                            items = Schema.string(),
                            description = "Exactly 4 options in Gujarati script"
                        ),
                        "correctOptionIndex" to Schema.integer(description = "0-based index (0 to 3) of the correct option"),
                        "explanationEn" to Schema.string(description = "Detailed analytical explanation in English"),
                        "explanationGu" to Schema.string(description = "Detailed analytical explanation in Gujarati"),
                        "referenceNote" to Schema.string(description = "Standard book or syllabus reference note")
                    )
                ),
                description = "List of 3 exam-grade bilingual MCQs"
            )
        )
    )

    private val mcqModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = mcqQuizSchema
            }
        )
    }

    private val mainsEvalSchema = Schema.obj(
        properties = mapOf(
            "overallScore" to Schema.integer(description = "Score out of 10 based on GPSC/UPSC Mains rubric"),
            "introFeedback" to Schema.string(description = "Evaluation of the Introduction (context, constitutional/historical hook, definitions)"),
            "bodyFeedback" to Schema.string(description = "Evaluation of the Body (multi-dimensional coverage, facts, committees, articles, Gujarat/India examples)"),
            "conclusionFeedback" to Schema.string(description = "Evaluation of the Conclusion (balanced way forward, SDGs / Viksit Bharat @ 2047 vision)"),
            "keyPointsCovered" to Schema.array(
                items = Schema.string(),
                description = "Key syllabus points and strengths identified in the student's answer"
            ),
            "improvementSuggestions" to Schema.array(
                items = Schema.string(),
                description = "Specific actionable points missed or needing improvement"
            ),
            "modelAnswerSummary" to Schema.string(description = "A concise high-scoring model answer blueprint")
        )
    )

    private val mainsEvalModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = mainsEvalSchema
            }
        )
    }

    private val currentAffairsSchema = Schema.obj(
        properties = mapOf(
            "tagEn" to Schema.string(description = "Category tag in English"),
            "tagGu" to Schema.string(description = "Category tag in Gujarati"),
            "titleEn" to Schema.string(description = "Headline in English"),
            "titleGu" to Schema.string(description = "Headline in Gujarati"),
            "summaryEn" to Schema.string(description = "Analytical exam-focused summary in English"),
            "summaryGu" to Schema.string(description = "Analytical exam-focused summary in Gujarati"),
            "syllabusLinkageEn" to Schema.string(description = "Syllabus paper & topic linkage in English"),
            "syllabusLinkageGu" to Schema.string(description = "Syllabus paper & topic linkage in Gujarati"),
            "questionTextEn" to Schema.string(description = "Practice MCQ based on this topic in English"),
            "questionTextGu" to Schema.string(description = "Practice MCQ based on this topic in Gujarati"),
            "optionsEn" to Schema.array(items = Schema.string(), description = "4 options in English"),
            "optionsGu" to Schema.array(items = Schema.string(), description = "4 options in Gujarati"),
            "correctOptionIndex" to Schema.integer(description = "0-based index (0 to 3)"),
            "explanationEn" to Schema.string(description = "Explanation in English"),
            "explanationGu" to Schema.string(description = "Explanation in Gujarati"),
            "referenceNote" to Schema.string(description = "Source reference")
        )
    )

    private val currentAffairsModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = currentAffairsSchema
            }
        )
    }

    // --- UI ACTIONS ---

    fun toggleLanguage() {
        _language.value = if (_language.value == AppLanguage.GUJARATI) {
            AppLanguage.ENGLISH
        } else {
            AppLanguage.GUJARATI
        }
    }

    fun selectExamCategory(category: ExamCategory) {
        _selectedCategory.value = category
        _currentQuestionIndex.value = 0
        _selectedForumTopicId.value = null
    }

    fun selectSubject(subject: ExamSubject) {
        _selectedSubject.value = subject
        _currentQuestionIndex.value = 0
    }

    fun toggleQuizViewMode() {
        _quizViewMode.value = if (_quizViewMode.value == QuizViewMode.ALL_QUESTIONS) {
            QuizViewMode.STEP_BY_STEP
        } else {
            QuizViewMode.ALL_QUESTIONS
        }
    }

    fun nextMcqQuestion() {
        val total = activeQuizSessionState.value.totalQuestions
        if (total > 0) {
            _currentQuestionIndex.value = (_currentQuestionIndex.value + 1).coerceAtMost(total - 1)
        }
    }

    fun previousMcqQuestion() {
        _currentQuestionIndex.value = (_currentQuestionIndex.value - 1).coerceAtLeast(0)
    }

    fun jumpToMcqQuestion(index: Int) {
        val total = activeQuizSessionState.value.totalQuestions
        if (total > 0) {
            _currentQuestionIndex.value = index.coerceIn(0, total - 1)
        }
    }

    fun clearStatusBanner() {
        _statusBanner.value = null
    }

    fun showStatusMessage(message: String) {
        _statusBanner.value = message
    }

    fun selectForumTopic(topicId: String?) {
        _selectedForumTopicId.value = topicId
    }

    fun selectMcqAnswer(question: McqQuestion, optionIndex: Int) {
        val alreadyAnswered = _selectedAnswers.value.containsKey(question.id)
        _selectedAnswers.value = _selectedAnswers.value + (question.id to optionIndex)

        if (!alreadyAnswered) {
            val isCorrect = optionIndex == question.correctOptionIndex
            recordQuizAttemptInSyllabus(question.examCategory, question.subject, isCorrect)
        }
    }

    fun resetMcqQuizAnswers() {
        _selectedAnswers.value = emptyMap()
        _currentQuestionIndex.value = 0
    }

    private fun recordQuizAttemptInSyllabus(examCategory: String, subject: String, isCorrect: Boolean) {
        viewModelScope.launch {
            val currentList = (syllabusProgressState.value as? CloudDataState.Success)?.data.orEmpty()
            val matchingTemplate = CurriculumSeedData.syllabusTopics.firstOrNull {
                it.subject == subject
            } ?: CurriculumSeedData.syllabusTopics.first()

            val existing = currentList.firstOrNull { it.topicKey == matchingTemplate.topicKey }
            val newAttempted = (existing?.questionsAttempted ?: 0) + 1
            val newCorrect = (existing?.questionsCorrect ?: 0) + (if (isCorrect) 1 else 0)

            repository.upsertSyllabusProgress(
                existingProgressId = existing?.id,
                topicKey = matchingTemplate.topicKey,
                examCategory = examCategory,
                subject = subject,
                topicTitleEn = matchingTemplate.titleEn,
                topicTitleGu = matchingTemplate.titleGu,
                completed = existing?.completed ?: false,
                questionsAttempted = newAttempted,
                questionsCorrect = newCorrect
            )
        }
    }

    fun toggleSyllabusTopicCompletion(
        topicKey: String,
        examCategory: String,
        subject: String,
        titleEn: String,
        titleGu: String
    ) {
        viewModelScope.launch {
            val currentList = (syllabusProgressState.value as? CloudDataState.Success)?.data.orEmpty()
            val existing = currentList.firstOrNull { it.topicKey == topicKey }
            val nextCompleted = !(existing?.completed ?: false)

            val res = repository.upsertSyllabusProgress(
                existingProgressId = existing?.id,
                topicKey = topicKey,
                examCategory = examCategory,
                subject = subject,
                topicTitleEn = titleEn,
                topicTitleGu = titleGu,
                completed = nextCompleted,
                questionsAttempted = existing?.questionsAttempted ?: 0,
                questionsCorrect = existing?.questionsCorrect ?: 0
            )
            if (res.isFailure) {
                _statusBanner.value = res.exceptionOrNull()?.localizedMessage ?: "Failed to update syllabus topic"
            }
        }
    }

    fun toggleBookmarkQuestion(question: McqQuestion) {
        viewModelScope.launch {
            val currentBookmarks = (bookmarksState.value as? CloudDataState.Success)?.data.orEmpty()
            val existing = currentBookmarks.firstOrNull {
                it.questionText == question.questionTextEn || it.questionTextGu == question.questionTextGu
            }
            if (existing != null) {
                val res = repository.deleteBookmark(existing.id)
                if (res.isSuccess) {
                    _statusBanner.value = if (_language.value == AppLanguage.GUJARATI) {
                        "બુકમાર્કમાંથી પ્રશ્ન દૂર કરવામાં આવ્યો"
                    } else {
                        "Removed question from Bookmarks"
                    }
                }
            } else {
                val res = repository.createBookmark(
                    examCategory = question.examCategory,
                    subject = question.subject,
                    questionText = question.questionTextEn,
                    questionTextGu = question.questionTextGu,
                    options = question.optionsEn,
                    optionsGu = question.optionsGu,
                    correctOptionIndex = question.correctOptionIndex,
                    explanation = question.explanationEn,
                    explanationGu = question.explanationGu,
                    referenceNote = question.referenceNote
                )
                if (res.isSuccess) {
                    _statusBanner.value = if (_language.value == AppLanguage.GUJARATI) {
                        "રિવિઝન માટે પ્રશ્ન બુકમાર્ક કરવામાં આવ્યો!"
                    } else {
                        "Saved question to Bookmarks for revision!"
                    }
                } else {
                    _statusBanner.value = res.exceptionOrNull()?.localizedMessage ?: "Failed to bookmark"
                }
            }
        }
    }

    fun updateBookmarkPersonalNote(bookmarkId: String, note: String) {
        viewModelScope.launch {
            val res = repository.updateBookmarkNote(bookmarkId, note)
            if (res.isSuccess) {
                _statusBanner.value = if (_language.value == AppLanguage.GUJARATI) {
                    "તમારી રિવિઝન નોંધ સેવ થઈ ગઈ!"
                } else {
                    "Personal revision note saved!"
                }
            } else {
                _statusBanner.value = res.exceptionOrNull()?.localizedMessage ?: "Failed to update note"
            }
        }
    }

    fun deleteBookmarkById(bookmarkId: String) {
        viewModelScope.launch {
            repository.deleteBookmark(bookmarkId)
        }
    }

    fun deleteMainsEvaluationById(evalId: String) {
        viewModelScope.launch {
            repository.deleteMainsEvaluation(evalId)
        }
    }

    // --- COMMUNITY DISCUSSION FORUM ACTIONS ---

    fun createForumTopic(
        authorName: String,
        examCategory: ExamCategory,
        subject: ExamSubject,
        title: String,
        body: String,
        resourceTitle: String?,
        resourceUrl: String?
    ) {
        if (title.isBlank() || body.isBlank()) {
            _statusBanner.value = if (_language.value == AppLanguage.GUJARATI) {
                "કૃપા કરીને ચર્ચાનું શીર્ષક અને વિગત બંને લખો."
            } else {
                "Please enter both a discussion title and post content."
            }
            return
        }
        viewModelScope.launch {
            val res = repository.createForumTopic(
                authorName = authorName,
                examCategory = examCategory.id,
                subject = subject.id,
                title = title,
                body = body,
                resourceTitle = resourceTitle,
                resourceUrl = resourceUrl
            )
            if (res.isSuccess) {
                _statusBanner.value = if (_language.value == AppLanguage.GUJARATI) {
                    "નવો ચર્ચા વિષય પ્રકાશિત થયો!"
                } else {
                    "Discussion topic published!"
                }
            } else {
                _statusBanner.value = res.exceptionOrNull()?.localizedMessage ?: "Failed to create topic"
            }
        }
    }

    fun toggleForumTopicUpvote(topic: ForumTopic) {
        viewModelScope.launch {
            val res = repository.toggleForumTopicUpvote(topic)
            if (res.isFailure) {
                _statusBanner.value = res.exceptionOrNull()?.localizedMessage ?: "Failed to upvote topic"
            }
        }
    }

    fun deleteForumTopic(topicId: String) {
        viewModelScope.launch {
            val res = repository.deleteForumTopic(topicId)
            if (res.isSuccess) {
                if (_selectedForumTopicId.value == topicId) {
                    _selectedForumTopicId.value = null
                }
                _statusBanner.value = if (_language.value == AppLanguage.GUJARATI) {
                    "ચર્ચા વિષય દૂર કરવામાં આવ્યો"
                } else {
                    "Discussion topic deleted"
                }
            } else {
                _statusBanner.value = res.exceptionOrNull()?.localizedMessage ?: "Failed to delete topic"
            }
        }
    }

    fun createForumReply(
        topic: ForumTopic,
        authorName: String,
        body: String,
        resourceTitle: String?,
        resourceUrl: String?
    ) {
        if (body.isBlank()) {
            _statusBanner.value = if (_language.value == AppLanguage.GUJARATI) {
                "કૃપા કરીને તમારો પ્રતિભાવ લખો."
            } else {
                "Please enter a reply comment."
            }
            return
        }
        viewModelScope.launch {
            val res = repository.createForumReply(
                topic = topic,
                authorName = authorName,
                body = body,
                resourceTitle = resourceTitle,
                resourceUrl = resourceUrl
            )
            if (res.isSuccess) {
                _statusBanner.value = if (_language.value == AppLanguage.GUJARATI) {
                    "તમારી ટિપ્પણી પોસ્ટ કરવામાં આવી!"
                } else {
                    "Your reply has been posted!"
                }
            } else {
                _statusBanner.value = res.exceptionOrNull()?.localizedMessage ?: "Failed to post reply"
            }
        }
    }

    fun toggleForumReplyUpvote(reply: ForumReply) {
        viewModelScope.launch {
            val res = repository.toggleForumReplyUpvote(reply)
            if (res.isFailure) {
                _statusBanner.value = res.exceptionOrNull()?.localizedMessage ?: "Failed to upvote comment"
            }
        }
    }

    fun deleteForumReply(replyId: String) {
        viewModelScope.launch {
            repository.deleteForumReply(replyId)
        }
    }

    fun seedStarterForumTopics(authorName: String) {
        val cat = _selectedCategory.value
        viewModelScope.launch {
            if (cat == ExamCategory.GPSC) {
                val t1 = repository.createForumTopic(
                    authorName = authorName,
                    examCategory = ExamCategory.GPSC.id,
                    subject = ExamSubject.GUJARAT_HISTORY.id,
                    title = "સોલંકી કાળ અને મારુ-ગુર્જર સ્થાપત્ય શૈલી માટે શ્રેષ્ઠ સંદર્ભ નોંધ (Solanki Era Architecture Notes)",
                    body = "GPSC ક્લાસ ૧-૨ મેઇન્સ અને પ્રિલિમ્સ બંનેમાં મોઢેરાનું સૂર્યમંદિર, રાણીની વાવ (પાટણ), વડનગર કીર્તિતોરણ અને રુદ્રમહાલય પર દર વર્ષે પ્રશ્નો પૂછાય છે. અહીં GCERT અને ગુજરાત રાજ્ય પુરાતત્વ વિભાગના મુખ્ય મુદ્દાઓની યાદી શેર કરું છું. તમારા શોર્ટ નોટ્સ અને ટ્રિક્સ અહીં કમેન્ટમાં શેર કરો!",
                    resourceTitle = "GCERT Heritage & Gujarat State Archeology Monograph (Ch. 3-5)",
                    resourceUrl = "https://archeology.gujarat.gov.in"
                )
                val t1Id = t1.getOrNull()
                if (!t1Id.isNullOrBlank()) {
                    repository.createForumReply(
                        topic = ForumTopic(
                            id = t1Id,
                            authorId = currentUserId,
                            authorName = authorName,
                            examCategory = ExamCategory.GPSC.id,
                            subject = ExamSubject.GUJARAT_HISTORY.id,
                            replyCount = 0
                        ),
                        authorName = authorName,
                        body = "મેઇન્સમાં ૧૦ ગુણના પ્રશ્નમાં મંદિરના ત્રણ ભાગો — ગર્ભગૃહ (મંડોવર), ગૂઢમંડપ અને સભામંડપ (રંગમંડપ) ની આકૃતિ દોરવાથી ૧.૫ થી ૨ ગુણ વધુ મળે છે!",
                        resourceTitle = "Maru-Gurjara Temple Plan Diagram Checklist",
                        resourceUrl = "https://gpsc.gujarat.gov.in"
                    )
                }

                repository.createForumTopic(
                    authorName = authorName,
                    examCategory = ExamCategory.GPSC.id,
                    subject = ExamSubject.POLITY.id,
                    title = "ગુજરાત પંચાયત અધિનિયમ, ૧૯૯૩ અને PESA કાયદાની સરખામણી (73rd Amendment & PESA)",
                    body = "ગ્રામસભાની સત્તાઓ, સમરસ ગ્રામ યોજના અને અનુસૂચિત વિસ્તારોમાં PESA હેઠળ ગ્રામસભાના વિશેષ અધિકારો અંગેના મહત્વના અનુચ્છેદ ૨૪૩-A થી ૨૪૩-O નું સંકલન.",
                    resourceTitle = "Gujarat Panchayat Department Official Compendium",
                    resourceUrl = "https://panchayat.gujarat.gov.in"
                )
            } else {
                val u1 = repository.createForumTopic(
                    authorName = authorName,
                    examCategory = ExamCategory.UPSC.id,
                    subject = ExamSubject.POLITY.id,
                    title = "High-Yield Supreme Court Judgments for UPSC GS-II (Basic Structure & Federalism)",
                    body = "Let's collaborate on a one-page cheat sheet of landmark Supreme Court cases (Kesavananda Bharati, S.R. Bommai, Puttaswamy Privacy case, and Mineral Area Development Authority fiscal federalism ruling) with exact Articles to quote in Mains answers.",
                    resourceTitle = "Supreme Court Observer — Constitutional Bench Summaries",
                    resourceUrl = "https://main.sci.gov.in"
                )
                val u1Id = u1.getOrNull()
                if (!u1Id.isNullOrBlank()) {
                    repository.createForumReply(
                        topic = ForumTopic(
                            id = u1Id,
                            authorId = currentUserId,
                            authorName = authorName,
                            examCategory = ExamCategory.UPSC.id,
                            subject = ExamSubject.POLITY.id,
                            replyCount = 0
                        ),
                        authorName = authorName,
                        body = "Pairing Article 246A (GST Council cooperative federalism) with the Mohit Minerals judgment is a great value-addition for GS-II federalism questions.",
                        resourceTitle = "PRS Legislative Research — Fiscal Federalism Brief",
                        resourceUrl = "https://prsindia.org"
                    )
                }

                repository.createForumTopic(
                    authorName = authorName,
                    examCategory = ExamCategory.UPSC.id,
                    subject = ExamSubject.ECONOMY.id,
                    title = "Union Budget & Economic Survey Key Data Points for GS-III Mains",
                    body = "Sharing key macroeconomic indicators: fiscal deficit glide path, Capex-to-GDP multiplier, Digital Public Infrastructure (DPI), and Green Hydrogen targets for quoting in Mains introductions and conclusions.",
                    resourceTitle = "Union Budget & Economic Survey Highlights PDF",
                    resourceUrl = "https://www.indiabudget.gov.in"
                )
            }
            _statusBanner.value = if (_language.value == AppLanguage.GUJARATI) {
                "સમુદાય ચર્ચા વિષયો અને અભ્યાસ સાહિત્ય લોડ થઈ ગયા!"
            } else {
                "Starter discussion topics & study resources loaded!"
            }
        }
    }

    // --- 1. AI PRELIMS MCQ GENERATION ---

    fun generateAiMcqQuiz(customSubtopic: String) {
        val category = _selectedCategory.value
        val subject = _selectedSubject.value
        val subtopicClause = if (customSubtopic.isNotBlank()) {
            "Focus specifically on subtopic: ${customSubtopic.trim()}."
        } else {
            "Focus on high-yield exam concepts, constitutional articles, historical facts, or administrative developments."
        }

        val prompt = """
            Generate 3 high-quality competitive exam Prelims Multiple Choice Questions (MCQs) for ${category.titleEn} (${category.subtitleEn}) in the subject "${subject.titleEn}".
            $subtopicClause
            Provide each question, its 4 options, and its detailed explanation in BOTH English and authentic academic Gujarati (ગુજરાતી).
            Ensure optionsEn and optionsGu each have exactly 4 items, and correctOptionIndex is between 0 and 3.
        """.trimIndent()

        viewModelScope.launch {
            _aiQuizGenerationState.value = AiTaskState.Loading
            try {
                val response = withTimeout(60_000L) {
                    mcqModel.generateContent(prompt)
                }
                val jsonString = response.text ?: throw IllegalStateException("Model returned an empty response.")
                val root = JSONObject(jsonString)
                val questionsArray = root.getJSONArray("questions")
                val generatedList = mutableListOf<McqQuestion>()

                for (i in 0 until questionsArray.length()) {
                    val obj = questionsArray.getJSONObject(i)
                    val optionsEnArr = obj.getJSONArray("optionsEn")
                    val optionsGuArr = obj.getJSONArray("optionsGu")
                    val optionsEn = (0 until optionsEnArr.length()).map { optionsEnArr.getString(it) }.take(4)
                    val optionsGu = (0 until optionsGuArr.length()).map { optionsGuArr.getString(it) }.take(4)
                    if (optionsEn.size >= 2 && optionsGu.size >= 2) {
                        generatedList.add(
                            McqQuestion(
                                id = "ai_mcq_${UUID.randomUUID()}",
                                examCategory = category.id,
                                subject = subject.id,
                                questionTextEn = obj.getString("questionTextEn"),
                                questionTextGu = obj.getString("questionTextGu"),
                                optionsEn = optionsEn,
                                optionsGu = optionsGu,
                                correctOptionIndex = obj.getInt("correctOptionIndex").coerceIn(0, optionsEn.size - 1),
                                explanationEn = obj.getString("explanationEn"),
                                explanationGu = obj.getString("explanationGu"),
                                referenceNote = obj.optString("referenceNote", "${category.titleEn} Syllabus • ${subject.titleEn}"),
                                isAiGenerated = true
                            )
                        )
                    }
                }

                if (generatedList.isNotEmpty()) {
                    _aiGeneratedMcqs.value = generatedList + _aiGeneratedMcqs.value
                    _aiQuizGenerationState.value = AiTaskState.Success(generatedList)
                } else {
                    _aiQuizGenerationState.value = AiTaskState.Error("No valid MCQs were generated.")
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "AI MCQ generation timed out", e)
                _aiQuizGenerationState.value = AiTaskState.Error("AI MCQ generation timed out after 60 seconds. Please try again.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI MCQ generation failed", e)
                _aiQuizGenerationState.value = AiTaskState.Error(e.localizedMessage ?: "Failed to generate AI MCQs.")
            }
        }
    }

    // --- 2. AI MAINS DESCRIPTIVE ANSWER EVALUATION ---

    fun evaluateMainsAnswer(
        examCategory: ExamCategory,
        subject: ExamSubject,
        questionPrompt: String,
        studentAnswer: String
    ) {
        if (questionPrompt.isBlank() || studentAnswer.isBlank()) {
            _mainsEvaluationState.value = AiTaskState.Error(
                if (_language.value == AppLanguage.GUJARATI) {
                    "કૃપા કરીને પ્રશ્ન અને તમારો વર્ણનાત્મક ઉત્તર બંને દાખલ કરો."
                } else {
                    "Please enter both the Mains question prompt and your descriptive answer."
                }
            )
            return
        }

        val targetLangName = if (_language.value == AppLanguage.GUJARATI) {
            "Gujarati (ગુજરાતી) with key administrative/constitutional terms in brackets"
        } else {
            "English"
        }

        val prompt = """
            You are a senior examiner for ${examCategory.titleEn} (${examCategory.subtitleEn}) Mains Descriptive Exam in the subject "${subject.titleEn}".
            Evaluate the student's descriptive answer below.
            Provide your feedback in $targetLangName.
            
            Question Prompt:
            $questionPrompt
            
            Student's Descriptive Answer:
            $studentAnswer
            
            Evaluate strictly across:
            1. Introduction (Context, definition, constitutional/historical background)
            2. Body (Structure, analytical depth, facts/articles/committees, Gujarat & national illustrations)
            3. Conclusion (Balanced, forward-looking way forward)
            4. Key points covered by the student
            5. Specific actionable suggestions for improvement
            6. Concise high-scoring Model Answer Summary
        """.trimIndent()

        viewModelScope.launch {
            _mainsEvaluationState.value = AiTaskState.Loading
            try {
                val response = withTimeout(60_000L) {
                    mainsEvalModel.generateContent(prompt)
                }
                val jsonString = response.text ?: throw IllegalStateException("Model returned empty evaluation.")
                val json = JSONObject(jsonString)

                val overallScore = json.optInt("overallScore", 6).coerceIn(0, 10)
                val introFeedback = json.getString("introFeedback")
                val bodyFeedback = json.getString("bodyFeedback")
                val conclusionFeedback = json.getString("conclusionFeedback")
                val keyPointsArr = json.getJSONArray("keyPointsCovered")
                val keyPointsCovered = (0 until keyPointsArr.length()).map { keyPointsArr.getString(it) }
                val suggestionsArr = json.getJSONArray("improvementSuggestions")
                val improvementSuggestions = (0 until suggestionsArr.length()).map { suggestionsArr.getString(it) }
                val modelAnswerSummary = json.getString("modelAnswerSummary")

                val saveResult = repository.createMainsEvaluation(
                    examCategory = examCategory.id,
                    subject = subject.id,
                    questionPrompt = questionPrompt,
                    studentAnswer = studentAnswer,
                    overallScore = overallScore,
                    introFeedback = introFeedback,
                    bodyFeedback = bodyFeedback,
                    conclusionFeedback = conclusionFeedback,
                    keyPointsCovered = keyPointsCovered,
                    improvementSuggestions = improvementSuggestions,
                    modelAnswerSummary = modelAnswerSummary
                )

                val savedId = saveResult.getOrDefault("")
                val evaluation = MainsEvaluation(
                    id = savedId,
                    userId = currentUserId,
                    examCategory = examCategory.id,
                    subject = subject.id,
                    questionPrompt = questionPrompt,
                    studentAnswer = studentAnswer,
                    overallScore = overallScore,
                    introFeedback = introFeedback,
                    bodyFeedback = bodyFeedback,
                    conclusionFeedback = conclusionFeedback,
                    keyPointsCovered = keyPointsCovered,
                    improvementSuggestions = improvementSuggestions,
                    modelAnswerSummary = modelAnswerSummary
                )
                _mainsEvaluationState.value = AiTaskState.Success(evaluation)
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Mains evaluation timed out", e)
                _mainsEvaluationState.value = AiTaskState.Error("Mains evaluation timed out after 60 seconds. Please try again.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Mains evaluation failed", e)
                _mainsEvaluationState.value = AiTaskState.Error(e.localizedMessage ?: "Mains evaluation failed.")
            }
        }
    }

    // --- 3. AI SYLLABUS-ALIGNED CURRENT AFFAIRS BRIEFING ---

    fun generateAiCurrentAffairs(focusTopic: String) {
        val category = _selectedCategory.value
        val subject = _selectedSubject.value
        val topicInstruction = if (focusTopic.isNotBlank()) {
            "Focus on topic: ${focusTopic.trim()}."
        } else {
            "Focus on a major Gujarat administrative policy, GI tag, renewable energy project, or national constitutional/economic update relevant to ${category.titleEn}."
        }

        val prompt = """
            Generate a syllabus-aligned Current Affairs briefing and 1 practice Prelims MCQ for ${category.titleEn} in subject "${subject.titleEn}".
            $topicInstruction
            Provide all fields in BOTH English and authentic Gujarati (ગુજરાતી).
        """.trimIndent()

        viewModelScope.launch {
            _aiAffairsState.value = AiTaskState.Loading
            try {
                val response = withTimeout(60_000L) {
                    currentAffairsModel.generateContent(prompt)
                }
                val jsonString = response.text ?: throw IllegalStateException("Empty response from AI.")
                val json = JSONObject(jsonString)
                val optionsEnArr = json.getJSONArray("optionsEn")
                val optionsGuArr = json.getJSONArray("optionsGu")
                val optionsEn = (0 until optionsEnArr.length()).map { optionsEnArr.getString(it) }.take(4)
                val optionsGu = (0 until optionsGuArr.length()).map { optionsGuArr.getString(it) }.take(4)
                val correctIdx = json.optInt("correctOptionIndex", 0).coerceIn(0, (optionsEn.size - 1).coerceAtLeast(0))

                val mappedCat = when (subject) {
                    ExamSubject.POLITY -> CurrentAffairsCategory.POLITY_GOVERNANCE
                    ExamSubject.ECONOMY -> CurrentAffairsCategory.ECONOMY_POLICY
                    ExamSubject.SCIENCE -> CurrentAffairsCategory.SCIENCE_TECH
                    ExamSubject.GEOGRAPHY -> CurrentAffairsCategory.ENVIRONMENT_GEO
                    ExamSubject.GUJARAT_HISTORY -> if (category == ExamCategory.GPSC) {
                        CurrentAffairsCategory.GUJARAT_SPECIAL
                    } else {
                        CurrentAffairsCategory.HISTORY_CULTURE
                    }
                }
                val todayDate = CurrentAffairsApiService.todayIsoDate(0)

                val item = CurrentAffairsItem(
                    id = "ai_ca_${UUID.randomUUID()}",
                    examCategory = category.id,
                    subject = subject.id,
                    tagEn = json.getString("tagEn"),
                    tagGu = json.getString("tagGu"),
                    dateLabel = "$todayDate • AI Syllabus Brief (${subject.code})",
                    titleEn = json.getString("titleEn"),
                    titleGu = json.getString("titleGu"),
                    summaryEn = json.getString("summaryEn"),
                    summaryGu = json.getString("summaryGu"),
                    syllabusLinkageEn = json.getString("syllabusLinkageEn"),
                    syllabusLinkageGu = json.getString("syllabusLinkageGu"),
                    practiceMcq = McqQuestion(
                        id = "ai_ca_mcq_${UUID.randomUUID()}",
                        examCategory = category.id,
                        subject = subject.id,
                        questionTextEn = json.getString("questionTextEn"),
                        questionTextGu = json.getString("questionTextGu"),
                        optionsEn = optionsEn,
                        optionsGu = optionsGu,
                        correctOptionIndex = correctIdx,
                        explanationEn = json.getString("explanationEn"),
                        explanationGu = json.getString("explanationGu"),
                        referenceNote = json.optString("referenceNote", "${category.titleEn} Current Affairs"),
                        isAiGenerated = true
                    ),
                    isoDate = todayDate,
                    category = mappedCat,
                    sourceName = "Gemini AI Syllabus Brief",
                    sourceUrl = null,
                    isFromPublicApi = false
                )
                _aiCurrentAffairs.value = listOf(item) + _aiCurrentAffairs.value
                _aiAffairsState.value = AiTaskState.Success(item)
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Current affairs generation timed out", e)
                _aiAffairsState.value = AiTaskState.Error("Current affairs generation timed out after 60 seconds.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Current affairs generation failed", e)
                _aiAffairsState.value = AiTaskState.Error(e.localizedMessage ?: "Failed to generate current affairs brief.")
            }
        }
    }

    // --- 4. PUBLIC API CURRENT AFFAIRS FETCH & FILTERING ---

    fun updateAffairsSearchQuery(query: String) {
        _affairsSearchQuery.value = query
    }

    fun selectAffairsCategory(category: CurrentAffairsCategory) {
        _selectedAffairsCategory.value = category
    }

    fun selectDateFilterPreset(preset: DateFilterPreset) {
        _selectedDatePreset.value = preset
    }

    fun refreshPublicCurrentAffairs(
        targetDate: String? = null,
        topicQuery: String? = null
    ) {
        viewModelScope.launch {
            _publicApiSyncState.value = AiTaskState.Loading
            try {
                val resolvedDate = targetDate ?: when (_selectedDatePreset.value) {
                    DateFilterPreset.YESTERDAY -> CurrentAffairsApiService.todayIsoDate(-1)
                    else -> CurrentAffairsApiService.todayIsoDate(0)
                }
                val fetched = currentAffairsApiService.fetchDailyExamNews(
                    targetIsoDate = resolvedDate,
                    topicQuery = topicQuery ?: _affairsSearchQuery.value.takeIf {
                        it.isNotBlank() && !Regex("\\d{4}-\\d{2}-\\d{2}").matches(it.trim())
                    }
                )
                if (fetched.isNotEmpty()) {
                    val merged = (fetched + _publicApiAffairs.value).distinctBy { it.id }
                    _publicApiAffairs.value = merged
                    _publicApiSyncState.value = AiTaskState.Success(merged.size)
                } else {
                    _publicApiSyncState.value = AiTaskState.Success(_publicApiAffairs.value.size)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Public API Current Affairs fetch error", e)
                _publicApiSyncState.value = AiTaskState.Error(
                    e.localizedMessage ?: "Unable to reach public news feed."
                )
            }
        }
    }

    private companion object {
        const val TAG = "GpscViewModel"
    }
}
