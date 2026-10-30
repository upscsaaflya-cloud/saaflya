package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.CurriculumSeedData
import com.example.data.model.AppLanguage
import com.example.data.model.Bookmark
import com.example.data.model.CurrentAffairsCategory
import com.example.data.model.CurrentAffairsItem
import com.example.data.model.DateFilterPreset
import com.example.data.model.ExamSubject
import com.example.data.model.LearningDashboardUiState
import com.example.data.model.McqQuestion
import com.example.data.model.SyllabusProgress
import com.example.data.network.CurrentAffairsApiService
import com.example.ui.components.LearningDashboardSection
import com.example.ui.components.StudyReminderCard
import com.example.viewmodel.AiTaskState
import com.example.viewmodel.CloudDataState

@Composable
fun CurrentAffairsAndSyllabusScreen(
    language: AppLanguage,
    filteredAffairs: List<CurrentAffairsItem>,
    publicApiCount: Int,
    publicApiSyncState: AiTaskState<Int>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: CurrentAffairsCategory,
    onSelectCategory: (CurrentAffairsCategory) -> Unit,
    categoryCounts: Map<CurrentAffairsCategory, Int>,
    selectedDatePreset: DateFilterPreset,
    onSelectDatePreset: (DateFilterPreset) -> Unit,
    onRefreshPublicNews: (String?, String?) -> Unit,
    aiAffairsState: AiTaskState<CurrentAffairsItem>,
    onGenerateAiCurrentAffairs: (String) -> Unit,
    selectedAnswers: Map<String, Int>,
    onSelectMcqAnswer: (McqQuestion, Int) -> Unit,
    bookmarks: List<Bookmark>,
    onToggleBookmark: (McqQuestion) -> Unit,
    syllabusProgressState: CloudDataState<List<SyllabusProgress>>,
    onToggleSyllabusTopic: (String, String, String, String, String) -> Unit,
    learningDashboardState: LearningDashboardUiState = LearningDashboardUiState(),
    selectedSubject: ExamSubject = ExamSubject.POLITY,
    onSelectSubject: (ExamSubject) -> Unit = {},
    onShowStatusMessage: (String) -> Unit = {},
    initialSubTab: Int = 0
) {
    var selectedSubTab by rememberSaveable { mutableIntStateOf(initialSubTab) } // 0 = Current Affairs, 1 = Syllabus Tracker & Reminders
    var focusTopic by rememberSaveable { mutableStateOf("") }
    var showAiGeneratorPanel by rememberSaveable { mutableStateOf(false) }

    val progressList = (syllabusProgressState as? CloudDataState.Success)?.data.orEmpty()
    val totalTopics = CurriculumSeedData.syllabusTopics.size
    val completedTopics = CurriculumSeedData.syllabusTopics.count { tmpl ->
        progressList.any { it.topicKey == tmpl.topicKey && it.completed }
    }
    val completionFraction = if (totalTopics > 0) completedTopics.toFloat() / totalTopics else 0f

    val todayIso = remember { CurrentAffairsApiService.todayIsoDate(0) }
    val yesterdayIso = remember { CurrentAffairsApiService.todayIsoDate(-1) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Segmented Switcher: Daily Current Affairs vs Syllabus Tracker
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SubTabPill(
                        selected = selectedSubTab == 0,
                        icon = Icons.Default.Newspaper,
                        label = if (language == AppLanguage.GUJARATI) {
                            "દૈનિક કરંટ અફેર્સ (${filteredAffairs.size})"
                        } else {
                            "Daily Current Affairs (${filteredAffairs.size})"
                        },
                        onClick = { selectedSubTab = 0 },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("subtab_current_affairs")
                    )
                    SubTabPill(
                        selected = selectedSubTab == 1,
                        icon = Icons.Default.TrackChanges,
                        label = if (language == AppLanguage.GUJARATI) {
                            "અભ્યાસક્રમ ટ્રેકર ($completedTopics/$totalTopics)"
                        } else {
                            "Syllabus Tracker ($completedTopics/$totalTopics)"
                        },
                        onClick = { selectedSubTab = 1 },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("subtab_syllabus_tracker")
                    )
                }
            }

            if (selectedSubTab == 0) {
                // 1. Live Public API Status & Refresh Header Card
                item {
                    PublicApiFeedHeaderCard(
                        language = language,
                        publicApiCount = publicApiCount,
                        publicApiSyncState = publicApiSyncState,
                        todayIso = todayIso,
                        showAiGeneratorPanel = showAiGeneratorPanel,
                        onToggleAiPanel = { showAiGeneratorPanel = !showAiGeneratorPanel },
                        onRefreshLiveFeed = {
                            val trimmed = searchQuery.trim()
                            val dateMatch = Regex("\\d{4}-\\d{2}-\\d{2}").matches(trimmed)
                            onRefreshPublicNews(
                                if (dateMatch) trimmed else null,
                                if (!dateMatch && trimmed.isNotBlank()) trimmed else null
                            )
                        }
                    )
                }

                // 2. Collapsible AI Syllabus-Aligned Current Affairs Generator
                item {
                    AnimatedVisibility(visible = showAiGeneratorPanel) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = if (language == AppLanguage.GUJARATI) {
                                        "AI અભ્યાસક્રમ આધારિત કરંટ અફેર્સ વિશ્લેષણ (Gujarat & National)"
                                    } else {
                                        "AI Syllabus-Linked Current Affairs Briefing"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = focusTopic,
                                    onValueChange = { focusTopic = it },
                                    placeholder = {
                                        Text(
                                            text = if (language == AppLanguage.GUJARATI) {
                                                "મુદ્દો લખો (દા.ત. ગુજરાત બજેટ, ગ્રીન હાઇડ્રોજન, ગિફ્ટ સિટી) અથવા ખાલી રાખો..."
                                            } else {
                                                "Enter topic (e.g., Gujarat Budget, Green Hydrogen, DPDP Act) or leave blank..."
                                            }
                                        )
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("affairs_topic_input")
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { onGenerateAiCurrentAffairs(focusTopic) },
                                    enabled = aiAffairsState !is AiTaskState.Loading,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("generate_affairs_button")
                                ) {
                                    if (aiAffairsState is AiTaskState.Loading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(if (language == AppLanguage.GUJARATI) "તૈયાર થઈ રહ્યું છે..." else "Generating Brief...")
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (language == AppLanguage.GUJARATI) {
                                                "નવું કરંટ અફેર્સ અને MCQ બનાવો"
                                            } else {
                                                "Generate New Brief + Practice MCQ"
                                            }
                                        )
                                    }
                                }
                                if (aiAffairsState is AiTaskState.Error) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = aiAffairsState.message,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Search Bar for Filtering by Date (YYYY-MM-DD) or Topic + Date Presets + Category Filter Bar
                item {
                    SearchAndFilterSection(
                        language = language,
                        searchQuery = searchQuery,
                        onSearchQueryChange = onSearchQueryChange,
                        selectedCategory = selectedCategory,
                        onSelectCategory = onSelectCategory,
                        categoryCounts = categoryCounts,
                        selectedDatePreset = selectedDatePreset,
                        onSelectDatePreset = onSelectDatePreset,
                        todayIso = todayIso,
                        yesterdayIso = yesterdayIso,
                        onFetchBySearchQuery = { query ->
                            val isDate = Regex("\\d{4}-\\d{2}-\\d{2}").matches(query.trim())
                            onRefreshPublicNews(
                                if (isDate) query.trim() else null,
                                if (!isDate) query.trim() else null
                            )
                        }
                    )
                }

                // 4. Empty Filter State or Categorized News List
                if (filteredAffairs.isEmpty()) {
                    item {
                        ElevatedCard(
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (language == AppLanguage.GUJARATI) {
                                        "આ ફિલ્ટર કે તારીખ માટે કોઈ સમાચાર મળ્યા નથી"
                                    } else {
                                        "No current affairs match this date or topic filter"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (language == AppLanguage.GUJARATI) {
                                        "તમે પબ્લિક API માંથી આ વિષય/તારીખના સમાચાર લાઇવ ફેચ કરી શકો છો અથવા ફિલ્ટર રીસેટ કરો."
                                    } else {
                                        "You can fetch live public API articles for this query/date or reset the active filters."
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            onSearchQueryChange("")
                                            onSelectCategory(CurrentAffairsCategory.ALL)
                                            onSelectDatePreset(DateFilterPreset.ALL_DATES)
                                        },
                                        modifier = Modifier.testTag("reset_affairs_filters_button")
                                    ) {
                                        Text(if (language == AppLanguage.GUJARATI) "ફિલ્ટર રીસેટ કરો" else "Reset Filters")
                                    }
                                    if (searchQuery.isNotBlank()) {
                                        Button(
                                            onClick = {
                                                val q = searchQuery.trim()
                                                val isDate = Regex("\\d{4}-\\d{2}-\\d{2}").matches(q)
                                                onRefreshPublicNews(
                                                    if (isDate) q else null,
                                                    if (!isDate) q else null
                                                )
                                            },
                                            modifier = Modifier.testTag("fetch_query_from_api_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CloudSync,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                if (language == AppLanguage.GUJARATI) {
                                                    "API માંથી શોધો"
                                                } else {
                                                    "Fetch from Public API"
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    items(
                        items = filteredAffairs,
                        key = { it.id }
                    ) { affair ->
                        val isBookmarked = bookmarks.any {
                            it.questionText == affair.practiceMcq.questionTextEn ||
                                it.questionTextGu == affair.practiceMcq.questionTextGu
                        }
                        CurrentAffairsCard(
                            item = affair,
                            language = language,
                            selectedOptionIndex = selectedAnswers[affair.practiceMcq.id],
                            isBookmarked = isBookmarked,
                            onSelectOption = { optIdx -> onSelectMcqAnswer(affair.practiceMcq, optIdx) },
                            onToggleBookmark = { onToggleBookmark(affair.practiceMcq) },
                            onGenerateAiDeepDive = {
                                onGenerateAiCurrentAffairs(affair.titleEn)
                            }
                        )
                    }
                }
            } else {
                // Syllabus Tracker, Daily Study Reminder & Learning Dashboard Charts View
                item {
                    StudyReminderCard(
                        language = language,
                        syllabusProgress = progressList,
                        onPracticeTopicSubject = onSelectSubject,
                        onShowStatusMessage = onShowStatusMessage
                    )
                }

                item {
                    LearningDashboardSection(
                        language = language,
                        dashboardState = learningDashboardState,
                        selectedSubject = selectedSubject,
                        onSelectSubject = onSelectSubject,
                        initiallyExpanded = true
                    )
                }

                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = if (language == AppLanguage.GUJARATI) {
                                    "GPSC અને UPSC અભ્યાસક્રમ પ્રગતિ (Syllabus Progress)"
                                } else {
                                    "GPSC & UPSC Syllabus Completion Tracker"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { completionFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val totalAttempts = progressList.sumOf { it.questionsAttempted }
                            val totalCorrect = progressList.sumOf { it.questionsCorrect }
                            Text(
                                text = if (language == AppLanguage.GUJARATI) {
                                    "પૂર્ણ કરેલા મુદ્દા: $completedTopics / $totalTopics • કુલ સોલ્વ કરેલા MCQ: $totalAttempts (સાચા: $totalCorrect)"
                                } else {
                                    "Topics Completed: $completedTopics / $totalTopics • Total MCQs Practiced: $totalAttempts (Correct: $totalCorrect)"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                items(
                    items = CurriculumSeedData.syllabusTopics,
                    key = { it.topicKey }
                ) { topic ->
                    val record = progressList.firstOrNull { it.topicKey == topic.topicKey }
                    val isCompleted = record?.completed ?: false

                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onToggleSyllabusTopic(
                                    topic.topicKey,
                                    topic.examCategory,
                                    topic.subject,
                                    topic.titleEn,
                                    topic.titleGu
                                )
                            }
                            .testTag("syllabus_topic_${topic.topicKey}"),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = if (isCompleted) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                contentDescription = if (isCompleted) "Completed" else "Mark Completed",
                                tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(28.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "${topic.examCategory} • ${topic.subject}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                    if (record != null && record.questionsAttempted > 0) {
                                        Text(
                                            text = "Quiz: ${record.questionsCorrect}/${record.questionsAttempted}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (language == AppLanguage.GUJARATI) topic.titleGu else topic.titleEn,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (language == AppLanguage.GUJARATI) topic.subtopicsGu else topic.subtopicsEn,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PublicApiFeedHeaderCard(
    language: AppLanguage,
    publicApiCount: Int,
    publicApiSyncState: AiTaskState<Int>,
    todayIso: String,
    showAiGeneratorPanel: Boolean,
    onToggleAiPanel: () -> Unit,
    onRefreshLiveFeed: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("public_api_status_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (language == AppLanguage.GUJARATI) {
                                "લાઇવ પબ્લિક API દૈનિક સમાચાર ફીડ"
                            } else {
                                "Live Public API Exam News Feed"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val statusSubtitle = when (publicApiSyncState) {
                            is AiTaskState.Loading -> if (language == AppLanguage.GUJARATI) {
                                "પબ્લિક API માંથી તાજા સમાચાર લોડ થઈ રહ્યા છે..."
                            } else {
                                "Fetching live daily exam news from public API..."
                            }
                            is AiTaskState.Error -> if (language == AppLanguage.GUJARATI) {
                                "ઓફલાઇન/API ચેતવણી • ક્યુરેટેડ ડેટા ઉપલબ્ધ ($todayIso)"
                            } else {
                                "Public feed offline • Showing cached/curated feed ($todayIso)"
                            }
                            else -> if (language == AppLanguage.GUJARATI) {
                                "Wikimedia અને Science API • $publicApiCount લાઇવ લેખો ($todayIso)"
                            } else {
                                "Wikimedia & Spaceflight APIs • $publicApiCount live items ($todayIso)"
                            }
                        }
                        Text(
                            text = statusSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onRefreshLiveFeed,
                    enabled = publicApiSyncState !is AiTaskState.Loading,
                    modifier = Modifier.testTag("refresh_public_api_button")
                ) {
                    if (publicApiSyncState is AiTaskState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = if (language == AppLanguage.GUJARATI) "તાજા સમાચાર મેળવો" else "Refresh Public API News",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = if (language == AppLanguage.GUJARATI) {
                            "GPSC ક્લાસ ૧-૨ અને UPSC GS-I, II, III"
                        } else {
                            "GPSC Class 1-2 & UPSC GS-I, II, III"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                TextButton(
                    onClick = onToggleAiPanel,
                    modifier = Modifier.testTag("toggle_ai_affairs_generator_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == AppLanguage.GUJARATI) {
                            if (showAiGeneratorPanel) "AI જનરેટર છુપાવો" else "+ AI કસ્ટમ ટોપિક"
                        } else {
                            if (showAiGeneratorPanel) "Hide AI Generator" else "+ AI Custom Topic"
                        },
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchAndFilterSection(
    language: AppLanguage,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: CurrentAffairsCategory,
    onSelectCategory: (CurrentAffairsCategory) -> Unit,
    categoryCounts: Map<CurrentAffairsCategory, Int>,
    selectedDatePreset: DateFilterPreset,
    onSelectDatePreset: (DateFilterPreset) -> Unit,
    todayIso: String,
    yesterdayIso: String,
    onFetchBySearchQuery: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Search Bar for Date or Topic
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = if (language == AppLanguage.GUJARATI) "શોધો" else "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
                            modifier = Modifier.testTag("clear_affairs_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = if (language == AppLanguage.GUJARATI) "સાફ કરો" else "Clear search"
                            )
                        }
                    }
                },
                placeholder = {
                    Text(
                        text = if (language == AppLanguage.GUJARATI) {
                            "તારીખ ($todayIso) અથવા વિષય શોધો (દા.ત. ISRO, ધોલેરા, બંધારણ)..."
                        } else {
                            "Search by date ($todayIso) or topic (e.g., ISRO, Dholera, Polity)..."
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("affairs_search_bar")
            )

            // Action banner when a specific YYYY-MM-DD date or keyword is typed
            if (searchQuery.trim().length >= 3) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.GUJARATI) {
                            "ફિલ્ટર: \"${searchQuery.trim()}\""
                        } else {
                            "Filtering by: \"${searchQuery.trim()}\""
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(
                        onClick = { onFetchBySearchQuery(searchQuery.trim()) },
                        modifier = Modifier.testTag("search_api_fetch_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (language == AppLanguage.GUJARATI) {
                                "પબ્લિક API માંથી લાઇવ શોધો"
                            } else {
                                "Query Public API"
                            },
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Date Filter Presets Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (language == AppLanguage.GUJARATI) "તારીખ ફિલ્ટર:" else "Filter by Date:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("affairs_date_filter_row")
            ) {
                items(
                    items = DateFilterPreset.entries,
                    key = { it.id }
                ) { preset ->
                    val selected = selectedDatePreset == preset
                    val dateSuffix = when (preset) {
                        DateFilterPreset.TODAY -> " ($todayIso)"
                        DateFilterPreset.YESTERDAY -> " ($yesterdayIso)"
                        else -> ""
                    }
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectDatePreset(preset) },
                        label = {
                            Text(
                                text = (if (language == AppLanguage.GUJARATI) preset.labelGu else preset.labelEn) + dateSuffix,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        modifier = Modifier.testTag("date_chip_${preset.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Categorized Topic Filter Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (language == AppLanguage.GUJARATI) "વિષય મુજબ વર્ગીકરણ:" else "Categorized Topics:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("affairs_category_filter_row")
            ) {
                items(
                    items = CurrentAffairsCategory.entries,
                    key = { it.id }
                ) { category ->
                    val selected = selectedCategory == category
                    val count = categoryCounts[category] ?: 0
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectCategory(category) },
                        leadingIcon = {
                            Icon(
                                imageVector = categoryIcon(category),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = {
                            val title = if (language == AppLanguage.GUJARATI) category.labelGu else category.labelEn
                            Text(
                                text = "$title ($count)",
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("category_chip_${category.id}")
                    )
                }
            }
        }
    }
}

private fun categoryIcon(category: CurrentAffairsCategory): ImageVector {
    return when (category) {
        CurrentAffairsCategory.ALL -> Icons.Default.Newspaper
        CurrentAffairsCategory.GUJARAT_SPECIAL -> Icons.Default.AccountBalance
        CurrentAffairsCategory.POLITY_GOVERNANCE -> Icons.Default.Gavel
        CurrentAffairsCategory.ECONOMY_POLICY -> Icons.Default.TrendingUp
        CurrentAffairsCategory.SCIENCE_TECH -> Icons.Default.Science
        CurrentAffairsCategory.ENVIRONMENT_GEO -> Icons.Default.Public
        CurrentAffairsCategory.HISTORY_CULTURE -> Icons.Default.HistoryEdu
    }
}

@Composable
private fun SubTabPill(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CurrentAffairsCard(
    item: CurrentAffairsItem,
    language: AppLanguage,
    selectedOptionIndex: Int?,
    isBookmarked: Boolean,
    onSelectOption: (Int) -> Unit,
    onToggleBookmark: () -> Unit,
    onGenerateAiDeepDive: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    var showPracticeMcq by rememberSaveable(item.id) { mutableStateOf(!item.isFromPublicApi) }

    ElevatedCard(
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("affairs_card_${item.id}")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Top Row: Category Badge + Source Badge + ISO Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = categoryIcon(item.category),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (language == AppLanguage.GUJARATI) item.category.labelGu else item.category.labelEn,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (item.isFromPublicApi) {
                            Color(0xFF007075).copy(alpha = 0.14f)
                        } else {
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                        }
                    ) {
                        Text(
                            text = if (item.isFromPublicApi) {
                                if (language == AppLanguage.GUJARATI) "લાઇવ API" else "Live API"
                            } else {
                                item.examCategory
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (item.isFromPublicApi) {
                                Color(0xFF007075)
                            } else {
                                MaterialTheme.colorScheme.onTertiaryContainer
                            },
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = item.isoDate,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Headline
            Text(
                text = if (language == AppLanguage.GUJARATI) item.titleGu else item.titleEn,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Summary
            Text(
                text = if (language == AppLanguage.GUJARATI) item.summaryGu else item.summaryEn,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Syllabus Linkage Box
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "📌 " + (if (language == AppLanguage.GUJARATI) item.syllabusLinkageGu else item.syllabusLinkageEn),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Source Attribution & Action Buttons Row (Read Source / AI Bilingual Deep Dive / Toggle MCQ)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.sourceName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!item.sourceUrl.isNullOrBlank()) {
                        TextButton(
                            onClick = {
                                runCatching { uriHandler.openUri(item.sourceUrl) }
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.testTag("affairs_source_link_${item.id}")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = "Open Source Link",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (language == AppLanguage.GUJARATI) "સ્ત્રોત જુઓ" else "Source",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (item.isFromPublicApi) {
                        OutlinedButton(
                            onClick = onGenerateAiDeepDive,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("ai_deep_dive_button_${item.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (language == AppLanguage.GUJARATI) "AI ગુજરાતી વિશ્લેષણ" else "AI Exam Brief",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    TextButton(
                        onClick = { showPracticeMcq = !showPracticeMcq },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("toggle_affairs_mcq_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Quiz,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (language == AppLanguage.GUJARATI) {
                                if (showPracticeMcq) "MCQ છુપાવો" else "પ્રિલિમ્સ MCQ"
                            } else {
                                if (showPracticeMcq) "Hide MCQ" else "Practice MCQ"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = if (showPracticeMcq) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = showPracticeMcq) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (language == AppLanguage.GUJARATI) {
                            "આ મુદ્દા પર આધારિત પ્રિલિમ્સ MCQ (બુકમાર્ક કરો):"
                        } else {
                            "Syllabus-Linked Prelims Practice Question:"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    McqQuestionCard(
                        index = 1,
                        question = item.practiceMcq,
                        language = language,
                        selectedOptionIndex = selectedOptionIndex,
                        isBookmarked = isBookmarked,
                        onSelectOption = onSelectOption,
                        onToggleBookmark = onToggleBookmark
                    )
                }
            }
        }
    }
}
