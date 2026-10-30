package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AppLanguage
import com.example.data.model.ExamCategory
import com.example.data.model.ExamSubject
import com.example.data.model.ForumReply
import com.example.data.model.ForumTopic
import com.example.viewmodel.CloudDataState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CommunityForumScreen(
    currentUserId: String,
    currentUserName: String,
    language: AppLanguage,
    selectedCategory: ExamCategory,
    onSelectCategory: (ExamCategory) -> Unit,
    selectedSubject: ExamSubject,
    onSelectSubject: (ExamSubject) -> Unit,
    forumTopicsState: CloudDataState<List<ForumTopic>>,
    selectedTopicId: String?,
    onSelectTopicId: (String?) -> Unit,
    forumRepliesState: CloudDataState<List<ForumReply>>,
    onCreateTopic: (ExamCategory, ExamSubject, String, String, String?, String?) -> Unit,
    onToggleTopicUpvote: (ForumTopic) -> Unit,
    onDeleteTopic: (String) -> Unit,
    onCreateReply: (ForumTopic, String, String?, String?) -> Unit,
    onToggleReplyUpvote: (ForumReply) -> Unit,
    onDeleteReply: (String) -> Unit,
    onSeedStarterDiscussions: () -> Unit
) {
    var filterAllSubjects by rememberSaveable { mutableStateOf(true) }
    var filterResourcesOnly by rememberSaveable { mutableStateOf(false) }
    var showCreateTopicDialog by rememberSaveable { mutableStateOf(false) }

    val allTopics = (forumTopicsState as? CloudDataState.Success)?.data.orEmpty()
    val activeTopic = remember(allTopics, selectedTopicId) {
        allTopics.firstOrNull { it.id == selectedTopicId }
    }

    if (activeTopic != null) {
        BackHandler {
            onSelectTopicId(null)
        }
        val replies = (forumRepliesState as? CloudDataState.Success)?.data.orEmpty()
        ForumTopicDetailView(
            currentUserId = currentUserId,
            language = language,
            topic = activeTopic,
            replies = replies,
            onBack = { onSelectTopicId(null) },
            onToggleTopicUpvote = { onToggleTopicUpvote(activeTopic) },
            onDeleteTopic = {
                onDeleteTopic(activeTopic.id)
                onSelectTopicId(null)
            },
            onPostReply = { body, resTitle, resUrl ->
                onCreateReply(activeTopic, body, resTitle, resUrl)
            },
            onToggleReplyUpvote = onToggleReplyUpvote,
            onDeleteReply = onDeleteReply
        )
        return
    }

    val filteredTopics = remember(allTopics, filterAllSubjects, selectedSubject, filterResourcesOnly) {
        allTopics.filter { topic ->
            val subjectMatch = filterAllSubjects || topic.subject == selectedSubject.id
            val resourceMatch = !filterResourcesOnly || !topic.resourceTitle.isNullOrBlank() || !topic.resourceUrl.isNullOrBlank()
            subjectMatch && resourceMatch
        }
    }

    if (showCreateTopicDialog) {
        CreateForumTopicDialog(
            language = language,
            initialCategory = selectedCategory,
            initialSubject = selectedSubject,
            onDismiss = { showCreateTopicDialog = false },
            onSubmit = { cat, subj, title, body, resTitle, resUrl ->
                onCreateTopic(cat, subj, title, body, resTitle, resUrl)
                showCreateTopicDialog = false
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateTopicDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("create_forum_topic_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Create New Topic")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == AppLanguage.GUJARATI) "નવી ચર્ચા +" else "New Topic",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 14.dp,
                    bottom = 88.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Forum Header & Exam Level Selector (GPSC Class 1-2 vs UPSC)
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Forum,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (language == AppLanguage.GUJARATI) {
                                            "સમુદાય ચર્ચા મંચ અને અભ્યાસ સાહિત્ય (Community Forum)"
                                        } else {
                                            "Community Discussion & Study Resource Forum"
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (language == AppLanguage.GUJARATI) {
                                    "GPSC અને UPSC ના વિદ્યાર્થીઓ સાથે વિષયવાર પ્રશ્નો ચર્ચો, અભ્યાસ સાહિત્ય/નોટ્સ શેર કરો અને ઉપયોગી જવાબોને અપવોટ કરો."
                                } else {
                                    "Collaborate with fellow GPSC & UPSC aspirants by subject: discuss exam strategies, share study notes/resources, and upvote helpful answers."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Exam Level Selector (GPSC / UPSC)
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
                                            .testTag("forum_category_${category.id}"),
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
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                        ) {
                                            Text(
                                                text = if (language == AppLanguage.GUJARATI) category.titleGu else category.titleEn,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = if (isSelected) {
                                                    MaterialTheme.colorScheme.onPrimary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                },
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = if (language == AppLanguage.GUJARATI) "ચર્ચા મંચ" else "Discussion Board",
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

                // 2. Subject Filter Row + Shared Resources Filter
                item {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = filterAllSubjects,
                                onClick = { filterAllSubjects = true },
                                label = {
                                    Text(if (language == AppLanguage.GUJARATI) "બધા વિષયો (All)" else "All Subjects")
                                },
                                modifier = Modifier.testTag("forum_subject_all")
                            )

                            FilterChip(
                                selected = filterResourcesOnly,
                                onClick = { filterResourcesOnly = !filterResourcesOnly },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        if (language == AppLanguage.GUJARATI) "અભ્યાસ સાહિત્ય (Study Resources)" else "With Study Resources"
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                                ),
                                modifier = Modifier.testTag("forum_filter_resources")
                            )

                            ExamSubject.entries.forEach { subject ->
                                val isSubjSelected = !filterAllSubjects && selectedSubject == subject
                                FilterChip(
                                    selected = isSubjSelected,
                                    onClick = {
                                        filterAllSubjects = false
                                        onSelectSubject(subject)
                                    },
                                    label = {
                                        Text(
                                            if (language == AppLanguage.GUJARATI) subject.titleGu else subject.titleEn
                                        )
                                    },
                                    modifier = Modifier.testTag("forum_subject_${subject.name}")
                                )
                            }
                        }
                    }
                }

                // 3. Topic List or Empty State with Starter Discussions Seeder
                if (filteredTopics.isEmpty()) {
                    item {
                        ElevatedCard(
                            shape = RoundedCornerShape(22.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Forum,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (language == AppLanguage.GUJARATI) {
                                        "${selectedCategory.titleGu} માટે પ્રથમ ચર્ચા શરૂ કરો!"
                                    } else {
                                        "Start the First Discussion for ${selectedCategory.titleEn}"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (language == AppLanguage.GUJARATI) {
                                        "તમે નવો વિષય બનાવી શકો છો અથવા નીચેના બટનથી અભ્યાસક્રમ આધારિત પ્રારંભિક ચર્ચાઓ અને નોટ્સ લોડ કરી શકો છો."
                                    } else {
                                        "Create a new discussion topic with study notes, or load starter aspirant discussions for ${selectedCategory.titleEn}."
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = onSeedStarterDiscussions,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("seed_forum_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            if (language == AppLanguage.GUJARATI) "નમૂનાની ચર્ચાઓ લોડ કરો" else "Load Starter Topics"
                                        )
                                    }
                                    Button(
                                        onClick = { showCreateTopicDialog = true },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("empty_state_new_topic_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            if (language == AppLanguage.GUJARATI) "નવો વિષય બનાવો" else "Create Topic"
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    items(
                        items = filteredTopics,
                        key = { it.id }
                    ) { topic ->
                        ForumTopicCard(
                            currentUserId = currentUserId,
                            language = language,
                            topic = topic,
                            onOpenTopic = { onSelectTopicId(topic.id) },
                            onToggleUpvote = { onToggleTopicUpvote(topic) },
                            onDelete = if (topic.authorId == currentUserId) {
                                { onDeleteTopic(topic.id) }
                            } else null
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ForumTopicCard(
    currentUserId: String,
    language: AppLanguage,
    topic: ForumTopic,
    onOpenTopic: () -> Unit,
    onToggleUpvote: () -> Unit,
    onDelete: (() -> Unit)?
) {
    val isUpvoted = topic.upvoterIds.contains(currentUserId)
    val uriHandler = LocalUriHandler.current

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenTopic() }
            .testTag("forum_topic_card_${topic.id}"),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Top badges: Exam Category + Subject + Author
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${topic.examCategory} • ${topic.subject}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                if (onDelete != null) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Topic",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = topic.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = topic.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            // Shared Study Resource Attachment Box
            if (!topic.resourceTitle.isNullOrBlank() || !topic.resourceUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                SharedResourceBadge(
                    resourceTitle = topic.resourceTitle,
                    resourceUrl = topic.resourceUrl,
                    language = language,
                    onClickUrl = { url ->
                        if (url.startsWith("http://") || url.startsWith("https://")) {
                            runCatching { uriHandler.openUri(url) }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(10.dp))

            // Footer: Author Info, Upvote Button, Replies Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (topic.authorName.firstOrNull() ?: 'A').uppercaseChar().toString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = topic.authorName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Upvote Pill
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onToggleUpvote() }
                            .testTag("upvote_topic_${topic.id}"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isUpvoted) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isUpvoted) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                                contentDescription = "Upvote Topic",
                                tint = if (isUpvoted) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = topic.upvoteCount.toString(),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isUpvoted) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }

                    // Replies Count Pill
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onOpenTopic() }
                            .testTag("open_replies_${topic.id}"),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = "Replies",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (language == AppLanguage.GUJARATI) {
                                    "${topic.replyCount} જવાબો"
                                } else {
                                    "${topic.replyCount} Replies"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ForumTopicDetailView(
    currentUserId: String,
    language: AppLanguage,
    topic: ForumTopic,
    replies: List<ForumReply>,
    onBack: () -> Unit,
    onToggleTopicUpvote: () -> Unit,
    onDeleteTopic: () -> Unit,
    onPostReply: (String, String?, String?) -> Unit,
    onToggleReplyUpvote: (ForumReply) -> Unit,
    onDeleteReply: (String) -> Unit
) {
    var replyBody by rememberSaveable { mutableStateOf("") }
    var showAttachResourceFields by rememberSaveable { mutableStateOf(false) }
    var replyResourceTitle by rememberSaveable { mutableStateOf("") }
    var replyResourceUrl by rememberSaveable { mutableStateOf("") }
    val uriHandler = LocalUriHandler.current
    val isTopicUpvoted = topic.upvoterIds.contains(currentUserId)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp)
        ) {
            // Top bar for thread navigation
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("forum_detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Forum"
                        )
                    }
                    Text(
                        text = if (language == AppLanguage.GUJARATI) {
                            "${topic.examCategory} • ${topic.subject} ચર્ચા"
                        } else {
                            "${topic.examCategory} • ${topic.subject} Discussion"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (topic.authorId == currentUserId) {
                        IconButton(onClick = onDeleteTopic) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete Topic",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Thread Content + Replies
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Full Parent Topic Card
                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = (topic.authorName.firstOrNull() ?: 'A').uppercaseChar().toString(),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = topic.authorName,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = formatTimestamp(topic.createdAt?.toDate()),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                OutlinedButton(
                                    onClick = onToggleTopicUpvote,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("detail_upvote_topic_button")
                                ) {
                                    Icon(
                                        imageVector = if (isTopicUpvoted) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                                        contentDescription = "Upvote",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("${topic.upvoteCount}")
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = topic.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = topic.body,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (!topic.resourceTitle.isNullOrBlank() || !topic.resourceUrl.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                SharedResourceBadge(
                                    resourceTitle = topic.resourceTitle,
                                    resourceUrl = topic.resourceUrl,
                                    language = language,
                                    onClickUrl = { url ->
                                        if (url.startsWith("http://") || url.startsWith("https://")) {
                                            runCatching { uriHandler.openUri(url) }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Replies Header
                item {
                    Text(
                        text = if (language == AppLanguage.GUJARATI) {
                            "વિદ્યાર્થીઓના જવાબો અને અભ્યાસ નોંધ (${replies.size})"
                        } else {
                            "Community Replies & Study Notes (${replies.size})"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (replies.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (language == AppLanguage.GUJARATI) {
                                    "હજુ સુધી કોઈ જવાબ નથી. નીચે તમારો પ્રતિભાવ કે અભ્યાસ સંદર્ભ શેર કરો!"
                                } else {
                                    "No replies yet. Be the first to share a helpful explanation or study resource below!"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(18.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(
                        items = replies,
                        key = { it.id }
                    ) { reply ->
                        val isHelpfulLeader = reply.upvoteCount > 0 && reply.id == replies.first().id
                        ForumReplyCard(
                            currentUserId = currentUserId,
                            language = language,
                            reply = reply,
                            isMostHelpful = isHelpfulLeader,
                            onToggleUpvote = { onToggleReplyUpvote(reply) },
                            onDelete = if (reply.authorId == currentUserId) {
                                { onDeleteReply(reply.id) }
                            } else null
                        )
                    }
                }
            }

            // Bottom Reply & Resource Composer
            Surface(
                tonalElevation = 4.dp,
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    if (showAttachResourceFields) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = replyResourceTitle,
                                onValueChange = { replyResourceTitle = it },
                                placeholder = {
                                    Text(
                                        if (language == AppLanguage.GUJARATI) "સંદર્ભ/નોટ્સનું નામ" else "Resource Title (e.g. GCERT Ch.4)"
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("reply_resource_title_input")
                            )
                            OutlinedTextField(
                                value = replyResourceUrl,
                                onValueChange = { replyResourceUrl = it },
                                placeholder = {
                                    Text(
                                        if (language == AppLanguage.GUJARATI) "લિંક અથવા પુસ્તક સંદર્ભ" else "URL or Book Citation"
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("reply_resource_url_input")
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { showAttachResourceFields = !showAttachResourceFields },
                            modifier = Modifier.testTag("toggle_reply_resource_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = "Attach Study Resource",
                                tint = if (showAttachResourceFields) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }

                        OutlinedTextField(
                            value = replyBody,
                            onValueChange = { replyBody = it },
                            placeholder = {
                                Text(
                                    if (language == AppLanguage.GUJARATI) {
                                        "તમારો જવાબ અથવા અભ્યાસ ટિપ્સ લખો..."
                                    } else {
                                        "Write a helpful reply or share study tips..."
                                    }
                                )
                            },
                            maxLines = 3,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reply_body_input")
                        )

                        Button(
                            onClick = {
                                if (replyBody.isNotBlank()) {
                                    onPostReply(
                                        replyBody,
                                        replyResourceTitle.takeIf { it.isNotBlank() },
                                        replyResourceUrl.takeIf { it.isNotBlank() }
                                    )
                                    replyBody = ""
                                    replyResourceTitle = ""
                                    replyResourceUrl = ""
                                    showAttachResourceFields = false
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("send_reply_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Post Reply",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ForumReplyCard(
    currentUserId: String,
    language: AppLanguage,
    reply: ForumReply,
    isMostHelpful: Boolean,
    onToggleUpvote: () -> Unit,
    onDelete: (() -> Unit)?
) {
    val isUpvoted = reply.upvoterIds.contains(currentUserId)
    val uriHandler = LocalUriHandler.current

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isMostHelpful) {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        border = if (isMostHelpful) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
        } else null,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("forum_reply_card_${reply.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = reply.authorName,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isMostHelpful) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondary
                        ) {
                            Text(
                                text = if (language == AppLanguage.GUJARATI) "★ સૌથી ઉપયોગી" else "★ Top Helpful",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onToggleUpvote() }
                            .testTag("upvote_reply_${reply.id}"),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isUpvoted) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isUpvoted) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                                contentDescription = "Upvote Helpful Reply",
                                tint = if (isUpvoted) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (language == AppLanguage.GUJARATI) {
                                    "ઉપયોગી (${reply.upvoteCount})"
                                } else {
                                    "Helpful (${reply.upvoteCount})"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isUpvoted) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }

                    if (onDelete != null) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete Reply",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = reply.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (!reply.resourceTitle.isNullOrBlank() || !reply.resourceUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                SharedResourceBadge(
                    resourceTitle = reply.resourceTitle,
                    resourceUrl = reply.resourceUrl,
                    language = language,
                    onClickUrl = { url ->
                        if (url.startsWith("http://") || url.startsWith("https://")) {
                            runCatching { uriHandler.openUri(url) }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SharedResourceBadge(
    resourceTitle: String?,
    resourceUrl: String?,
    language: AppLanguage,
    onClickUrl: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = !resourceUrl.isNullOrBlank()) {
                if (!resourceUrl.isNullOrBlank()) {
                    onClickUrl(resourceUrl)
                }
            },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = resourceTitle?.takeIf { it.isNotBlank() }
                        ?: (if (language == AppLanguage.GUJARATI) "શેર કરેલ અભ્યાસ સાહિત્ય" else "Shared Study Resource"),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    fontWeight = FontWeight.Bold
                )
                if (!resourceUrl.isNullOrBlank()) {
                    Text(
                        text = resourceUrl,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (!resourceUrl.isNullOrBlank()) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = "Open Resource",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun CreateForumTopicDialog(
    language: AppLanguage,
    initialCategory: ExamCategory,
    initialSubject: ExamSubject,
    onDismiss: () -> Unit,
    onSubmit: (ExamCategory, ExamSubject, String, String, String?, String?) -> Unit
) {
    var category by remember { mutableStateOf(initialCategory) }
    var subject by remember { mutableStateOf(initialSubject) }
    var title by rememberSaveable { mutableStateOf("") }
    var body by rememberSaveable { mutableStateOf("") }
    var resourceTitle by rememberSaveable { mutableStateOf("") }
    var resourceUrl by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (language == AppLanguage.GUJARATI) {
                    "નવો ચર્ચા વિષય અને અભ્યાસ સાહિત્ય શેર કરો"
                } else {
                    "Create Discussion Topic & Share Resource"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Exam Category Selector
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExamCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = {
                                Text(if (language == AppLanguage.GUJARATI) cat.titleGu else cat.titleEn)
                            }
                        )
                    }
                }

                // Subject Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExamSubject.entries.forEach { subj ->
                        FilterChip(
                            selected = subject == subj,
                            onClick = { subject = subj },
                            label = {
                                Text(if (language == AppLanguage.GUJARATI) subj.titleGu else subj.titleEn)
                            }
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = {
                        Text(if (language == AppLanguage.GUJARATI) "ચર્ચાનું શીર્ષક (Topic Title)" else "Topic Title")
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_topic_title_input")
                )

                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = {
                        Text(
                            if (language == AppLanguage.GUJARATI) {
                                "પ્રશ્ન અથવા ચર્ચાની વિગત (Discussion Details)"
                            } else {
                                "Discussion Question or Study Notes"
                            }
                        )
                    },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_topic_body_input")
                )

                Text(
                    text = if (language == AppLanguage.GUJARATI) {
                        "અભ્યાસ સાહિત્ય / સંદર્ભ શેર કરો (વૈકલ્પિક):"
                    } else {
                        "Share Study Resource / Reference (Optional):"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = resourceTitle,
                    onValueChange = { resourceTitle = it },
                    label = {
                        Text(
                            if (language == AppLanguage.GUJARATI) {
                                "સાહિત્યનું નામ (દા.ત. GCERT ધોરણ ૧૧ ભૂગોળ નોટ્સ)"
                            } else {
                                "Resource Title (e.g., Laxmikanth Ch. 11 Summary)"
                            }
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_resource_title_input")
                )

                OutlinedTextField(
                    value = resourceUrl,
                    onValueChange = { resourceUrl = it },
                    label = {
                        Text(
                            if (language == AppLanguage.GUJARATI) {
                                "લિંક અથવા પુસ્તક સંદર્ભ (URL / Book Reference)"
                            } else {
                                "Resource Link or Book Citation"
                            }
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_resource_url_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(
                        category,
                        subject,
                        title,
                        body,
                        resourceTitle.takeIf { it.isNotBlank() },
                        resourceUrl.takeIf { it.isNotBlank() }
                    )
                },
                enabled = title.isNotBlank() && body.isNotBlank(),
                modifier = Modifier.testTag("dialog_publish_topic_button")
            ) {
                Text(if (language == AppLanguage.GUJARATI) "પ્રકાશિત કરો" else "Publish Topic")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (language == AppLanguage.GUJARATI) "રદ કરો" else "Cancel")
            }
        }
    )
}

private fun formatTimestamp(date: Date?): String {
    if (date == null) return "Just now"
    return SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(date)
}
