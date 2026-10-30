package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.AppLanguage
import com.example.data.model.Bookmark
import com.example.ui.theme.CorrectGreen
import com.example.viewmodel.CloudDataState

@Composable
fun BookmarksScreen(
    language: AppLanguage,
    bookmarksState: CloudDataState<List<Bookmark>>,
    onUpdatePersonalNote: (String, String) -> Unit,
    onDeleteBookmark: (String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    var editingBookmark by remember { mutableStateOf<Bookmark?>(null) }
    var noteDraft by remember { mutableStateOf("") }

    val allBookmarks = (bookmarksState as? CloudDataState.Success)?.data.orEmpty()
    val filteredBookmarks = remember(allBookmarks, selectedFilter) {
        if (selectedFilter == "ALL") {
            allBookmarks
        } else {
            allBookmarks.filter { it.examCategory == selectedFilter }
        }
    }

    if (editingBookmark != null) {
        val target = editingBookmark!!
        AlertDialog(
            onDismissRequest = { editingBookmark = null },
            title = {
                Text(
                    text = if (language == AppLanguage.GUJARATI) "પર્સનલ રિવિઝન નોંધ ઉમેરો" else "Personal Revision Note"
                )
            },
            text = {
                OutlinedTextField(
                    value = noteDraft,
                    onValueChange = { noteDraft = it },
                    placeholder = {
                        Text(
                            if (language == AppLanguage.GUJARATI) {
                                "આ પ્રશ્ન માટે શોર્ટકટ કે યાદ રાખવાની ટ્રિક લખો..."
                            } else {
                                "Write your mnemonic, article reference, or revision tip..."
                            }
                        )
                    },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdatePersonalNote(target.id, noteDraft)
                        editingBookmark = null
                    }
                ) {
                    Text(if (language == AppLanguage.GUJARATI) "સેવ કરો" else "Save Note")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingBookmark = null }) {
                    Text(if (language == AppLanguage.GUJARATI) "રદ કરો" else "Cancel")
                }
            }
        )
    }

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
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.GUJARATI) {
                            "બુકમાર્ક કરેલા પ્રશ્નો (${filteredBookmarks.size})"
                        } else {
                            "Saved Revision Bookmarks (${filteredBookmarks.size})"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("ALL", "GPSC", "UPSC").forEach { filterKey ->
                            FilterChip(
                                selected = selectedFilter == filterKey,
                                onClick = { selectedFilter = filterKey },
                                label = { Text(filterKey) }
                            )
                        }
                    }
                }
            }

            if (filteredBookmarks.isEmpty()) {
                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmarks,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (language == AppLanguage.GUJARATI) {
                                    "હજુ સુધી કોઈ પ્રશ્ન બુકમાર્ક કર્યો નથી"
                                } else {
                                    "No Bookmarked Questions Yet"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (language == AppLanguage.GUJARATI) {
                                    "પ્રિલિમ્સ પ્રેક્ટિસ અથવા કરંટ અફેર્સ વિભાગમાંથી મહત્વના પ્રશ્નો પર બુકમાર્ક આઇકન દબાવીને અહીં રિવિઝન માટે સેવ કરો."
                                } else {
                                    "Tap the bookmark icon on any Prelims MCQ or Current Affairs question to save it here for quick revision."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(
                    items = filteredBookmarks,
                    key = { it.id }
                ) { bookmark ->
                    BookmarkRevisionCard(
                        bookmark = bookmark,
                        language = language,
                        onEditNote = {
                            noteDraft = bookmark.personalNote.orEmpty()
                            editingBookmark = bookmark
                        },
                        onDelete = { onDeleteBookmark(bookmark.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BookmarkRevisionCard(
    bookmark: Bookmark,
    language: AppLanguage,
    onEditNote: () -> Unit,
    onDelete: () -> Unit
) {
    var revealAnswer by remember { mutableStateOf(false) }
    val options = if (language == AppLanguage.GUJARATI) bookmark.optionsGu else bookmark.options
    val questionText = if (language == AppLanguage.GUJARATI) bookmark.questionTextGu else bookmark.questionText
    val explanationText = if (language == AppLanguage.GUJARATI) bookmark.explanationGu else bookmark.explanation

    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${bookmark.examCategory} • ${bookmark.subject}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Row {
                    IconButton(onClick = onEditNote) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Edit Personal Note",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_bookmark_${bookmark.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkRemove,
                            contentDescription = "Remove Bookmark",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = questionText,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            val labels = listOf("A", "B", "C", "D", "E", "F")
            options.forEachIndexed { idx, opt ->
                val isCorrect = idx == bookmark.correctOptionIndex
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (revealAnswer && isCorrect) {
                        CorrectGreen.copy(alpha = 0.16f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                ) {
                    Text(
                        text = "${labels.getOrElse(idx) { "${idx + 1}" }}. $opt",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (revealAnswer && isCorrect) FontWeight.Bold else FontWeight.Normal,
                        color = if (revealAnswer && isCorrect) CorrectGreen else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            if (!bookmark.personalNote.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "📝 " + bookmark.personalNote,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = { revealAnswer = !revealAnswer },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (revealAnswer) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (revealAnswer) {
                        if (language == AppLanguage.GUJARATI) "જવાબ અને સમજૂતી છુપાવો" else "Hide Answer & Explanation"
                    } else {
                        if (language == AppLanguage.GUJARATI) "સાચો જવાબ અને સમજૂતી જુઓ" else "Reveal Answer & Explanation"
                    }
                )
            }

            AnimatedVisibility(visible = revealAnswer) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = explanationText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "📚 ${bookmark.referenceNote}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}
