package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class GpscRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createAndObserveBookmark_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = GpscRepository(firestore)

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createBookmark(
                examCategory = "GPSC",
                subject = "Gujarat History & Culture",
                questionText = "Who built the Sun Temple at Modhera?",
                questionTextGu = "મોઢેરાનું સૂર્યમંદિર કોણે બંધાવ્યું હતું?",
                options = listOf("Bhimdev I", "Siddharaj Jaisinh", "Karandev", "Kumarpal"),
                optionsGu = listOf("ભીમદેવ પ્રથમ", "સિદ્ધરાજ જયસિંહ", "કર્ણદેવ", "કુમારપાળ"),
                correctOptionIndex = 0,
                explanation = "King Bhimdev I of the Solanki dynasty built the Modhera Sun Temple in 1026-27 CE.",
                explanationGu = "સોલંકી વંશના રાજા ભીમદેવ પ્રથમે ઈ.સ. ૧૦૨૬-૨૭માં મોઢેરાનું સૂર્યમંદિર બંધાવ્યું હતું.",
                referenceNote = "GPSC Prelims GS-1 Gujarat History",
                personalNote = "Revise Solanki architecture"
            )
        }
        assertTrue("Expected bookmark creation to succeed", createResult.isSuccess)
        val bookmarkId = createResult.getOrThrow()
        assertTrue(bookmarkId.isNotEmpty())

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeBookmarks(uid).first { list -> list.any { it.id == bookmarkId } }
        }
        assertTrue(emitted.any { it.id == bookmarkId })
    }

    @Test
    fun getBookmarkById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val aliceRepo = GpscRepository(firestore)
        val bookmarkId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createBookmark(
                examCategory = "UPSC",
                subject = "Indian Polity",
                questionText = "Which Article deals with Constitutional Remedies?",
                questionTextGu = "બંધારણીય ઈલાજોનો અધિકાર કયા અનુચ્છેદમાં છે?",
                options = listOf("Article 19", "Article 21", "Article 32", "Article 44"),
                optionsGu = listOf("અનુચ્છેદ ૧૯", "અનુચ્છેદ ૨૧", "અનુચ્છેદ ૩૨", "અનુચ્છેદ ૪૪"),
                correctOptionIndex = 2,
                explanation = "Article 32 provides the right to move the Supreme Court.",
                explanationGu = "અનુચ્છેદ ૩૨ સુપ્રીમ કોર્ટમાં જવાની સત્તા આપે છે.",
                referenceNote = "Indian Polity by M. Laxmikanth"
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = GpscRepository(firestore)
        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getBookmarkById(bookmarkId)
        }
        assertTrue("Expected cross-user read to fail", result.isFailure)
        val firestoreEx = result.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(firestoreEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
    }

    @Test
    fun observeBookmarks_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = GpscRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeBookmarks("unauth_uid").first()
            }
            fail("Expected exception for unauthenticated flow observation")
        } catch (e: Exception) {
            val firestoreEx = e.unwrapFirestoreException()
            assertNotNull("Expected nested FirebaseFirestoreException, got $e", firestoreEx)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
        }
    }

    @Test
    fun upsertSyllabusProgress_authenticatedOwner_createsAndUpdatesProgress() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = GpscRepository(firestore)

        val progressId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.upsertSyllabusProgress(
                existingProgressId = null,
                topicKey = "GPSC_HIST_INDUS",
                examCategory = "GPSC",
                subject = "Gujarat History & Culture",
                topicTitleEn = "Indus Valley Sites in Gujarat (Lothal & Dholavira)",
                topicTitleGu = "ગુજરાતમાં સિંધુ ખીણની સંસ્કૃતિના સ્થળો (લોથલ અને ધોળાવીરા)",
                completed = true,
                questionsAttempted = 5,
                questionsCorrect = 4
            ).getOrThrow()
        }
        assertTrue(progressId.isNotEmpty())

        val updatedId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.upsertSyllabusProgress(
                existingProgressId = progressId,
                topicKey = "GPSC_HIST_INDUS",
                examCategory = "GPSC",
                subject = "Gujarat History & Culture",
                topicTitleEn = "Indus Valley Sites in Gujarat (Lothal & Dholavira)",
                topicTitleGu = "ગુજરાતમાં સિંધુ ખીણની સંસ્કૃતિના સ્થળો (લોથલ અને ધોળાવીરા)",
                completed = true,
                questionsAttempted = 10,
                questionsCorrect = 9
            ).getOrThrow()
        }
        assertEquals(progressId, updatedId)

        val list = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeSyllabusProgress(uid).first { items ->
                items.any { it.id == progressId && it.questionsAttempted == 10 }
            }
        }
        assertTrue(list.any { it.id == progressId && it.questionsCorrect == 9 })
    }

    @Test
    fun forumTopicsAndReplies_createReplyAndPeerUpvote_succeeds() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val aliceRepo = GpscRepository(firestore)

        val topicId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createForumTopic(
                authorName = "Alice Aspirant",
                examCategory = "GPSC",
                subject = "Indian Polity",
                title = "Panchayati Raj 73rd Amendment key articles",
                body = "Sharing my revision chart for Articles 243 to 243-O.",
                resourceTitle = "Gujarat Panchayat Act 1993 Notes",
                resourceUrl = "https://panchayat.gujarat.gov.in"
            ).getOrThrow()
        }
        assertTrue(topicId.isNotEmpty())

        val createdTopic = withTimeout(FLOW_TIMEOUT_MS) {
            aliceRepo.observeForumTopics("GPSC").first { list -> list.any { it.id == topicId } }
                .first { it.id == topicId }
        }

        // Bob signs in, replies with a resource, and upvotes Alice's topic
        val bobUid = signInTestUser(BOB_EMAIL)
        val bobRepo = GpscRepository(firestore)

        val upvoteTopicRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.toggleForumTopicUpvote(createdTopic)
        }
        assertTrue("Expected peer upvote on topic to succeed", upvoteTopicRes.isSuccess)

        val replyId = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.createForumReply(
                topic = createdTopic.copy(upvoterIds = listOf(bobUid), upvoteCount = 1),
                authorName = "Bob Scholar",
                body = "Great chart! Also remember PESA 1996 provisions for Scheduled Areas.",
                resourceTitle = "PESA Act Summary",
                resourceUrl = "https://panchayat.gov.in"
            ).getOrThrow()
        }
        assertTrue(replyId.isNotEmpty())

        val createdReply = withTimeout(FLOW_TIMEOUT_MS) {
            bobRepo.observeForumReplies(topicId, "GPSC").first { list -> list.any { it.id == replyId } }
                .first { it.id == replyId }
        }

        // Alice signs back in and upvotes Bob's helpful reply
        signInTestUser(ALICE_EMAIL)
        val upvoteReplyRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.toggleForumReplyUpvote(createdReply)
        }
        assertTrue("Expected helpful reply upvote to succeed", upvoteReplyRes.isSuccess)
    }

    private companion object {
        const val ALICE_EMAIL = "alice@gpscai.test"
        const val BOB_EMAIL = "bob@gpscai.test"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
