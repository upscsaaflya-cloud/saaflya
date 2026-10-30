# Security Specification for GPSCAI

## 1. Data Invariants
1. **Multi-User Ownership Isolation (Personal Collections)**: Every document in `/bookmarks/{bookmarkId}`, `/mains_evaluations/{evalId}`, and `/syllabus_progress/{progressId}` MUST belong to an authenticated user (`request.auth != null`) and have `userId == request.auth.uid`.
2. **Community Forum Integrity (`/forum_topics` & `/forum_replies`)**:
   - Creating a topic or reply requires `request.auth != null` and `incoming().authorId == request.auth.uid`.
   - Creating a reply in `/forum_replies/{replyId}` requires `exists(/databases/$(database)/documents/forum_topics/$(incoming().topicId))` so no orphaned replies can be created.
   - `authorId`, `examCategory`, and `createdAt` (plus `subject` on topics and `topicId` on replies) are strictly immutable after creation.
   - Author-only content edits (`title`, `body`, `resourceTitle`, `resourceUrl`) are restricted to `existing().authorId == request.auth.uid`.
   - Collaborative actions (toggling upvotes in `upvoterIds`/`upvoteCount` and updating `replyCount`) are restricted via action-based `affectedKeys().hasOnly(...)` and validated by `isValidForumTopic()` / `isValidForumReply()`.
   - Only the topic/reply author (`existing().authorId == request.auth.uid`) may delete their own post.
3. **Query Enforcement**: No collection permits blanket `list` reads. Personal collections enforce `existing().userId == request.auth.uid`, `/forum_topics` enforces `isValidExamCategory(existing().examCategory)`, and `/forum_replies` enforces `isValidId(existing().topicId) && isValidExamCategory(existing().examCategory)`.
4. **Strict Schema & Bounds**: Every string field has a bounded `.size()`, every list has a bounded `.size()`, and every integer is range-checked.

## 2. The "Dirty Dozen" Adversarial Payloads
1. **Unauthenticated Read**: `get` or `list` on `/bookmarks` or `/forum_topics` without `request.auth`.
2. **Cross-User Private Read**: Alice reading Bob's `/bookmarks/bob_bm_1`.
3. **Unfiltered Private List Query**: Alice calling `db.collection("bookmarks").get()` without `.where("userId", "==", aliceUid)`.
4. **Identity Spoofing on Create**: Alice creating a `/forum_topics` document with `authorId: "bob_456"`.
5. **Orphaned Reply Injection**: Alice creating a `/forum_replies` document pointing to a non-existent `topicId`.
6. **Unauthorized Post Tampering**: Bob attempting to edit `title` or `body` of Alice's `/forum_topics` document.
7. **Unauthorized Forum Deletion**: Bob attempting to delete Alice's `/forum_topics` or `/forum_replies` document.
8. **Shadow Update (Ghost Field Injection)**: Updating `/forum_topics/{id}` with an unauthorized field `isPinned: true`.
9. **Author Transfer on Update**: Updating `authorId` on `/forum_topics/{id}` from Alice to Bob.
10. **Invalid Enum Injection**: Creating a `/forum_topics` document with `examCategory: "INVALID_EXAM"`.
11. **Oversized String DoS**: Creating `/forum_topics/{id}` with a 50,000-character `body`.
12. **Future Timestamp Spoofing**: Creating `/forum_replies/{id}` with `createdAt` set 1 hour in the future.
