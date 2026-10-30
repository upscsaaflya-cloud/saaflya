package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class AppLanguage(val labelEn: String, val labelGu: String, val code: String) {
    GUJARATI("Gujarati", "ગુજરાતી", "GU"),
    ENGLISH("English", "English", "EN")
}

enum class ExamCategory(
    val id: String,
    val titleEn: String,
    val titleGu: String,
    val subtitleEn: String,
    val subtitleGu: String
) {
    GPSC(
        id = "GPSC",
        titleEn = "GPSC Class 1-2",
        titleGu = "જીપીએસસી ક્લાસ ૧-૨",
        subtitleEn = "Gujarat Administrative & Civil Services",
        subtitleGu = "ગુજરાત વહીવટી અને મુલ્કી સેવા"
    ),
    UPSC(
        id = "UPSC",
        titleEn = "UPSC CSE",
        titleGu = "યુપીએસસી સિવિલ સર્વિસીસ",
        subtitleEn = "IAS, IPS & Central Civil Services",
        subtitleGu = "આઈએએસ, આઈપીએસ અને કેન્દ્રીય સેવા"
    )
}

enum class ExamSubject(
    val id: String,
    val titleEn: String,
    val titleGu: String,
    val code: String
) {
    POLITY(
        id = "Indian Polity",
        titleEn = "Indian Polity",
        titleGu = "ભારતીય બંધારણ અને રાજવ્યવસ્થા",
        code = "GS-II"
    ),
    GUJARAT_HISTORY(
        id = "Gujarat History & Culture",
        titleEn = "Gujarat History & Culture",
        titleGu = "ગુજરાતનો ઇતિહાસ અને સંસ્કૃતિ",
        code = "GS-I"
    ),
    GEOGRAPHY(
        id = "Geography",
        titleEn = "Geography",
        titleGu = "ભારત અને ગુજરાતની ભૂગોળ",
        code = "GS-I"
    ),
    ECONOMY(
        id = "Indian Economy",
        titleEn = "Indian Economy",
        titleGu = "ભારતીય અર્થતંત્ર અને આયોજન",
        code = "GS-III"
    ),
    SCIENCE(
        id = "General Science",
        titleEn = "General Science & Tech",
        titleGu = "સામાન્ય વિજ્ઞાન અને ટેકનોલોજી",
        code = "GS-III"
    )
}

data class McqQuestion(
    val id: String,
    val examCategory: String,
    val subject: String,
    val questionTextEn: String,
    val questionTextGu: String,
    val optionsEn: List<String>,
    val optionsGu: List<String>,
    val correctOptionIndex: Int,
    val explanationEn: String,
    val explanationGu: String,
    val referenceNote: String,
    val isAiGenerated: Boolean = false,
    val topicTagEn: String = "Core Syllabus",
    val topicTagGu: String = "મુખ્ય અભ્યાસક્રમ",
    val difficulty: String = "Prelims GS",
    val keyTakeawaysEn: List<String> = emptyList(),
    val keyTakeawaysGu: List<String> = emptyList()
)

enum class QuizViewMode {
    ALL_QUESTIONS,
    STEP_BY_STEP
}

data class McqQuizSessionUiState(
    val category: ExamCategory = ExamCategory.GPSC,
    val subject: ExamSubject = ExamSubject.POLITY,
    val questions: List<McqQuestion> = emptyList(),
    val selectedAnswers: Map<String, Int> = emptyMap(),
    val currentQuestionIndex: Int = 0,
    val viewMode: QuizViewMode = QuizViewMode.ALL_QUESTIONS
) {
    val totalQuestions: Int get() = questions.size
    val attemptedCount: Int get() = questions.count { selectedAnswers.containsKey(it.id) }
    val correctCount: Int get() = questions.count { q -> selectedAnswers[q.id] == q.correctOptionIndex }
    val incorrectCount: Int get() = (attemptedCount - correctCount).coerceAtLeast(0)
    val accuracyPercent: Int
        get() = if (attemptedCount > 0) ((correctCount * 100f) / attemptedCount).toInt() else 0
    val safeCurrentIndex: Int
        get() = if (questions.isEmpty()) 0 else currentQuestionIndex.coerceIn(0, questions.lastIndex)
    val activeQuestion: McqQuestion?
        get() = questions.getOrNull(safeCurrentIndex)
}

@IgnoreExtraProperties
data class Bookmark(
    @DocumentId val id: String = "",
    val userId: String = "",
    val examCategory: String = "GPSC",
    val subject: String = "Indian Polity",
    val questionText: String = "",
    val questionTextGu: String = "",
    val options: List<String> = emptyList(),
    val optionsGu: List<String> = emptyList(),
    val correctOptionIndex: Int = 0,
    val explanation: String = "",
    val explanationGu: String = "",
    val referenceNote: String = "",
    val personalNote: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class MainsEvaluation(
    @DocumentId val id: String = "",
    val userId: String = "",
    val examCategory: String = "GPSC",
    val subject: String = "Indian Polity",
    val questionPrompt: String = "",
    val studentAnswer: String = "",
    val overallScore: Int = 0,
    val introFeedback: String = "",
    val bodyFeedback: String = "",
    val conclusionFeedback: String = "",
    val keyPointsCovered: List<String> = emptyList(),
    val improvementSuggestions: List<String> = emptyList(),
    val modelAnswerSummary: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SyllabusProgress(
    @DocumentId val id: String = "",
    val userId: String = "",
    val topicKey: String = "",
    val examCategory: String = "GPSC",
    val subject: String = "Indian Polity",
    val topicTitleEn: String = "",
    val topicTitleGu: String = "",
    val completed: Boolean = false,
    val questionsAttempted: Int = 0,
    val questionsCorrect: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ForumTopic(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val examCategory: String = "GPSC",
    val subject: String = "Indian Polity",
    val title: String = "",
    val body: String = "",
    val resourceTitle: String? = null,
    val resourceUrl: String? = null,
    val upvoterIds: List<String> = emptyList(),
    val upvoteCount: Int = 0,
    val replyCount: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ForumReply(
    @DocumentId val id: String = "",
    val topicId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val examCategory: String = "GPSC",
    val body: String = "",
    val resourceTitle: String? = null,
    val resourceUrl: String? = null,
    val upvoterIds: List<String> = emptyList(),
    val upvoteCount: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

enum class CurrentAffairsCategory(
    val id: String,
    val labelEn: String,
    val labelGu: String,
    val gsPaperCode: String
) {
    ALL("ALL", "All Topics", "તમામ વિષયો", "All GS"),
    GUJARAT_SPECIAL("GUJARAT", "Gujarat & GPSC", "ગુજરાત વિશેષ", "GPSC GS"),
    POLITY_GOVERNANCE("POLITY", "Polity & Governance", "બંધારણ અને શાસન", "GS-II"),
    ECONOMY_POLICY("ECONOMY", "Economy & Policy", "અર્થતંત્ર અને નીતિ", "GS-III"),
    SCIENCE_TECH("SCIENCE", "Science & Space Tech", "વિજ્ઞાન અને અવકાશ", "GS-III"),
    ENVIRONMENT_GEO("ENVIRONMENT", "Environment & Geo", "પર્યાવરણ અને ભૂગોળ", "GS-I/III"),
    HISTORY_CULTURE("HISTORY", "History & Heritage", "ઇતિહાસ અને વારસો", "GS-I")
}

enum class DateFilterPreset(
    val id: String,
    val labelEn: String,
    val labelGu: String
) {
    ALL_DATES("ALL", "All Dates", "તમામ તારીખો"),
    TODAY("TODAY", "Today", "આજે"),
    YESTERDAY("YESTERDAY", "Yesterday", "ગઈકાલે"),
    LAST_7_DAYS("LAST_7_DAYS", "Last 7 Days", "છેલ્લા ૭ દિવસ")
}

data class CurrentAffairsItem(
    val id: String,
    val examCategory: String,
    val subject: String,
    val tagEn: String,
    val tagGu: String,
    val dateLabel: String,
    val titleEn: String,
    val titleGu: String,
    val summaryEn: String,
    val summaryGu: String,
    val syllabusLinkageEn: String,
    val syllabusLinkageGu: String,
    val practiceMcq: McqQuestion,
    val isoDate: String = "2026-10-29",
    val category: CurrentAffairsCategory = CurrentAffairsCategory.GUJARAT_SPECIAL,
    val sourceName: String = "PIB & Gujarat Info",
    val sourceUrl: String? = null,
    val isFromPublicApi: Boolean = false
)

data class MainsPracticePrompt(
    val id: String,
    val examCategory: String,
    val subject: String,
    val marks: Int,
    val wordLimit: Int,
    val promptEn: String,
    val promptGu: String,
    val sampleStructureHintEn: String,
    val sampleStructureHintGu: String
)

data class SyllabusTopicTemplate(
    val topicKey: String,
    val examCategory: String,
    val subject: String,
    val titleEn: String,
    val titleGu: String,
    val subtopicsEn: String,
    val subtopicsGu: String
)

data class SubjectProgressMetric(
    val subject: ExamSubject,
    val shortLabelEn: String,
    val shortLabelGu: String,
    val totalQuestions: Int,
    val attemptedQuestions: Int,
    val correctQuestions: Int,
    val syllabusTopicsTotal: Int,
    val syllabusTopicsCompleted: Int,
    val completionPercentage: Int,
    val accuracyPercentage: Int,
    val masteryScore: Int
)

data class LearningDashboardUiState(
    val overallCompletionPercent: Int = 0,
    val overallAccuracyPercent: Int = 0,
    val totalQuestionsAttempted: Int = 0,
    val totalQuestionsAvailable: Int = 0,
    val totalSyllabusCompleted: Int = 0,
    val totalSyllabusTopics: Int = 0,
    val subjectMetrics: List<SubjectProgressMetric> = emptyList()
)

