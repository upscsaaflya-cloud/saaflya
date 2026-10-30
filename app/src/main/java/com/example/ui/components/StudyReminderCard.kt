package com.example.ui.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.CurriculumSeedData
import com.example.data.model.AppLanguage
import com.example.data.model.ExamSubject
import com.example.data.model.SyllabusProgress
import com.example.data.model.SyllabusTopicTemplate
import com.example.notification.StudyReminderConfig
import com.example.notification.StudyReminderManager

private data class StudyTimePreset(
    val id: String,
    val hour: Int,
    val minute: Int,
    val labelEn: String,
    val labelGu: String
)

private val QuickTimePresets = listOf(
    StudyTimePreset("MORNING_EARLY", 6, 30, "06:30 AM • Morning Focus", "06:30 AM • પ્રભાત અભ્યાસ"),
    StudyTimePreset("MORNING_MAIN", 9, 0, "09:00 AM • Prelims Session", "09:00 AM • સવાર સેશન"),
    StudyTimePreset("EVENING_REV", 18, 0, "06:00 PM • Evening Revision", "06:00 PM • સાંજ રિવિઝન"),
    StudyTimePreset("NIGHT_QUIZ", 21, 30, "09:30 PM • Night MCQ Drill", "09:30 PM • રાત્રિ MCQ")
)

/**
 * Interactive Daily Study Reminder Card aligned with the user's Syllabus Tracker.
 * Lets students choose their preferred reminder time (via presets or M3 TimePicker dialog),
 * filter by subject, inspect the next recommended uncompleted syllabus topic, and send an
 * immediate test notification.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyReminderCard(
    language: AppLanguage,
    syllabusProgress: List<SyllabusProgress>,
    onPracticeTopicSubject: (ExamSubject) -> Unit,
    onShowStatusMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var config by remember { mutableStateOf(StudyReminderManager.loadConfig(context)) }
    var showTimePickerDialog by rememberSaveable { mutableStateOf(false) }
    var pendingActionAfterPermission by remember { mutableStateOf<String?>(null) }

    // Keep SharedPreferences cache of completed topics synced with Firestore SyllabusProgress
    LaunchedEffect(syllabusProgress, language) {
        StudyReminderManager.updateCompletedTopicsCache(context, language, syllabusProgress)
    }

    val completedTopicKeys = remember(syllabusProgress) {
        syllabusProgress.filter { it.completed }.map { it.topicKey }.toSet()
    }

    val suggestedTopic: SyllabusTopicTemplate = remember(
        completedTopicKeys,
        config.preferredSubjectFilter,
        config.customTopicKey
    ) {
        StudyReminderManager.recommendNextSyllabusTopic(
            completedTopicKeys = completedTopicKeys,
            preferredSubjectFilter = config.preferredSubjectFilter,
            customTopicKey = config.customTopicKey
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            when (pendingActionAfterPermission) {
                "ENABLE" -> {
                    val updated = config.copy(isEnabled = true)
                    config = updated
                    StudyReminderManager.saveAndSyncReminder(context, updated, language, syllabusProgress)
                    onShowStatusMessage(
                        if (language == AppLanguage.GUJARATI) {
                            "દૈનિક અભ્યાસ રીમાઇન્ડર ${updated.formattedTime12Hr} માટે સેટ થઈ ગયું!"
                        } else {
                            "Daily study reminder scheduled for ${updated.formattedTime12Hr}!"
                        }
                    )
                }
                "TEST_NOW" -> {
                    val sent = StudyReminderManager.triggerSyllabusReminderNotification(
                        context = context,
                        overrideTopic = suggestedTopic,
                        overrideLanguage = language
                    )
                    if (sent) {
                        onShowStatusMessage(
                            if (language == AppLanguage.GUJARATI) {
                                "અભ્યાસક્રમ આધારિત નોટિફિકેશન મોકલવામાં આવ્યું!"
                            } else {
                                "Syllabus study reminder notification sent!"
                            }
                        )
                    }
                }
            }
        } else {
            onShowStatusMessage(
                if (language == AppLanguage.GUJARATI) {
                    "નોટિફિકેશન મેળવવા માટે કૃપા કરીને નોટિફિકેશન પરવાનગી આપો."
                } else {
                    "Notification permission is required to receive daily study reminders."
                }
            )
        }
        pendingActionAfterPermission = null
    }

    if (showTimePickerDialog) {
        val timePickerState = rememberTimePickerState(
            initialHour = config.hourOfDay,
            initialMinute = config.minute,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePickerDialog = false },
            title = {
                Text(
                    text = if (language == AppLanguage.GUJARATI) {
                        "દૈનિક અભ્યાસ રીમાઇન્ડર સમય પસંદ કરો"
                    } else {
                        "Select Preferred Study Reminder Time"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = config.copy(
                            hourOfDay = timePickerState.hour,
                            minute = timePickerState.minute,
                            isEnabled = true
                        )
                        showTimePickerDialog = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            !StudyReminderManager.hasNotificationPermission(context)
                        ) {
                            config = updated.copy(isEnabled = false)
                            pendingActionAfterPermission = "ENABLE"
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            config = updated
                            StudyReminderManager.saveAndSyncReminder(context, updated, language, syllabusProgress)
                            onShowStatusMessage(
                                if (language == AppLanguage.GUJARATI) {
                                    "રીમાઇન્ડર સમય ${updated.formattedTime12Hr} પર અપડેટ થયો!"
                                } else {
                                    "Daily study reminder updated to ${updated.formattedTime12Hr}!"
                                }
                            )
                        }
                    },
                    modifier = Modifier.testTag("confirm_time_picker_button")
                ) {
                    Text(if (language == AppLanguage.GUJARATI) "સમય સેવ કરો" else "Save Time")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePickerDialog = false }) {
                    Text(if (language == AppLanguage.GUJARATI) "રદ કરો" else "Cancel")
                }
            }
        )
    }

    ElevatedCard(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("study_reminder_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // 1. Header with Icon, Title, Active Time Badge, and Enable Switch
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
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (config.isEnabled) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (config.isEnabled) {
                                Icons.Default.NotificationsActive
                            } else {
                                Icons.Default.NotificationsNone
                            },
                            contentDescription = null,
                            tint = if (config.isEnabled) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (language == AppLanguage.GUJARATI) {
                                "દૈનિક અભ્યાસ રીમાઇન્ડર (Study Notification)"
                            } else {
                                "Daily Syllabus Study Reminder"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (config.isEnabled) {
                                if (language == AppLanguage.GUJARATI) {
                                    "સક્રિય • દરરોજ ${config.formattedTime12Hr} વાગ્યે અભ્યાસક્રમ મુજબ સૂચના"
                                } else {
                                    "Active • Daily reminder at ${config.formattedTime12Hr}"
                                }
                            } else {
                                if (language == AppLanguage.GUJARATI) {
                                    "તમારા પસંદગીના સમયે અભ્યાસક્રમના મુદ્દાની યાદ અપાવશે"
                                } else {
                                    "Get daily notifications with your next uncompleted syllabus topic"
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (config.isEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                Switch(
                    checked = config.isEnabled,
                    onCheckedChange = { shouldEnable ->
                        if (shouldEnable) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                !StudyReminderManager.hasNotificationPermission(context)
                            ) {
                                pendingActionAfterPermission = "ENABLE"
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                val updated = config.copy(isEnabled = true)
                                config = updated
                                StudyReminderManager.saveAndSyncReminder(
                                    context,
                                    updated,
                                    language,
                                    syllabusProgress
                                )
                                onShowStatusMessage(
                                    if (language == AppLanguage.GUJARATI) {
                                        "દૈનિક અભ્યાસ રીમાઇન્ડર (${updated.formattedTime12Hr}) શરૂ કરવામાં આવ્યું!"
                                    } else {
                                        "Daily study reminder enabled for ${updated.formattedTime12Hr}!"
                                    }
                                )
                            }
                        } else {
                            val updated = config.copy(isEnabled = false)
                            config = updated
                            StudyReminderManager.saveAndSyncReminder(
                                context,
                                updated,
                                language,
                                syllabusProgress
                            )
                            onShowStatusMessage(
                                if (language == AppLanguage.GUJARATI) {
                                    "દૈનિક અભ્યાસ રીમાઇન્ડર બંધ કરવામાં આવ્યું."
                                } else {
                                    "Daily study reminder turned off."
                                }
                            )
                        }
                    },
                    modifier = Modifier.testTag("study_reminder_switch")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Preferred Reminder Time Section (Quick Presets + Custom TimePicker Button)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (language == AppLanguage.GUJARATI) {
                            "પસંદગીનો સમય: ${config.formattedTime12Hr}"
                        } else {
                            "Preferred Time: ${config.formattedTime12Hr}"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                OutlinedButton(
                    onClick = { showTimePickerDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("open_time_picker_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AlarmOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == AppLanguage.GUJARATI) "કસ્ટમ સમય" else "Custom Time",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reminder_time_presets_row")
            ) {
                items(
                    items = QuickTimePresets,
                    key = { it.id }
                ) { preset ->
                    val isSelected = config.hourOfDay == preset.hour && config.minute == preset.minute
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val updated = config.copy(
                                hourOfDay = preset.hour,
                                minute = preset.minute,
                                isEnabled = true
                            )
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                !StudyReminderManager.hasNotificationPermission(context)
                            ) {
                                config = updated.copy(isEnabled = false)
                                pendingActionAfterPermission = "ENABLE"
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                config = updated
                                StudyReminderManager.saveAndSyncReminder(
                                    context,
                                    updated,
                                    language,
                                    syllabusProgress
                                )
                                onShowStatusMessage(
                                    if (language == AppLanguage.GUJARATI) {
                                        "અભ્યાસ રીમાઇન્ડર ${updated.formattedTime12Hr} પર સેટ થયું!"
                                    } else {
                                        "Study reminder set for ${updated.formattedTime12Hr}!"
                                    }
                                )
                            }
                        },
                        label = {
                            Text(
                                text = if (language == AppLanguage.GUJARATI) preset.labelGu else preset.labelEn,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("time_preset_${preset.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Subject Alignment Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reminder_subject_filter_row")
            ) {
                item {
                    val isAll = config.preferredSubjectFilter == "ALL"
                    FilterChip(
                        selected = isAll,
                        onClick = {
                            val updated = config.copy(preferredSubjectFilter = "ALL", customTopicKey = null)
                            config = updated
                            StudyReminderManager.saveAndSyncReminder(context, updated, language, syllabusProgress)
                        },
                        label = {
                            Text(
                                text = if (language == AppLanguage.GUJARATI) "તમામ વિષયો (Auto)" else "All Subjects (Auto)",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier.testTag("reminder_subject_ALL")
                    )
                }
                items(
                    items = ExamSubject.entries,
                    key = { it.name }
                ) { subj ->
                    val isSelected = config.preferredSubjectFilter == subj.id
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val updated = config.copy(preferredSubjectFilter = subj.id, customTopicKey = null)
                            config = updated
                            StudyReminderManager.saveAndSyncReminder(context, updated, language, syllabusProgress)
                        },
                        label = {
                            Text(
                                text = if (language == AppLanguage.GUJARATI) subj.titleGu else subj.titleEn,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier.testTag("reminder_subject_${subj.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // 4. Syllabus-Aligned Suggested Focus Topic Box
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("suggested_syllabus_focus_box")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (language == AppLanguage.GUJARATI) {
                                    "આજનો સૂચિત અભ્યાસક્રમ મુદ્દો (Next Focus Topic)"
                                } else {
                                    "Suggested Syllabus Focus Topic"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "${suggestedTopic.examCategory} • ${suggestedTopic.subject}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (language == AppLanguage.GUJARATI) suggestedTopic.titleGu else suggestedTopic.titleEn,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "📌 " + (if (language == AppLanguage.GUJARATI) suggestedTopic.subtopicsGu else suggestedTopic.subtopicsEn),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                val allTopics = CurriculumSeedData.syllabusTopics
                                if (allTopics.isNotEmpty()) {
                                    val currIdx = allTopics.indexOfFirst { it.topicKey == suggestedTopic.topicKey }
                                    val nextTopic = allTopics[(currIdx + 1) % allTopics.size]
                                    val updated = config.copy(customTopicKey = nextTopic.topicKey)
                                    config = updated
                                    StudyReminderManager.saveAndSyncReminder(
                                        context,
                                        updated,
                                        language,
                                        syllabusProgress
                                    )
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("cycle_suggested_topic_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (language == AppLanguage.GUJARATI) "બીજો મુદ્દો પસંદ કરો" else "Next Topic",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                        !StudyReminderManager.hasNotificationPermission(context)
                                    ) {
                                        pendingActionAfterPermission = "TEST_NOW"
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        val sent = StudyReminderManager.triggerSyllabusReminderNotification(
                                            context = context,
                                            overrideTopic = suggestedTopic,
                                            overrideLanguage = language
                                        )
                                        if (sent) {
                                            onShowStatusMessage(
                                                if (language == AppLanguage.GUJARATI) {
                                                    "નોટિફિકેશન મોકલવામાં આવ્યું: ${suggestedTopic.titleGu}"
                                                } else {
                                                    "Notification sent: ${suggestedTopic.titleEn}"
                                                }
                                            )
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("send_test_reminder_notification_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (language == AppLanguage.GUJARATI) "ટેસ્ટ નોટિફિકેશન" else "Test Alert",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }

                            Button(
                                onClick = {
                                    val matchingSubject = ExamSubject.entries.firstOrNull {
                                        it.id.equals(suggestedTopic.subject, ignoreCase = true)
                                    } ?: ExamSubject.POLITY
                                    onPracticeTopicSubject(matchingSubject)
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("practice_suggested_topic_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (language == AppLanguage.GUJARATI) "અભ્યાસ શરૂ કરો" else "Study Now",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
