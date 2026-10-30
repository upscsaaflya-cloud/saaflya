package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Museum
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AppLanguage
import com.example.data.model.ExamCategory
import com.example.data.model.ExamSubject
import com.example.data.model.McqQuestion

private data class SubjectCardMeta(
    val subject: ExamSubject,
    val icon: ImageVector,
    val highlightEn: String,
    val highlightGu: String
)

private val SUBJECT_GRID_METADATA = listOf(
    SubjectCardMeta(
        subject = ExamSubject.POLITY,
        icon = Icons.Default.AccountBalance,
        highlightEn = "Constitution, Fundamental Rights & Panchayati Raj",
        highlightGu = "બંધારણ, મૂળભૂત અધિકારો અને પંચાયતી રાજ"
    ),
    SubjectCardMeta(
        subject = ExamSubject.GUJARAT_HISTORY,
        icon = Icons.Default.Museum,
        highlightEn = "Indus Sites, Solanki Dynasty & Cultural Heritage",
        highlightGu = "સિંધુ ખીણ, સોલંકી કાળ અને સાંસ્કૃતિક વારસો"
    ),
    SubjectCardMeta(
        subject = ExamSubject.GEOGRAPHY,
        icon = Icons.Default.Public,
        highlightEn = "Rivers, Climate, Physiography & Resources",
        highlightGu = "નદી તંત્ર, આબોહવા, ભૂપૃષ્ઠ અને સંસાધનો"
    ),
    SubjectCardMeta(
        subject = ExamSubject.ECONOMY,
        icon = Icons.Default.TrendingUp,
        highlightEn = "Union & Gujarat Budget, RBI Policy & Schemes",
        highlightGu = "કેન્દ્રીય અને ગુજરાત બજેટ, RBI નીતિ અને યોજનાઓ"
    ),
    SubjectCardMeta(
        subject = ExamSubject.SCIENCE,
        icon = Icons.Default.Science,
        highlightEn = "ISRO Space Missions, Biotechnology & ICT",
        highlightGu = "ISRO અવકાશ મિશન, બાયોટેકનોલોજી અને ICT"
    )
)

/**
 * Clean, clickable 2-column dashboard grid organizing all study subjects
 * (Indian Polity, Gujarat History & Culture, Geography, Indian Economy, General Science)
 * plus a Current Affairs & Syllabus quick-card and cross-module shortcuts.
 */
@Composable
fun StudyCategoryDashboardGrid(
    language: AppLanguage,
    selectedCategory: ExamCategory,
    selectedSubject: ExamSubject,
    onSelectSubject: (ExamSubject) -> Unit,
    allQuestions: List<McqQuestion>,
    selectedAnswers: Map<String, Int>,
    onNavigateToMains: () -> Unit = {},
    onNavigateToForum: () -> Unit = {},
    onNavigateToAffairs: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isGridExpanded by rememberSaveable { mutableStateOf(true) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Header with Expand/Collapse Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = if (language == AppLanguage.GUJARATI) {
                            "અભ્યાસ વિષય ડેશબોર્ડ (Study Categories)"
                        } else {
                            "Study Categories Dashboard"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (language == AppLanguage.GUJARATI) {
                            "${selectedCategory.titleGu} • વિષય પર ક્લિક કરીને પ્રશ્નોત્તરી શરૂ કરો"
                        } else {
                            "${selectedCategory.titleEn} • Tap any subject card to switch study module"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            TextButton(
                onClick = { isGridExpanded = !isGridExpanded },
                modifier = Modifier.testTag("toggle_subject_grid_button")
            ) {
                Text(
                    text = if (isGridExpanded) {
                        if (language == AppLanguage.GUJARATI) "સંક્ષિપ્ત" else "Compact"
                    } else {
                        if (language == AppLanguage.GUJARATI) "બધા વિષયો" else "Show Grid"
                    },
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (isGridExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // 2-Column Clickable Subject Grid (3 rows x 2 columns = 5 Subjects + 1 Current Affairs & Syllabus Card)
        AnimatedVisibility(visible = isGridExpanded) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("study_categories_grid")
            ) {
                val rows = SUBJECT_GRID_METADATA.chunked(2)
                rows.forEachIndexed { rowIndex, rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { meta ->
                            val subjectQuestions = allQuestions.filter {
                                it.subject == meta.subject.id &&
                                    (it.examCategory == selectedCategory.id || allQuestions.none { q ->
                                        q.subject == meta.subject.id && q.examCategory == selectedCategory.id
                                    })
                            }
                            val attemptedCount = subjectQuestions.count { selectedAnswers.containsKey(it.id) }
                            val isSelected = meta.subject == selectedSubject

                            StudySubjectGridCard(
                                meta = meta,
                                language = language,
                                isSelected = isSelected,
                                questionCount = subjectQuestions.size,
                                attemptedCount = attemptedCount,
                                onClick = { onSelectSubject(meta.subject) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("subject_chip_${meta.subject.name}")
                            )
                        }

                        // Fill the 6th slot in the final row with the Current Affairs & Syllabus Tracker shortcut card
                        if (rowIndex == rows.lastIndex && rowItems.size == 1) {
                            CurrentAffairsShortcutGridCard(
                                language = language,
                                onClick = onNavigateToAffairs,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("grid_shortcut_current_affairs")
                            )
                        }
                    }
                }
            }
        }

        // Selected Subject Cross-Module Quick Actions Bar
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (language == AppLanguage.GUJARATI) {
                                "સક્રિય વિષય: ${selectedSubject.titleGu} (${selectedSubject.code})"
                            } else {
                                "Active Subject: ${selectedSubject.titleEn} (${selectedSubject.code})"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = onNavigateToMains,
                        label = {
                            Text(
                                text = if (language == AppLanguage.GUJARATI) {
                                    "મેઇન્સ AI મૂલ્યાંકન"
                                } else {
                                    "Mains Answer AI"
                                },
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.FactCheck,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.testTag("quick_action_mains_subject")
                    )

                    AssistChip(
                        onClick = onNavigateToForum,
                        label = {
                            Text(
                                text = if (language == AppLanguage.GUJARATI) {
                                    "વિષય ચર્ચા મંચ"
                                } else {
                                    "Subject Forum"
                                },
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Forum,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.testTag("quick_action_forum_subject")
                    )
                }
            }
        }
    }
}

@Composable
private fun StudySubjectGridCard(
    meta: SubjectCardMeta,
    language: AppLanguage,
    isSelected: Boolean,
    questionCount: Int,
    attemptedCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Row: Subject Icon + GS Paper Code Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    },
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = meta.icon,
                            contentDescription = meta.subject.titleEn,
                            tint = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = meta.subject.code,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onSecondary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Subject Title (Gujarati or English)
            Text(
                text = if (language == AppLanguage.GUJARATI) meta.subject.titleGu else meta.subject.titleEn,
                style = MaterialTheme.typography.titleSmall,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Key Syllabus Focus Subtitle
            Text(
                text = if (language == AppLanguage.GUJARATI) meta.highlightGu else meta.highlightEn,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Progress & Question Count Footer
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.GUJARATI) {
                            "$questionCount પ્રશ્નો"
                        } else {
                            "$questionCount MCQs"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.secondary
                        },
                        fontWeight = FontWeight.SemiBold
                    )

                    if (attemptedCount > 0) {
                        Text(
                            text = "$attemptedCount/$questionCount",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                val progress = if (questionCount > 0) {
                    (attemptedCount.toFloat() / questionCount.toFloat()).coerceIn(0f, 1f)
                } else {
                    0f
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondary
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CurrentAffairsShortcutGridCard(
    language: AppLanguage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Newspaper,
                            contentDescription = "Current Affairs & Syllabus",
                            tint = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = if (language == AppLanguage.GUJARATI) {
                    "કરંટ અફેર્સ અને અભ્યાસક્રમ"
                } else {
                    "Current Affairs & Syllabus"
                },
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )

            Text(
                text = if (language == AppLanguage.GUJARATI) {
                    "ગુજરાત અને રાષ્ટ્રીય અપડેટ્સ તથા અભ્યાસક્રમ ટ્રેકર જુઓ"
                } else {
                    "Daily Gujarat & National updates + topic completion tracker"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = if (language == AppLanguage.GUJARATI) "અહીં ખોલો →" else "Open Tracker →",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
