package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.CurriculumSeedData
import com.example.data.model.AppLanguage
import com.example.data.repository.GpscRepository
import com.example.security.configureAppCheck
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOut
import com.example.ui.screens.BookmarksScreen
import com.example.ui.screens.CommunityForumScreen
import com.example.ui.screens.CurrentAffairsAndSyllabusScreen
import com.example.ui.screens.MainsEvaluationScreen
import com.example.ui.screens.PrelimsPracticeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.CloudDataState
import com.example.viewmodel.GpscViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

enum class GpscDestination(
    val labelEn: String,
    val labelGu: String,
    val icon: ImageVector,
    val tag: String
) {
    PRELIMS("Prelims", "પ્રિલિમ્સ", Icons.Default.Quiz, "nav_prelims"),
    MAINS("Mains AI", "મેઇન્સ AI", Icons.AutoMirrored.Filled.FactCheck, "nav_mains"),
    AFFAIRS("Affairs", "કરંટ અફેર્સ", Icons.Default.Newspaper, "nav_affairs"),
    FORUM("Forum", "ચર્ચા મંચ", Icons.Default.Forum, "nav_forum"),
    BOOKMARKS("Saved", "બુકમાર્ક્સ", Icons.Default.Bookmarks, "nav_bookmarks")
}

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Mandatory App Check configuration with launch intent before UI or ViewModels
        configureAppCheck(this, intent)
        com.example.notification.StudyReminderManager.ensureNotificationChannel(this)

        enableEdgeToEdge()
        setContent {
            val systemDark = isSystemInDarkTheme()
            var isDarkTheme by rememberSaveable { mutableStateOf(systemDark) }
            var authLanguage by rememberSaveable { mutableStateOf(AppLanguage.GUJARATI) }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                AppNavigation(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = !isDarkTheme },
                    authLanguage = authLanguage,
                    onToggleAuthLanguage = {
                        authLanguage = if (authLanguage == AppLanguage.GUJARATI) {
                            AppLanguage.ENGLISH
                        } else {
                            AppLanguage.GUJARATI
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AppNavigation(
    auth: FirebaseAuth = Firebase.auth,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    authLanguage: AppLanguage,
    onToggleAuthLanguage: () -> Unit
) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        AuthScreen(
            language = authLanguage,
            onToggleLanguage = onToggleAuthLanguage,
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme,
            onAuthSuccess = {}
        )
    } else {
        GpscMainDashboard(
            currentUser = user,
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpscMainDashboard(
    currentUser: FirebaseUser,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val currentUserId = currentUser.uid
    val currentUserName = currentUser.displayName?.takeIf { it.isNotBlank() }
        ?: currentUser.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
        ?: "GPSC Aspirant"

    val viewModel: GpscViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                GpscViewModel(GpscRepository(db), currentUserId)
            }
        }
    )

    val language by viewModel.language.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedSubject by viewModel.selectedSubject.collectAsStateWithLifecycle()
    val aiGeneratedMcqs by viewModel.aiGeneratedMcqs.collectAsStateWithLifecycle()
    val selectedAnswers by viewModel.selectedAnswers.collectAsStateWithLifecycle()
    val activeQuizSessionState by viewModel.activeQuizSessionState.collectAsStateWithLifecycle()
    val aiQuizState by viewModel.aiQuizGenerationState.collectAsStateWithLifecycle()
    val mainsEvaluationState by viewModel.mainsEvaluationState.collectAsStateWithLifecycle()
    val mainsHistoryState by viewModel.mainsHistoryState.collectAsStateWithLifecycle()
    val filteredCurrentAffairs by viewModel.filteredCurrentAffairs.collectAsStateWithLifecycle()
    val publicApiAffairs by viewModel.publicApiAffairs.collectAsStateWithLifecycle()
    val publicApiSyncState by viewModel.publicApiSyncState.collectAsStateWithLifecycle()
    val affairsSearchQuery by viewModel.affairsSearchQuery.collectAsStateWithLifecycle()
    val selectedAffairsCategory by viewModel.selectedAffairsCategory.collectAsStateWithLifecycle()
    val affairsCategoryCounts by viewModel.affairsCategoryCounts.collectAsStateWithLifecycle()
    val selectedDatePreset by viewModel.selectedDatePreset.collectAsStateWithLifecycle()
    val aiAffairsState by viewModel.aiAffairsState.collectAsStateWithLifecycle()
    val bookmarksState by viewModel.bookmarksState.collectAsStateWithLifecycle()
    val syllabusProgressState by viewModel.syllabusProgressState.collectAsStateWithLifecycle()
    val learningDashboardState by viewModel.learningDashboardState.collectAsStateWithLifecycle()
    val forumTopicsState by viewModel.forumTopicsState.collectAsStateWithLifecycle()
    val selectedForumTopicId by viewModel.selectedForumTopicId.collectAsStateWithLifecycle()
    val forumRepliesState by viewModel.forumRepliesState.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()

    var currentDestination by rememberSaveable { mutableStateOf(GpscDestination.PRELIMS) }
    var showProfileDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(statusBanner) {
        val msg = statusBanner
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusBanner()
        }
    }

    if (currentDestination != GpscDestination.PRELIMS) {
        BackHandler {
            currentDestination = GpscDestination.PRELIMS
        }
    }

    val bookmarks = (bookmarksState as? CloudDataState.Success)?.data.orEmpty()
    val evaluations = (mainsHistoryState as? CloudDataState.Success)?.data.orEmpty()
    val syllabusList = (syllabusProgressState as? CloudDataState.Success)?.data.orEmpty()
    val forumTopics = (forumTopicsState as? CloudDataState.Success)?.data.orEmpty()

    if (showProfileDialog) {
        UserProfileDialog(
            user = currentUser,
            language = language,
            bookmarksCount = bookmarks.size,
            evaluationsCount = evaluations.size,
            completedTopicsCount = syllabusList.count { it.completed },
            totalAttemptedMcqs = syllabusList.sumOf { it.questionsAttempted },
            forumTopicsCount = forumTopics.count { it.authorId == currentUserId },
            onDismiss = { showProfileDialog = false }
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            com.example.ui.components.SaaflyaFeatherEmblemBadge(
                                size = 40.dp,
                                modifier = Modifier.testTag("header_brand_logo")
                            )
                            Column {
                                Text(
                                    text = if (language == AppLanguage.GUJARATI) "સાફલ્ય AI GPSC" else "GPSCAI",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (language == AppLanguage.GUJARATI) "સફળતાની શરૂઆત" else "GPSC & UPSC AI",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    },
                    actions = {
                        // Bilingual Language Switcher Pill
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.toggleLanguage() }
                                .testTag("header_language_selector"),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = "Language Selector",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.GUJARATI) "ગુજરાતી" else "English",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        // Dark / Light Mode Toggle
                        IconButton(
                            onClick = onToggleTheme,
                            modifier = Modifier.testTag("header_theme_toggle")
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = if (isDarkTheme) "Light Mode" else "Dark Mode"
                            )
                        }

                        // User Profile Summary Button
                        IconButton(
                            onClick = { showProfileDialog = true },
                            modifier = Modifier.testTag("header_profile_button")
                        ) {
                            val initial = (currentUser.displayName?.firstOrNull()
                                ?: currentUser.email?.firstOrNull()
                                ?: 'A').uppercaseChar()
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initial.toString(),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                if (!isWideScreen) {
                    NavigationBar {
                        GpscDestination.entries.forEach { dest ->
                            val selected = currentDestination == dest
                            NavigationBarItem(
                                selected = selected,
                                onClick = { currentDestination = dest },
                                icon = {
                                    if (dest == GpscDestination.BOOKMARKS && bookmarks.isNotEmpty()) {
                                        BadgedBox(
                                            badge = {
                                                Badge { Text(bookmarks.size.toString()) }
                                            }
                                        ) {
                                            Icon(imageVector = dest.icon, contentDescription = dest.labelEn)
                                        }
                                    } else {
                                        Icon(imageVector = dest.icon, contentDescription = dest.labelEn)
                                    }
                                },
                                label = {
                                    Text(
                                        text = if (language == AppLanguage.GUJARATI) dest.labelGu else dest.labelEn,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                modifier = Modifier.testTag(dest.tag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isWideScreen) {
                    NavigationRail(
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        GpscDestination.entries.forEach { dest ->
                            val selected = currentDestination == dest
                            NavigationRailItem(
                                selected = selected,
                                onClick = { currentDestination = dest },
                                icon = {
                                    if (dest == GpscDestination.BOOKMARKS && bookmarks.isNotEmpty()) {
                                        BadgedBox(
                                            badge = {
                                                Badge { Text(bookmarks.size.toString()) }
                                            }
                                        ) {
                                            Icon(imageVector = dest.icon, contentDescription = dest.labelEn)
                                        }
                                    } else {
                                        Icon(imageVector = dest.icon, contentDescription = dest.labelEn)
                                    }
                                },
                                label = {
                                    Text(
                                        text = if (language == AppLanguage.GUJARATI) dest.labelGu else dest.labelEn
                                    )
                                },
                                modifier = Modifier.testTag(dest.tag)
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (currentDestination) {
                        GpscDestination.PRELIMS -> PrelimsPracticeScreen(
                            language = language,
                            selectedCategory = selectedCategory,
                            onSelectCategory = viewModel::selectExamCategory,
                            selectedSubject = selectedSubject,
                            onSelectSubject = viewModel::selectSubject,
                            aiGeneratedMcqs = aiGeneratedMcqs,
                            selectedAnswers = selectedAnswers,
                            onSelectAnswer = viewModel::selectMcqAnswer,
                            onResetQuiz = viewModel::resetMcqQuizAnswers,
                            aiQuizState = aiQuizState,
                            onGenerateAiMcqs = viewModel::generateAiMcqQuiz,
                            bookmarks = bookmarks,
                            onToggleBookmark = viewModel::toggleBookmarkQuestion,
                            quizSessionState = activeQuizSessionState,
                            onToggleQuizViewMode = viewModel::toggleQuizViewMode,
                            onNextQuestion = viewModel::nextMcqQuestion,
                            onPreviousQuestion = viewModel::previousMcqQuestion,
                            onJumpToQuestion = viewModel::jumpToMcqQuestion,
                            learningDashboardState = learningDashboardState,
                            syllabusProgress = syllabusList,
                            onShowStatusMessage = viewModel::showStatusMessage,
                            onNavigateToMains = { currentDestination = GpscDestination.MAINS },
                            onNavigateToForum = { currentDestination = GpscDestination.FORUM },
                            onNavigateToAffairs = { currentDestination = GpscDestination.AFFAIRS }
                        )

                        GpscDestination.MAINS -> MainsEvaluationScreen(
                            language = language,
                            selectedCategory = selectedCategory,
                            onSelectCategory = viewModel::selectExamCategory,
                            selectedSubject = selectedSubject,
                            onSelectSubject = viewModel::selectSubject,
                            mainsEvaluationState = mainsEvaluationState,
                            mainsHistoryState = mainsHistoryState,
                            onEvaluateMains = viewModel::evaluateMainsAnswer,
                            onDeleteEvaluation = viewModel::deleteMainsEvaluationById
                        )

                        GpscDestination.AFFAIRS -> CurrentAffairsAndSyllabusScreen(
                            language = language,
                            filteredAffairs = filteredCurrentAffairs,
                            publicApiCount = publicApiAffairs.size,
                            publicApiSyncState = publicApiSyncState,
                            searchQuery = affairsSearchQuery,
                            onSearchQueryChange = viewModel::updateAffairsSearchQuery,
                            selectedCategory = selectedAffairsCategory,
                            onSelectCategory = viewModel::selectAffairsCategory,
                            categoryCounts = affairsCategoryCounts,
                            selectedDatePreset = selectedDatePreset,
                            onSelectDatePreset = viewModel::selectDateFilterPreset,
                            onRefreshPublicNews = viewModel::refreshPublicCurrentAffairs,
                            aiAffairsState = aiAffairsState,
                            onGenerateAiCurrentAffairs = viewModel::generateAiCurrentAffairs,
                            selectedAnswers = selectedAnswers,
                            onSelectMcqAnswer = viewModel::selectMcqAnswer,
                            bookmarks = bookmarks,
                            onToggleBookmark = viewModel::toggleBookmarkQuestion,
                            syllabusProgressState = syllabusProgressState,
                            onToggleSyllabusTopic = viewModel::toggleSyllabusTopicCompletion,
                            learningDashboardState = learningDashboardState,
                            selectedSubject = selectedSubject,
                            onSelectSubject = { subj ->
                                viewModel.selectSubject(subj)
                                currentDestination = GpscDestination.PRELIMS
                            },
                            onShowStatusMessage = viewModel::showStatusMessage
                        )

                        GpscDestination.FORUM -> CommunityForumScreen(
                            currentUserId = currentUserId,
                            currentUserName = currentUserName,
                            language = language,
                            selectedCategory = selectedCategory,
                            onSelectCategory = viewModel::selectExamCategory,
                            selectedSubject = selectedSubject,
                            onSelectSubject = viewModel::selectSubject,
                            forumTopicsState = forumTopicsState,
                            selectedTopicId = selectedForumTopicId,
                            onSelectTopicId = viewModel::selectForumTopic,
                            forumRepliesState = forumRepliesState,
                            onCreateTopic = { cat, subj, title, body, resTitle, resUrl ->
                                viewModel.createForumTopic(
                                    authorName = currentUserName,
                                    examCategory = cat,
                                    subject = subj,
                                    title = title,
                                    body = body,
                                    resourceTitle = resTitle,
                                    resourceUrl = resUrl
                                )
                            },
                            onToggleTopicUpvote = viewModel::toggleForumTopicUpvote,
                            onDeleteTopic = viewModel::deleteForumTopic,
                            onCreateReply = { topic, body, resTitle, resUrl ->
                                viewModel.createForumReply(
                                    topic = topic,
                                    authorName = currentUserName,
                                    body = body,
                                    resourceTitle = resTitle,
                                    resourceUrl = resUrl
                                )
                            },
                            onToggleReplyUpvote = viewModel::toggleForumReplyUpvote,
                            onDeleteReply = viewModel::deleteForumReply,
                            onSeedStarterDiscussions = {
                                viewModel.seedStarterForumTopics(currentUserName)
                            }
                        )

                        GpscDestination.BOOKMARKS -> BookmarksScreen(
                            language = language,
                            bookmarksState = bookmarksState,
                            onUpdatePersonalNote = viewModel::updateBookmarkPersonalNote,
                            onDeleteBookmark = viewModel::deleteBookmarkById
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserProfileDialog(
    user: FirebaseUser,
    language: AppLanguage,
    bookmarksCount: Int,
    evaluationsCount: Int,
    completedTopicsCount: Int,
    totalAttemptedMcqs: Int,
    forumTopicsCount: Int,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            com.example.ui.components.SaaflyaFeatherEmblemBadge(
                size = 56.dp
            )
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = user.displayName ?: (if (language == AppLanguage.GUJARATI) "સ્પર્ધાત્મક પરીક્ષા વિદ્યાર્થી" else "Civil Services Aspirant"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (!user.email.isNullOrBlank()) {
                    Text(
                        text = user.email!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                HorizontalDivider()
                ProfileStatRow(
                    label = if (language == AppLanguage.GUJARATI) "સોલ્વ કરેલા પ્રિલિમ્સ MCQ" else "Prelims MCQs Attempted",
                    value = totalAttemptedMcqs.toString()
                )
                ProfileStatRow(
                    label = if (language == AppLanguage.GUJARATI) "સેવ કરેલા બુકમાર્ક્સ" else "Saved Revision Bookmarks",
                    value = bookmarksCount.toString()
                )
                ProfileStatRow(
                    label = if (language == AppLanguage.GUJARATI) "મેઇન્સ AI મૂલ્યાંકન" else "Mains AI Evaluations",
                    value = evaluationsCount.toString()
                )
                ProfileStatRow(
                    label = if (language == AppLanguage.GUJARATI) "પૂર્ણ કરેલા અભ્યાસક્રમ મુદ્દા" else "Syllabus Topics Completed",
                    value = "$completedTopicsCount / ${CurriculumSeedData.syllabusTopics.size}"
                )
                ProfileStatRow(
                    label = if (language == AppLanguage.GUJARATI) "શરૂ કરેલી ફોરમ ચર્ચાઓ" else "Forum Topics Created",
                    value = forumTopicsCount.toString()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    signOut(
                        context = context,
                        credentialManager = credentialManager,
                        onSignOutComplete = onDismiss,
                        scope = scope
                    )
                },
                modifier = Modifier.testTag("profile_sign_out_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (language == AppLanguage.GUJARATI) "સાઇન આઉટ" else "Sign Out")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (language == AppLanguage.GUJARATI) "બંધ કરો" else "Close")
            }
        }
    )
}

@Composable
private fun ProfileStatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}
