package com.example.notification

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.CurriculumSeedData
import com.example.data.model.AppLanguage
import com.example.data.model.SyllabusProgress
import com.example.data.model.SyllabusTopicTemplate
import java.util.Calendar
import java.util.Locale

data class StudyReminderConfig(
    val isEnabled: Boolean = false,
    val hourOfDay: Int = 7,
    val minute: Int = 0,
    val preferredSubjectFilter: String = "ALL",
    val customTopicKey: String? = null
) {
    val formattedTime12Hr: String
        get() {
            val amPm = if (hourOfDay < 12) "AM" else "PM"
            val hour12 = when {
                hourOfDay == 0 -> 12
                hourOfDay > 12 -> hourOfDay - 12
                else -> hourOfDay
            }
            return String.format(Locale.US, "%02d:%02d %s", hour12, minute, amPm)
        }

    val formattedTime24Hr: String
        get() = String.format(Locale.US, "%02d:%02d", hourOfDay, minute)
}

object StudyReminderManager {
    const val CHANNEL_ID = "gpscai_daily_study_reminders"
    const val ACTION_DAILY_STUDY_REMINDER = "com.example.action.DAILY_STUDY_REMINDER"
    const val EXTRA_NAVIGATE_TO_SYLLABUS = "extra_navigate_to_syllabus"
    const val EXTRA_SUGGESTED_TOPIC_KEY = "extra_suggested_topic_key"

    private const val PREFS_NAME = "gpscai_study_reminders_prefs"
    private const val KEY_ENABLED = "reminder_enabled"
    private const val KEY_HOUR = "reminder_hour"
    private const val KEY_MINUTE = "reminder_minute"
    private const val KEY_SUBJECT_FILTER = "reminder_subject_filter"
    private const val KEY_CUSTOM_TOPIC_KEY = "reminder_custom_topic_key"
    private const val KEY_LANG_CODE = "reminder_lang_code"
    private const val KEY_COMPLETED_TOPICS_CSV = "reminder_completed_topics_csv"

    private const val REMINDER_REQUEST_CODE = 4102
    private const val NOTIFICATION_ID_DAILY = 7101
    private const val TAG = "StudyReminderManager"

    fun ensureNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return
            val existing = manager.getNotificationChannel(CHANNEL_ID)
            if (existing == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "GPSCAI Daily Syllabus Study Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Daily study reminders aligned with your GPSC & UPSC Syllabus Tracker"
                    enableVibration(true)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun loadConfig(context: Context): StudyReminderConfig {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return StudyReminderConfig(
            isEnabled = prefs.getBoolean(KEY_ENABLED, false),
            hourOfDay = prefs.getInt(KEY_HOUR, 7).coerceIn(0, 23),
            minute = prefs.getInt(KEY_MINUTE, 0).coerceIn(0, 59),
            preferredSubjectFilter = prefs.getString(KEY_SUBJECT_FILTER, "ALL") ?: "ALL",
            customTopicKey = prefs.getString(KEY_CUSTOM_TOPIC_KEY, null)?.takeIf { it.isNotBlank() }
        )
    }

    fun saveAndSyncReminder(
        context: Context,
        config: StudyReminderConfig,
        language: AppLanguage,
        syllabusProgress: List<SyllabusProgress>
    ) {
        val completedKeys = syllabusProgress.filter { it.completed }.map { it.topicKey }.toSet()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_ENABLED, config.isEnabled)
            .putInt(KEY_HOUR, config.hourOfDay.coerceIn(0, 23))
            .putInt(KEY_MINUTE, config.minute.coerceIn(0, 59))
            .putString(KEY_SUBJECT_FILTER, config.preferredSubjectFilter)
            .putString(KEY_CUSTOM_TOPIC_KEY, config.customTopicKey ?: "")
            .putString(KEY_LANG_CODE, language.code)
            .putString(KEY_COMPLETED_TOPICS_CSV, completedKeys.joinToString(","))
            .apply()

        ensureNotificationChannel(context)
        if (config.isEnabled) {
            scheduleDailyAlarm(context, config.hourOfDay, config.minute)
        } else {
            cancelDailyAlarm(context)
        }
    }

    fun updateCompletedTopicsCache(
        context: Context,
        language: AppLanguage,
        syllabusProgress: List<SyllabusProgress>
    ) {
        val completedKeys = syllabusProgress.filter { it.completed }.map { it.topicKey }.toSet()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_LANG_CODE, language.code)
            .putString(KEY_COMPLETED_TOPICS_CSV, completedKeys.joinToString(","))
            .apply()
    }

    /**
     * Selects the most relevant syllabus topic for the student to focus on next:
     * 1. If the user explicitly pinned a custom topic key that exists, use it (unless already completed, in which case advance to next uncompleted).
     * 2. Otherwise, pick the first uncompleted syllabus topic matching [preferredSubjectFilter] (or across all subjects if "ALL").
     * 3. If all matching topics are completed, pick a daily rotating revision topic based on day-of-year.
     */
    fun recommendNextSyllabusTopic(
        completedTopicKeys: Set<String>,
        preferredSubjectFilter: String = "ALL",
        customTopicKey: String? = null
    ): SyllabusTopicTemplate {
        val allTopics = CurriculumSeedData.syllabusTopics
        if (allTopics.isEmpty()) {
            return SyllabusTopicTemplate(
                topicKey = "POLITY_CONST_FRAMEWORK",
                examCategory = "GPSC",
                subject = "Indian Polity",
                titleEn = "Preamble, Fundamental Rights, DPSP & Fundamental Duties",
                titleGu = "આમુખ, મૂળભૂત અધિકારો, રાજનીતિના માર્ગદર્શક સિદ્ધાંતો અને મૂળભૂત ફરજો",
                subtopicsEn = "Articles 12–51A, Writ Jurisdiction & Constitutional Doctrines",
                subtopicsGu = "અનુચ્છેદ ૧૨ થી ૫૧A, રીટ સત્તાક્ષેત્ર અને બંધારણીય સુધારા"
            )
        }

        if (!customTopicKey.isNullOrBlank()) {
            val explicitMatch = allTopics.firstOrNull {
                it.topicKey == customTopicKey && !completedTopicKeys.contains(it.topicKey)
            } ?: allTopics.firstOrNull { it.topicKey == customTopicKey }
            if (explicitMatch != null) {
                return explicitMatch
            }
        }

        val subjectFiltered = if (preferredSubjectFilter == "ALL") {
            allTopics
        } else {
            allTopics.filter { it.subject.equals(preferredSubjectFilter, ignoreCase = true) }
                .ifEmpty { allTopics }
        }

        val nextUncompleted = subjectFiltered.firstOrNull { !completedTopicKeys.contains(it.topicKey) }
            ?: allTopics.firstOrNull { !completedTopicKeys.contains(it.topicKey) }

        if (nextUncompleted != null) {
            return nextUncompleted
        }

        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return subjectFiltered[dayOfYear % subjectFiltered.size]
    }

    fun scheduleDailyAlarm(context: Context, hourOfDay: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, StudyReminderReceiver::class.java).apply {
            action = ACTION_DAILY_STUDY_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay.coerceIn(0, 23))
            set(Calendar.MINUTE, minute.coerceIn(0, 59))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        runCatching {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                target.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
            Log.i(TAG, "Scheduled daily syllabus study reminder for ${target.time}")
        }.onFailure { e ->
            Log.e(TAG, "Failed to schedule daily study reminder alarm", e)
        }
    }

    fun cancelDailyAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, StudyReminderReceiver::class.java).apply {
            action = ACTION_DAILY_STUDY_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        runCatching {
            alarmManager.cancel(pendingIntent)
        }
    }

    /**
     * Builds and dispatches the local notification suggesting the specific syllabus topic
     * and subtopics to focus on today. Returns true if the notification was posted.
     */
    fun triggerSyllabusReminderNotification(
        context: Context,
        overrideTopic: SyllabusTopicTemplate? = null,
        overrideLanguage: AppLanguage? = null
    ): Boolean {
        ensureNotificationChannel(context)
        if (!hasNotificationPermission(context)) {
            Log.w(TAG, "Cannot post notification: POST_NOTIFICATIONS permission not granted")
            return false
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val langCode = overrideLanguage?.code ?: prefs.getString(KEY_LANG_CODE, "GU") ?: "GU"
        val isGujarati = langCode == "GU"
        val subjectFilter = prefs.getString(KEY_SUBJECT_FILTER, "ALL") ?: "ALL"
        val customTopicKey = prefs.getString(KEY_CUSTOM_TOPIC_KEY, null)?.takeIf { it.isNotBlank() }
        val completedCsv = prefs.getString(KEY_COMPLETED_TOPICS_CSV, "").orEmpty()
        val completedKeys = completedCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()

        val topic = overrideTopic ?: recommendNextSyllabusTopic(
            completedTopicKeys = completedKeys,
            preferredSubjectFilter = subjectFilter,
            customTopicKey = customTopicKey
        )

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NAVIGATE_TO_SYLLABUS, true)
            putExtra(EXTRA_SUGGESTED_TOPIC_KEY, topic.topicKey)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            REMINDER_REQUEST_CODE + 1,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isGujarati) {
            "📚 આજનો અભ્યાસ લક્ષ્યાંક (${topic.examCategory} • ${topic.subject})"
        } else {
            "📚 Daily Study Focus (${topic.examCategory} • ${topic.subject})"
        }

        val shortBody = if (isGujarati) {
            "આજે અભ્યાસ કરો: ${topic.titleGu}"
        } else {
            "Focus Topic: ${topic.titleEn}"
        }

        val expandedBody = if (isGujarati) {
            "🎯 વિષય: ${topic.titleGu}\n📌 પેટા-મુદ્દા: ${topic.subtopicsGu}\n\nસાફલ્ય AI GPSC ખોલો અને આ મુદ્દાના પ્રિલિમ્સ MCQ સોલ્વ કરો!"
        } else {
            "🎯 Topic: ${topic.titleEn}\n📌 Key Subtopics: ${topic.subtopicsEn}\n\nTap to open GPSCAI and practice syllabus MCQs!"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(shortBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedBody))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .build()

        return runCatching {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_DAILY, notification)
            true
        }.getOrElse { e ->
            Log.e(TAG, "Failed to post study reminder notification", e)
            false
        }
    }
}

class StudyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val config = StudyReminderManager.loadConfig(context)
        when (action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                if (config.isEnabled) {
                    StudyReminderManager.scheduleDailyAlarm(context, config.hourOfDay, config.minute)
                }
            }
            StudyReminderManager.ACTION_DAILY_STUDY_REMINDER -> {
                StudyReminderManager.triggerSyllabusReminderNotification(context)
            }
        }
    }
}
