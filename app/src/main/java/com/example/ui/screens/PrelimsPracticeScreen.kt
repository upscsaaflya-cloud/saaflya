package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CurriculumSeedData
import com.example.data.model.AppLanguage
import com.example.data.model.Bookmark
import com.example.data.model.ExamCategory
import com.example.data.model.ExamSubject
import com.example.data.model.LearningDashboardUiState
import com.example.data.model.McqQuestion
import com.example.data.model.McqQuizSessionUiState
import com.example.data.model.QuizViewMode
import com.example.data.model.SyllabusProgress
import com.example.ui.components.LearningDashboardSection
import com.example.ui.components.StudyCategoryDashboardGrid
import com.example.ui.components.StudyReminderCard
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.WrongRed
import com.example.viewmodel.AiTaskState

@Composable
fun PrelimsPracticeScreen(
    language: AppLanguage,
    selectedCategory: ExamCategory,
    onSelectCategory: (ExamCategory) -> Unit,
    selectedSubject: ExamSubject,
    onSelectSubject: (ExamSubject) -> Unit,
    aiGeneratedMcqs: List<McqQuestion>,
    selectedAnswers: Map<String, Int>,
    onSelectAnswer: (McqQuestion, Int) -> Unit,
    onResetQuiz: () -> Unit,
    aiQuizState: AiTaskState<List<McqQuestion>>,
    onGenerateAiMcqs: (String) -> Unit,
    bookmarks: List<Bookmark>,
    onToggleBookmark: (McqQuestion) -> Unit,
    quizSessionState: McqQuizSessionUiState = McqQuizSessionUiState(
        category = selectedCategory,
        subject = selectedSubject,
        questions = aiGeneratedMcqs + CurriculumSeedData.curatedMcqs.filter { it.subject == selectedSubject.id },
        selectedAnswers = selectedAnswers
    ),
    onToggleQuizViewMode: () -> Unit = {},
    onNextQuestion: () -> Unit = {},
    onPreviousQuestion: () -> Unit = {},
    onJumpToQuestion: (Int) -> Unit = {},
    learningDashboardState: LearningDashboardUiState = LearningDashboardUiState(),
    syllabusProgress: List<SyllabusProgress> = emptyList(),
    onShowStatusMessage: (String) -> Unit = {},
    onNavigateToMains: () -> Unit = {},
    onNavigateToForum: () -> Unit = {},
    onNavigateToAffairs: () -> Unit = {}
) {
    var customTopicInput by remember { mutableStateOf("") }
    val allAvailableQuestions = remember(aiGeneratedMcqs) {
        aiGeneratedMcqs + CurriculumSeedData.curatedMcqs
    }

    val matchingQuestions = quizSessionState.questions
    val totalAttemptedInView = quizSessionState.attemptedCount
    val totalCorrectInView = quizSessionState.correctCount

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Exam Category Selector (GPSC Class 1-2 vs UPSC)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (language == AppLanguage.GUJARATI) {
                                "પરીક્ષા કેટેગરી પસંદ કરો (Exam Category)"
                            } else {
                                "Select Exam Category (Prelims)"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ExamCategory.entries.forEach { category ->
                                val isSelected = category == selectedCategory
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { onSelectCategory(category) }
                                        .testTag("category_${category.id}"),
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    },
                                    border = if (isSelected) null else BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.outlineVariant
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                                    ) {
                                        Text(
                                            text = if (language == AppLanguage.GUJARATI) category.titleGu else category.titleEn,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            },
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (language == AppLanguage.GUJARATI) category.subtitleGu else category.subtitleEn,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Clickable 2-Column Study Categories Dashboard Grid
            item {
                StudyCategoryDashboardGrid(
                    language = language,
                    selectedCategory = selectedCategory,
                    selectedSubject = selectedSubject,
                    onSelectSubject = onSelectSubject,
                    allQuestions = allAvailableQuestions,
                    selectedAnswers = selectedAnswers,
                    onNavigateToMains = onNavigateToMains,
                    onNavigateToForum = onNavigateToForum,
                    onNavigateToAffairs = onNavigateToAffairs
                )
            }

            // 2B. Interactive Learning Dashboard (Recharts-Style Subject Completion & Accuracy Charts)
            item {
                LearningDashboardSection(
                    language = language,
                    dashboardState = learningDashboardState,
                    selectedSubject = selectedSubject,
                    onSelectSubject = onSelectSubject,
                    initiallyExpanded = true
                )
            }

            // 2C. Daily Syllabus-Aligned Study Reminder & Local Notification Card
            item {
                StudyReminderCard(
                    language = language,
                    syllabusProgress = syllabusProgress,
                    onPracticeTopicSubject = onSelectSubject,
                    onShowStatusMessage = onShowStatusMessage
                )
            }

            // 3. Interactive Subject Quiz Scoreboard & View Mode Switcher
            item {
                SubjectQuizScoreboardCard(
                    language = language,
                    sessionState = quizSessionState,
                    onToggleViewMode = onToggleQuizViewMode,
                    onResetQuiz = onResetQuiz,
                    onJumpToQuestion = onJumpToQuestion
                )
            }

            // 4. AI Prelims Question Generator Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (language == AppLanguage.GUJARATI) {
                                        "AI પ્રશ્નોત્તરી જનરેટર (${selectedSubject.titleGu})"
                                    } else {
                                        "AI Prelims MCQ Generator (${selectedSubject.titleEn})"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = customTopicInput,
                            onValueChange = { customTopicInput = it },
                            placeholder = {
                                Text(
                                    text = if (language == AppLanguage.GUJARATI) {
                                        "પેટા-વિષય લખો (દા.ત. મૂળભૂત અધિકારો, નર્મદા બેસિન, સોલંકી કાળ) અથવા ખાલી રાખો..."
                                    } else {
                                        "Optional subtopic (e.g., Fundamental Rights, River Systems, Solanki era)..."
                                    }
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ai_mcq_topic_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { onGenerateAiMcqs(customTopicInput) },
                                enabled = aiQuizState !is AiTaskState.Loading,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("generate_ai_mcqs_button")
                            ) {
                                if (aiQuizState is AiTaskState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (language == AppLanguage.GUJARATI) "પ્રશ્નો બની રહ્યા છે..." else "Generating MCQs..."
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (language == AppLanguage.GUJARATI) {
                                            "નવા ૩ AI પ્રશ્નો બનાવો"
                                        } else {
                                            "Generate 3 New AI MCQs"
                                        }
                                    )
                                }
                            }

                            if (totalAttemptedInView > 0) {
                                OutlinedButton(
                                    onClick = onResetQuiz,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("reset_quiz_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "$totalCorrectInView/$totalAttemptedInView"
                                    )
                                }
                            }
                        }

                        if (aiQuizState is AiTaskState.Error) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = aiQuizState.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // 5. Interactive MCQ Display (Step-by-Step Focused Mode OR All Questions List Mode)
            if (quizSessionState.viewMode == QuizViewMode.STEP_BY_STEP) {
                val activeQuestion = quizSessionState.activeQuestion
                if (activeQuestion != null) {
                    val activeIndex = quizSessionState.safeCurrentIndex
                    val isBookmarked = bookmarks.any {
                        it.questionText == activeQuestion.questionTextEn ||
                            it.questionTextGu == activeQuestion.questionTextGu
                    }
                    item(key = "step_${activeQuestion.id}") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            McqQuestionCard(
                                index = activeIndex + 1,
                                question = activeQuestion,
                                language = language,
                                selectedOptionIndex = selectedAnswers[activeQuestion.id],
                                isBookmarked = isBookmarked,
                                onSelectOption = { optIdx -> onSelectAnswer(activeQuestion, optIdx) },
                                onToggleBookmark = { onToggleBookmark(activeQuestion) }
                            )

                            // Step-by-Step Previous / Next Navigation Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = onPreviousQuestion,
                                    enabled = activeIndex > 0,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("prev_mcq_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (language == AppLanguage.GUJARATI) "આગળનો પ્રશ્ન" else "Previous"
                                    )
                                }

                                Text(
                                    text = "${activeIndex + 1} / ${matchingQuestions.size}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )

                                Button(
                                    onClick = onNextQuestion,
                                    enabled = activeIndex < matchingQuestions.lastIndex,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("next_mcq_button")
                                ) {
                                    Text(
                                        text = if (language == AppLanguage.GUJARATI) "પછીનો પ્રશ્ન" else "Next"
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                itemsIndexed(
                    items = matchingQuestions,
                    key = { _, q -> q.id }
                ) { index, question ->
                    val isBookmarked = bookmarks.any {
                        it.questionText == question.questionTextEn || it.questionTextGu == question.questionTextGu
                    }
                    McqQuestionCard(
                        index = index + 1,
                        question = question,
                        language = language,
                        selectedOptionIndex = selectedAnswers[question.id],
                        isBookmarked = isBookmarked,
                        onSelectOption = { optIdx -> onSelectAnswer(question, optIdx) },
                        onToggleBookmark = { onToggleBookmark(question) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SubjectQuizScoreboardCard(
    language: AppLanguage,
    sessionState: McqQuizSessionUiState,
    onToggleViewMode: () -> Unit,
    onResetQuiz: () -> Unit,
    onJumpToQuestion: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (language == AppLanguage.GUJARATI) {
                            "${sessionState.subject.titleGu} • પ્રશ્નોત્તરી વિશ્લેષણ"
                        } else {
                            "${sessionState.subject.titleEn} • Live Quiz Tracker"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (language == AppLanguage.GUJARATI) {
                            "વિકલ્પ પસંદ કરતા જ ત્વરિત જવાબ અને સમજૂતી મળશે"
                        } else {
                            "Tap any option for immediate feedback & detailed explanation"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilterChip(
                    selected = sessionState.viewMode == QuizViewMode.STEP_BY_STEP,
                    onClick = onToggleViewMode,
                    label = {
                        Text(
                            text = if (sessionState.viewMode == QuizViewMode.STEP_BY_STEP) {
                                if (language == AppLanguage.GUJARATI) "બધા પ્રશ્નો" else "List Mode"
                            } else {
                                if (language == AppLanguage.GUJARATI) "એક પછી એક" else "Focus Mode"
                            },
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (sessionState.viewMode == QuizViewMode.STEP_BY_STEP) {
                                Icons.Default.ViewAgenda
                            } else {
                                Icons.Default.ViewCarousel
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.testTag("toggle_quiz_view_mode")
                )
            }

            // Score Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuizMetricPill(
                    label = if (language == AppLanguage.GUJARATI) "કુલ પ્રશ્નો" else "Total",
                    value = sessionState.totalQuestions.toString(),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                QuizMetricPill(
                    label = if (language == AppLanguage.GUJARATI) "સાચા ✓" else "Correct ✓",
                    value = sessionState.correctCount.toString(),
                    containerColor = CorrectGreen.copy(alpha = 0.14f),
                    contentColor = CorrectGreen,
                    modifier = Modifier.weight(1f)
                )
                QuizMetricPill(
                    label = if (language == AppLanguage.GUJARATI) "ખોટા ✗" else "Wrong ✗",
                    value = sessionState.incorrectCount.toString(),
                    containerColor = WrongRed.copy(alpha = 0.14f),
                    contentColor = WrongRed,
                    modifier = Modifier.weight(1f)
                )
                QuizMetricPill(
                    label = if (language == AppLanguage.GUJARATI) "ચોકસાઈ" else "Accuracy",
                    value = "${sessionState.accuracyPercent}%",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }

            // Question Jump Pills Row
            if (sessionState.questions.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    sessionState.questions.forEachIndexed { idx, q ->
                        val ans = sessionState.selectedAnswers[q.id]
                        val isAnswered = ans != null
                        val isCorrect = ans == q.correctOptionIndex
                        val isActiveInFocus = sessionState.viewMode == QuizViewMode.STEP_BY_STEP &&
                            sessionState.safeCurrentIndex == idx

                        val bg = when {
                            isAnswered && isCorrect -> CorrectGreen
                            isAnswered && !isCorrect -> WrongRed
                            isActiveInFocus -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        val fg = if (isAnswered || isActiveInFocus) {
                            Color.White
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onJumpToQuestion(idx) }
                                .testTag("jump_q_${idx + 1}"),
                            shape = RoundedCornerShape(10.dp),
                            color = bg
                        ) {
                            Text(
                                text = "Q${idx + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                color = fg,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizMetricPill(
    label: String,
    value: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = containerColor
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = contentColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor
            )
        }
    }
}

@Composable
fun McqQuestionCard(
    index: Int,
    question: McqQuestion,
    language: AppLanguage,
    selectedOptionIndex: Int?,
    isBookmarked: Boolean,
    onSelectOption: (Int) -> Unit,
    onToggleBookmark: () -> Unit
) {
    val options = if (language == AppLanguage.GUJARATI) question.optionsGu else question.optionsEn
    val questionText = if (language == AppLanguage.GUJARATI) question.questionTextGu else question.questionTextEn
    val explanationText = if (language == AppLanguage.GUJARATI) question.explanationGu else question.explanationEn
    val topicTag = if (language == AppLanguage.GUJARATI) question.topicTagGu else question.topicTagEn
    val keyTakeaways = if (language == AppLanguage.GUJARATI) question.keyTakeawaysGu else question.keyTakeawaysEn
    val optionLabels = listOf("A", "B", "C", "D", "E", "F")

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mcq_card_${question.id}"),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Top Header: Question Number, Exam/AI Tag, Topic Tag & Bookmark Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "Q$index • ${question.examCategory}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = if (question.isAiGenerated) "AI Generated" else topicTag,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier.testTag("bookmark_button_${question.id}")
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (isBookmarked) "Remove Bookmark" else "Bookmark Question",
                        tint = if (isBookmarked) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Question Stem
            Text(
                text = questionText,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive Options List
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                options.forEachIndexed { optIndex, optionText ->
                    val isAnswered = selectedOptionIndex != null
                    val isThisSelected = selectedOptionIndex == optIndex
                    val isThisCorrect = optIndex == question.correctOptionIndex

                    val containerColor = when {
                        !isAnswered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        isThisCorrect -> CorrectGreen.copy(alpha = 0.16f)
                        isThisSelected && !isThisCorrect -> WrongRed.copy(alpha = 0.16f)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    }

                    val borderColor = when {
                        !isAnswered -> MaterialTheme.colorScheme.outlineVariant
                        isThisCorrect -> CorrectGreen
                        isThisSelected && !isThisCorrect -> WrongRed
                        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelectOption(optIndex) }
                            .testTag("mcq_${question.id}_option_$optIndex"),
                        shape = RoundedCornerShape(14.dp),
                        color = containerColor,
                        border = BorderStroke(1.5.dp, borderColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = when {
                                    isAnswered && isThisCorrect -> CorrectGreen
                                    isAnswered && isThisSelected && !isThisCorrect -> WrongRed
                                    else -> MaterialTheme.colorScheme.primaryContainer
                                },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = optionLabels.getOrElse(optIndex) { "${optIndex + 1}" },
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (isAnswered && (isThisCorrect || isThisSelected)) {
                                            Color.White
                                        } else {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = optionText,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )

                            if (isAnswered && isThisCorrect) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Correct Option",
                                    tint = CorrectGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            } else if (isAnswered && isThisSelected && !isThisCorrect) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = "Incorrect Option",
                                    tint = WrongRed,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Instant Answer Feedback, Detailed Explanation, Key Exam Takeaways & Reference Note
            AnimatedVisibility(visible = selectedOptionIndex != null) {
                val isCorrectChoice = selectedOptionIndex == question.correctOptionIndex
                val correctLetter = optionLabels.getOrElse(question.correctOptionIndex) { "A" }
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mcq_explanation_${question.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (isCorrectChoice) {
                                    if (language == AppLanguage.GUJARATI) {
                                        "✓ સાચો જવાબ (વિકલ્પ $correctLetter)! વિશ્લેષણ અને સમજૂતી:"
                                    } else {
                                        "✓ Correct Answer (Option $correctLetter)! Detailed Explanation:"
                                    }
                                } else {
                                    if (language == AppLanguage.GUJARATI) {
                                        "✗ ખોટો જવાબ • સાચો વિકલ્પ $correctLetter છે. વિશ્લેષણ જુઓ:"
                                    } else {
                                        "✗ Incorrect • Correct Option is $correctLetter. Detailed Explanation:"
                                    }
                                },
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isCorrectChoice) CorrectGreen else WrongRed,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = explanationText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (keyTakeaways.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Lightbulb,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (language == AppLanguage.GUJARATI) {
                                                    "પરીક્ષા લક્ષી મુખ્ય મુદ્દાઓ (Key Exam Takeaways):"
                                                } else {
                                                    "Key Exam Takeaways:"
                                                },
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.secondary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        keyTakeaways.forEach { bullet ->
                                            Text(
                                                text = "• $bullet",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = question.referenceNote,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
