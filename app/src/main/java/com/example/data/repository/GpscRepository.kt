package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.OperationType
import com.example.data.handleFirestoreError
import com.example.data.model.Bookmark
import com.example.data.model.ForumReply
import com.example.data.model.ForumTopic
import com.example.data.model.MainsEvaluation
import com.example.data.model.SyllabusProgress
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class GpscRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth get() = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- 1. BOOKMARKS ---

    fun observeBookmarks(userId: String): Flow<List<Bookmark>> {
        val path = "bookmarks"
        return db.collection(path)
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Bookmark::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun getUserBookmarks(): Result<List<Bookmark>> = runCatching {
        val uid = requireUserId()
        val path = "bookmarks"
        try {
            val snap = db.collection(path)
                .whereEqualTo("userId", uid)
                .get()
                .await()
            snap.toObjects(Bookmark::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            throw e
        }
    }

    suspend fun getBookmarkById(bookmarkId: String): Result<Bookmark?> = runCatching {
        val path = "bookmarks/$bookmarkId"
        try {
            val doc = db.collection("bookmarks").document(bookmarkId).get().await()
            doc.toObject(Bookmark::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            throw e
        }
    }

    suspend fun createBookmark(
        examCategory: String,
        subject: String,
        questionText: String,
        questionTextGu: String,
        options: List<String>,
        optionsGu: List<String>,
        correctOptionIndex: Int,
        explanation: String,
        explanationGu: String,
        referenceNote: String,
        personalNote: String? = null
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val colRef = db.collection("bookmarks")
        val docRef = colRef.document()
        val payload = mutableMapOf<String, Any>(
            "userId" to uid,
            "examCategory" to examCategory,
            "subject" to subject,
            "questionText" to questionText.take(2000),
            "questionTextGu" to questionTextGu.take(2000),
            "options" to options.take(6).map { it.take(500) },
            "optionsGu" to optionsGu.take(6).map { it.take(500) },
            "correctOptionIndex" to correctOptionIndex.coerceIn(0, (options.size - 1).coerceAtLeast(0)),
            "explanation" to explanation.take(3000),
            "explanationGu" to explanationGu.take(3000),
            "referenceNote" to referenceNote.take(1000),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (!personalNote.isNullOrBlank()) {
            payload["personalNote"] = personalNote.take(1000)
        }
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateBookmarkNote(bookmarkId: String, personalNote: String): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("bookmarks").document(bookmarkId)
        try {
            docRef.update(
                mapOf(
                    "personalNote" to personalNote.take(1000),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteBookmark(bookmarkId: String): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("bookmarks").document(bookmarkId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- 2. MAINS EVALUATIONS ---

    fun observeMainsEvaluations(userId: String): Flow<List<MainsEvaluation>> {
        val path = "mains_evaluations"
        return db.collection(path)
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(MainsEvaluation::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun createMainsEvaluation(
        examCategory: String,
        subject: String,
        questionPrompt: String,
        studentAnswer: String,
        overallScore: Int,
        introFeedback: String,
        bodyFeedback: String,
        conclusionFeedback: String,
        keyPointsCovered: List<String>,
        improvementSuggestions: List<String>,
        modelAnswerSummary: String
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("mains_evaluations").document()
        val payload = mapOf<String, Any>(
            "userId" to uid,
            "examCategory" to examCategory,
            "subject" to subject.take(100),
            "questionPrompt" to questionPrompt.take(2000),
            "studentAnswer" to studentAnswer.take(10000),
            "overallScore" to overallScore.coerceIn(0, 10),
            "introFeedback" to introFeedback.take(3000),
            "bodyFeedback" to bodyFeedback.take(3000),
            "conclusionFeedback" to conclusionFeedback.take(3000),
            "keyPointsCovered" to keyPointsCovered.take(15).map { it.take(500) },
            "improvementSuggestions" to improvementSuggestions.take(15).map { it.take(500) },
            "modelAnswerSummary" to modelAnswerSummary.take(4000),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteMainsEvaluation(evalId: String): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("mains_evaluations").document(evalId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- 3. SYLLABUS PROGRESS ---

    fun observeSyllabusProgress(userId: String): Flow<List<SyllabusProgress>> {
        val path = "syllabus_progress"
        return db.collection(path)
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(SyllabusProgress::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun upsertSyllabusProgress(
        existingProgressId: String?,
        topicKey: String,
        examCategory: String,
        subject: String,
        topicTitleEn: String,
        topicTitleGu: String,
        completed: Boolean,
        questionsAttempted: Int,
        questionsCorrect: Int
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val safeAttempted = questionsAttempted.coerceAtLeast(0)
        val safeCorrect = questionsCorrect.coerceIn(0, safeAttempted)

        if (!existingProgressId.isNullOrBlank()) {
            val docRef = db.collection("syllabus_progress").document(existingProgressId)
            try {
                docRef.update(
                    mapOf(
                        "completed" to completed,
                        "questionsAttempted" to safeAttempted,
                        "questionsCorrect" to safeCorrect,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                existingProgressId
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.UPDATE, docRef.path)
                throw e
            }
        } else {
            val docRef = db.collection("syllabus_progress").document()
            val payload = mapOf<String, Any>(
                "userId" to uid,
                "topicKey" to topicKey.take(120),
                "examCategory" to examCategory,
                "subject" to subject.take(100),
                "topicTitleEn" to topicTitleEn.take(300),
                "topicTitleGu" to topicTitleGu.take(300),
                "completed" to completed,
                "questionsAttempted" to safeAttempted,
                "questionsCorrect" to safeCorrect,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            try {
                docRef.set(payload).await()
                docRef.id
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, docRef.path)
                throw e
            }
        }
    }

    // --- 4. COMMUNITY DISCUSSION FORUM (TOPICS & REPLIES) ---

    fun observeForumTopics(examCategory: String): Flow<List<ForumTopic>> {
        val path = "forum_topics"
        return db.collection(path)
            .whereEqualTo("examCategory", examCategory)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(ForumTopic::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    fun observeForumReplies(topicId: String, examCategory: String): Flow<List<ForumReply>> {
        val path = "forum_replies"
        return db.collection(path)
            .whereEqualTo("topicId", topicId)
            .whereEqualTo("examCategory", examCategory)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(ForumReply::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun createForumTopic(
        authorName: String,
        examCategory: String,
        subject: String,
        title: String,
        body: String,
        resourceTitle: String? = null,
        resourceUrl: String? = null
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("forum_topics").document()
        val payload = mutableMapOf<String, Any>(
            "authorId" to uid,
            "authorName" to authorName.ifBlank { "Civil Services Aspirant" }.take(100),
            "examCategory" to examCategory,
            "subject" to subject.take(100),
            "title" to title.trim().take(250),
            "body" to body.trim().take(4000),
            "upvoterIds" to emptyList<String>(),
            "upvoteCount" to 0,
            "replyCount" to 0,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (!resourceTitle.isNullOrBlank()) {
            payload["resourceTitle"] = resourceTitle.trim().take(200)
        }
        if (!resourceUrl.isNullOrBlank()) {
            payload["resourceUrl"] = resourceUrl.trim().take(500)
        }
        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun toggleForumTopicUpvote(topic: ForumTopic): Result<Unit> = runCatching {
        val uid = requireUserId()
        require(topic.id.isNotBlank()) { "Topic ID must not be blank" }
        val docRef = db.collection("forum_topics").document(topic.id)
        val currentUpvoters = topic.upvoterIds
        val updatedUpvoters = if (currentUpvoters.contains(uid)) {
            currentUpvoters - uid
        } else {
            (currentUpvoters + uid).take(500)
        }
        try {
            docRef.update(
                mapOf(
                    "upvoterIds" to updatedUpvoters,
                    "upvoteCount" to updatedUpvoters.size,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteForumTopic(topicId: String): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("forum_topics").document(topicId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun createForumReply(
        topic: ForumTopic,
        authorName: String,
        body: String,
        resourceTitle: String? = null,
        resourceUrl: String? = null
    ): Result<String> = runCatching {
        val uid = requireUserId()
        require(topic.id.isNotBlank()) { "Topic ID must not be blank" }
        val replyRef = db.collection("forum_replies").document()
        val topicRef = db.collection("forum_topics").document(topic.id)

        val payload = mutableMapOf<String, Any>(
            "topicId" to topic.id,
            "authorId" to uid,
            "authorName" to authorName.ifBlank { "Civil Services Aspirant" }.take(100),
            "examCategory" to topic.examCategory,
            "body" to body.trim().take(3000),
            "upvoterIds" to emptyList<String>(),
            "upvoteCount" to 0,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (!resourceTitle.isNullOrBlank()) {
            payload["resourceTitle"] = resourceTitle.trim().take(200)
        }
        if (!resourceUrl.isNullOrBlank()) {
            payload["resourceUrl"] = resourceUrl.trim().take(500)
        }

        try {
            replyRef.set(payload).await()
            topicRef.update(
                mapOf(
                    "replyCount" to (topic.replyCount + 1).coerceAtLeast(1),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            replyRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, replyRef.path)
            throw e
        }
    }

    suspend fun toggleForumReplyUpvote(reply: ForumReply): Result<Unit> = runCatching {
        val uid = requireUserId()
        require(reply.id.isNotBlank()) { "Reply ID must not be blank" }
        val docRef = db.collection("forum_replies").document(reply.id)
        val currentUpvoters = reply.upvoterIds
        val updatedUpvoters = if (currentUpvoters.contains(uid)) {
            currentUpvoters - uid
        } else {
            (currentUpvoters + uid).take(500)
        }
        try {
            docRef.update(
                mapOf(
                    "upvoterIds" to updatedUpvoters,
                    "upvoteCount" to updatedUpvoters.size,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteForumReply(replyId: String): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("forum_replies").document(replyId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }
}
