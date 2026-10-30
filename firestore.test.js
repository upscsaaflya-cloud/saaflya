const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function validBookmarkPayload(userId) {
  const now = new Date();
  return {
    userId,
    examCategory: "GPSC",
    subject: "Gujarat History & Culture",
    questionText: "Where is Rani ki Vav located?",
    questionTextGu: "રાણીની વાવ ક્યાં આવેલી છે?",
    options: ["Patan", "Junagadh", "Vadodara", "Bhuj"],
    optionsGu: ["પાટણ", "જૂનાગઢ", "વડોદરા", "ભુજ"],
    correctOptionIndex: 0,
    explanation: "Rani ki Vav is a stepwell situated in the town of Patan in Gujarat.",
    explanationGu: "રાણીની વાવ ગુજરાતના પાટણ શહેરમાં આવેલી વાવ છે.",
    referenceNote: "GPSC Class 1-2 Cultural Heritage Syllabus",
    personalNote: "Important UNESCO World Heritage site",
    createdAt: now,
    updatedAt: now,
  };
}

function validTopicPayload(authorId) {
  const now = new Date();
  return {
    authorId,
    authorName: "Aarav Patel",
    examCategory: "GPSC",
    subject: "Gujarat History & Culture",
    title: "Best notes for Solanki Era Maru-Gurjara temple architecture?",
    body: "Sharing my compiled notes on Sun Temple Modhera and Rani ki Vav for GPSC Class 1-2 Mains.",
    resourceTitle: "GCERT Cultural Heritage Ch. 4 Summary Notes",
    resourceUrl: "https://gpsc.gujarat.gov.in",
    upvoterIds: [],
    upvoteCount: 0,
    replyCount: 0,
    createdAt: now,
    updatedAt: now,
  };
}

test("Unauthenticated user: cannot read or create bookmarks or forum topics", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("bookmarks").doc("bm_1").get());
  await assertFails(unauthDb.collection("forum_topics").doc("top_1").get());
  await assertFails(unauthDb.collection("forum_topics").doc("top_1").set(validTopicPayload(ALICE_UID)));
});

test("Authenticated user: can create and query own bookmarks with userId filter", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("bookmarks").doc("bm_alice").set(validBookmarkPayload(ALICE_UID)));
  await assertSucceeds(
    aliceDb.collection("bookmarks").where("userId", "==", ALICE_UID).get()
  );
});

test("Authenticated user: cannot read another user's bookmark or query without userId filter", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("bookmarks").doc("bm_bob").set(validBookmarkPayload(BOB_UID));
  });
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("bookmarks").doc("bm_bob").get());
  await assertFails(aliceDb.collection("bookmarks").get());
});

test("Forum topics & replies: create, query by examCategory, upvote by peer, and block unauthorized body edits", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  // Alice creates a forum topic
  await assertSucceeds(
    aliceDb.collection("forum_topics").doc("topic_1").set(validTopicPayload(ALICE_UID))
  );

  // Bob queries forum topics by examCategory
  await assertSucceeds(
    bobDb.collection("forum_topics").where("examCategory", "==", "GPSC").get()
  );

  // Bob upvotes Alice's topic (allowed collaborative action)
  await assertSucceeds(
    bobDb.collection("forum_topics").doc("topic_1").update({
      upvoterIds: [BOB_UID],
      upvoteCount: 1,
      updatedAt: new Date(),
    })
  );

  // Bob attempts to tamper with Alice's topic title (rejected!)
  await assertFails(
    bobDb.collection("forum_topics").doc("topic_1").update({
      title: "Hacked Title",
      updatedAt: new Date(),
    })
  );

  // Bob posts a helpful reply with a study resource on Alice's topic
  const now = new Date();
  await assertSucceeds(
    bobDb.collection("forum_replies").doc("reply_1").set({
      topicId: "topic_1",
      authorId: BOB_UID,
      authorName: "Meera Shah",
      examCategory: "GPSC",
      body: "Also check the Gujarat State Archeology Department monograph on Patan stepwells!",
      resourceTitle: "Gujarat Archeology Monograph - Stepwells",
      resourceUrl: "https://archeology.gujarat.gov.in",
      upvoterIds: [],
      upvoteCount: 0,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Alice upvotes Bob's helpful reply
  await assertSucceeds(
    aliceDb.collection("forum_replies").doc("reply_1").update({
      upvoterIds: [ALICE_UID],
      upvoteCount: 1,
      updatedAt: new Date(),
    })
  );

  // Orphaned reply to non-existent topic is rejected
  await assertFails(
    bobDb.collection("forum_replies").doc("reply_orphan").set({
      topicId: "non_existent_topic",
      authorId: BOB_UID,
      authorName: "Meera Shah",
      examCategory: "GPSC",
      body: "Orphaned reply",
      upvoterIds: [],
      upvoteCount: 0,
      createdAt: now,
      updatedAt: now,
    })
  );
});
