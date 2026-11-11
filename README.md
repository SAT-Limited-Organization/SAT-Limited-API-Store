# SAT Limited API Store

An Android app for exploring SAT Limited API catalog entries and resources, trying requests in an in-app playground, and managing custom endpoint configurations and API keys. The project is built with Kotlin and Jetpack Compose and uses Firebase Authentication and Cloud Firestore for account-related data.

> **API availability:** The app includes catalog definitions and sample request/response data. The example routes and host below describe what the app presents; this repository alone does not confirm that a production API server is deployed or that every example endpoint is live. Configure a compatible backend before relying on these routes in production.

## API categories

| Category | What it is for | Example route shown in the catalog |
| --- | --- | --- |
| Exam papers | Search past papers and marking guides by board, subject, and year | `GET /v1/exams/papers?board=ZIMSEC&subject=Mathematics&year=2024` |
| Textbooks | Search curriculum textbooks and study resources | `GET /v1/textbooks/search?subject=Physics&grade=Form%204` |
| Movies | Browse movie listings and stream metadata | `GET /v1/movies/trending?quality=1080p` |
| YouTube tools | Look up video metadata and available media formats | `GET /v1/youtube/extract?url=https://youtube.com/watch?v=dQw4w9WgXcQ` |
| WhatsApp / Baileys | Send bot messages or integrate with Baileys webhooks | `POST /v1/baileys/messages/send-text` |
| AI (Gemini) | Submit prompts for answers, study help, or summaries | `POST /v1/ai/generate` |
| Custom | User-defined endpoint configuration with a selected method and response payload | User-configured, for example `/v1/custom/hello` |

The catalog examples are illustrative. Use services and media only when you have the necessary rights and follow their terms of service.

## How API requests look

The playground uses `https://api.satlimited.org` as its example base URL and sends an API key in the `x-sat-api-key` header. The host, credentials, and endpoint availability must be confirmed for your deployment. For example, an exam-paper request can be represented as:

```http
GET /v1/exams/papers?board=ZIMSEC&subject=Mathematics&year=2024 HTTP/1.1
Host: api.satlimited.org
x-sat-api-key: YOUR_API_KEY
Accept: application/json
```

A catalog sample response has this general JSON shape:

```json
{
  "status": "success",
  "board": "ZIMSEC",
  "subject": "Mathematics",
  "year": 2024,
  "totalPapers": 1,
  "papers": [
    {
      "id": "zimsec-math-2024-p1",
      "paper": "Paper 1",
      "duration": "2h 30m",
      "season": "November",
      "downloadUrl": "https://example.org/papers/paper.pdf",
      "markingSchemeUrl": "https://example.org/papers/marking-scheme.pdf"
    }
  ]
}
```

A WhatsApp message endpoint is shown as a `POST` with a JSON body:

```http
POST /v1/baileys/messages/send-text HTTP/1.1
Host: api.satlimited.org
x-sat-api-key: YOUR_API_KEY
Content-Type: application/json
```

```json
{
  "recipientJid": "263771234567@s.whatsapp.net",
  "messageText": "Your resource is ready."
}
```

The app's custom API builder also supports `GET`, `POST`, `PUT`, and `DELETE`. A custom API entry stores a name, description, category, endpoint path, content type, response payload, rate limit, and active status. Its configured response payload can be a JSON template such as:

```json
{
  "status": "success",
  "message": "Custom API response from SAT Limited"
}
```

## Using the app

1. Launch the Android app and sign in with the configured authentication provider.
2. In the catalog, search or filter by category, then choose **Explore Data** to inspect sample resource records or **Test in Tester** to open an API in the playground.
3. In the playground, select a store or custom API, review its method and endpoint, provide a request body when needed, and tap **Send Request**. The response inspector displays the result when a backend is configured.
4. Use **Export Code** to copy a request example for Node.js (Baileys), cURL, Python, or Kotlin.
5. In the custom API builder, choose **New API**, provide the endpoint details and JSON response payload, then save it to your account.
6. In API keys and logs, create and manage keys and review request telemetry. Treat API keys as secrets; do not commit them to source control.
7. Save catalog resources to access them later in the saved library.

## Build and configure

- Open the project in Android Studio with a compatible Android SDK and JDK installed. The app module targets SDK 36 and supports Android 7.0 (API 24) and later.
- Configure Firebase for your own project and add its Android configuration file (`google-services.json`) to the app module as required by the Google Services Gradle plugin.
- Review `.env.example` and the Gradle Secrets Plugin configuration. Put any required local secrets in an ignored `.env` file; never commit production credentials.
- Confirm Firebase Authentication, Firestore, and App Check settings are configured for your environment. The repository includes Firestore rules and a rules test file; review and test security rules before deployment.
- Build and run the `app` configuration from Android Studio. A production API backend and valid endpoint credentials are separate from building the Android app.

## Implementation notes

- UI: Kotlin, Jetpack Compose, Material 3.
- Authentication and user data: Firebase Authentication and Cloud Firestore.
- API networking dependencies: Retrofit and OkHttp.
- Sample catalog definitions and example payloads: `app/src/main/java/com/example/data/catalog/SatApiCatalog.kt`.
- Firestore data model blueprint: `firebase-blueprint.json`.
