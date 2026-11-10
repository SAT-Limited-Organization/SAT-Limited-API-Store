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

test("Unauthenticated user: cannot read user profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("Unauthenticated user: cannot read customApis", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("customApis").get());
});

test("Authenticated user: Alice can create her own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice Dev",
      email: "alice@satlimited.com",
      tier: "free",
      totalRequests: 0,
      quotaLimit: 1000,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Cross-user access: Bob cannot read or write Alice's custom APIs", async () => {
  // Alice creates a custom API
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("customApis").doc("api_exams_1").set({
      id: "api_exams_1",
      userId: ALICE_UID,
      name: "ZIMSEC Past Papers API",
      description: "Past exam question papers",
      category: "exams",
      method: "GET",
      endpointPath: "/v1/exams/zimsec",
      responseContentType: "application/json",
      responsePayload: "{\"status\": \"ok\"}",
      rateLimitPerMin: 60,
      isActive: true,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  // Bob attempts to read Alice's API -> must FAIL
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("customApis").doc("api_exams_1").get()
  );

  // Bob attempts to overwrite Alice's API -> must FAIL
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("customApis").doc("api_exams_1").set({
      id: "api_exams_1",
      userId: BOB_UID,
      name: "Hacked API",
      category: "exams",
      method: "GET",
      endpointPath: "/v1/exams/zimsec",
      isActive: true,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Append-only audit logs: Alice can create an apiLog but cannot update or delete it", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const logRef = aliceDb.collection("users").doc(ALICE_UID).collection("apiLogs").doc("log_123");
  await assertSucceeds(
    logRef.set({
      id: "log_123",
      userId: ALICE_UID,
      apiName: "Movie Box HD Stream API",
      endpoint: "/v1/movies/stream",
      method: "GET",
      statusCode: 200,
      latencyMs: 145,
      createdAt: new Date(),
    })
  );

  // Update must fail
  await assertFails(logRef.update({ statusCode: 500 }));
  // Delete must fail
  await assertFails(logRef.delete());
});
