package com.example.data.catalog

import com.example.data.model.ExamPaper
import com.example.data.model.MovieItem
import com.example.data.model.SchoolTextbook
import com.example.data.model.StoreApiDefinition
import com.example.data.model.YouTubeVideo

object SatApiCatalog {

    val storeApis: List<StoreApiDefinition> = listOf(
        StoreApiDefinition(
            id = "sat-exams-api",
            title = "Examination Papers API",
            summary = "Fetch past examination papers, marking guides, and syllabi across Cambridge, ZIMSEC, WAEC, SAT, and KCSE boards.",
            category = "exams",
            method = "GET",
            defaultEndpoint = "/v1/exams/papers?board=ZIMSEC&subject=Mathematics&year=2024",
            badge = "Academic",
            documentationMarkdown = """
                # Examination Papers API
                Query official past examination papers and answer schemes by board, subject, level, and year.
                
                ### Query Parameters:
                - `board`: Exam board (`ZIMSEC`, `Cambridge`, `WAEC`, `SAT`, `KCSE`)
                - `subject`: Subject name (e.g. `Mathematics`, `Physics`, `Chemistry`)
                - `year`: Exam year (e.g. `2024`, `2023`, `2022`)
                - `season`: Exam session (`June`, `November`, `Spring`)
                
                ### Authentication:
                Header: `x-sat-api-key: YOUR_KEY`
            """.trimIndent(),
            sampleRequestParams = mapOf(
                "board" to "ZIMSEC",
                "subject" to "Mathematics",
                "year" to "2024",
                "level" to "O-Level"
            ),
            sampleResponseJson = """
                {
                  "status": "success",
                  "board": "ZIMSEC",
                  "subject": "Mathematics",
                  "year": 2024,
                  "totalPapers": 2,
                  "papers": [
                    {
                      "id": "zimsec-math-2024-p1",
                      "paper": "Paper 1 (Non-Calculator)",
                      "duration": "2h 30m",
                      "season": "November",
                      "downloadUrl": "https://satlimited.org/dl/papers/zimsec_math_2024_p1.pdf",
                      "markingSchemeUrl": "https://satlimited.org/dl/papers/zimsec_math_2024_p1_ms.pdf"
                    },
                    {
                      "id": "zimsec-math-2024-p2",
                      "paper": "Paper 2 (Calculator)",
                      "duration": "2h 30m",
                      "season": "November",
                      "downloadUrl": "https://satlimited.org/dl/papers/zimsec_math_2024_p2.pdf",
                      "markingSchemeUrl": "https://satlimited.org/dl/papers/zimsec_math_2024_p2_ms.pdf"
                    }
                  ]
                }
            """.trimIndent()
        ),
        StoreApiDefinition(
            id = "sat-textbooks-api",
            title = "School Textbooks API",
            summary = "Access curriculum textbooks, e-books, chapter modules, and study guides for secondary and high school subjects.",
            category = "textbooks",
            method = "GET",
            defaultEndpoint = "/v1/textbooks/search?subject=Physics&grade=Form%204",
            badge = "Curriculum",
            documentationMarkdown = """
                # School Textbooks API
                Retrieve curriculum-aligned digitized school textbooks and reading modules.
                
                ### Query Parameters:
                - `subject`: Subject name (`Physics`, `Chemistry`, `Biology`, `Math`)
                - `grade`: Level / Grade (`Form 1-4`, `Grade 9-12`, `A-Level`)
                - `author`: Publisher or author name
            """.trimIndent(),
            sampleRequestParams = mapOf(
                "subject" to "Physics",
                "grade" to "Form 4",
                "format" to "pdf"
            ),
            sampleResponseJson = """
                {
                  "status": "success",
                  "query": "Physics Form 4",
                  "results": [
                    {
                      "id": "tb-phys-f4-01",
                      "title": "Conceptual Secondary Physics: Volume 4",
                      "publisher": "SAT Limited Educational Press",
                      "isbn": "978-1-849-55012-4",
                      "chaptersCount": 14,
                      "readerUrl": "https://satlimited.org/reader/textbook/tb-phys-f4-01",
                      "featuredChapters": ["Electromagnetism", "Quantum Energy", "Nuclear Physics"]
                    }
                  ]
                }
            """.trimIndent()
        ),
        StoreApiDefinition(
            id = "sat-movies-api",
            title = "Movie Box Stream API",
            summary = "Movie Box style API to search trending movies, stream server mirrors, 4K/1080p links, and subtitle files.",
            category = "movies",
            method = "GET",
            defaultEndpoint = "/v1/movies/trending?quality=1080p",
            badge = "Media",
            documentationMarkdown = """
                # Movie Box Stream API
                Query latest releases, streaming server mirrors, direct video URLs, and subtitles like on Movie Box.
                
                ### Parameters:
                - `action`: `trending`, `search`, `details`
                - `query`: Title search keyword
                - `quality`: `4k`, `1080p`, `720p`
            """.trimIndent(),
            sampleRequestParams = mapOf(
                "action" to "trending",
                "genre" to "Sci-Fi",
                "quality" to "1080p"
            ),
            sampleResponseJson = """
                {
                  "status": "success",
                  "source": "SAT Movie Box CDN",
                  "count": 3,
                  "movies": [
                    {
                      "id": "mv-dune-2",
                      "title": "Dune: Part Two",
                      "year": 2024,
                      "rating": 8.5,
                      "resolution": "1080p Full HD",
                      "streamUrl": "https://stream.satlimited.org/hls/dune2/master.m3u8",
                      "subtitles": ["en", "fr", "es"],
                      "serverMirrors": ["FastCDN-US", "FastCDN-EU"]
                    }
                  ]
                }
            """.trimIndent()
        ),
        StoreApiDefinition(
            id = "sat-youtube-api",
            title = "YouTube Stream & MP3 API",
            summary = "Extract high-speed direct video MP4 (1080p/720p) and MP3 audio streams, captions, and playlist metadata.",
            category = "youtube",
            method = "GET",
            defaultEndpoint = "/v1/youtube/extract?url=https://youtube.com/watch?v=dQw4w9WgXcQ",
            badge = "Extractor",
            documentationMarkdown = """
                # YouTube Tools API
                Extract direct playable media streams (MP4/MP3) without ads or bandwidth throttle.
                
                ### Parameters:
                - `url`: YouTube video URL or Video ID
                - `format`: `mp4_1080p`, `mp4_720p`, `mp3_320k`
            """.trimIndent(),
            sampleRequestParams = mapOf(
                "videoId" to "dQw4w9WgXcQ",
                "format" to "mp4_1080p"
            ),
            sampleResponseJson = """
                {
                  "status": "success",
                  "videoId": "dQw4w9WgXcQ",
                  "title": "Rick Astley - Never Gonna Give You Up",
                  "duration": "03:33",
                  "formats": {
                    "video_1080p": "https://yt-stream.satlimited.org/v/dQw4w9WgXcQ_1080p.mp4",
                    "video_720p": "https://yt-stream.satlimited.org/v/dQw4w9WgXcQ_720p.mp4",
                    "audio_320k": "https://yt-stream.satlimited.org/a/dQw4w9WgXcQ_320.mp3"
                  }
                }
            """.trimIndent()
        ),
        StoreApiDefinition(
            id = "sat-whatsapp-baileys-api",
            title = "WhatsApp Bot API (Baileys Support)",
            summary = "Official webhook & REST bridge for @whiskeysockets/baileys WhatsApp bots with QR auth, message dispatch, and event listeners.",
            category = "whatsapp",
            method = "POST",
            defaultEndpoint = "/v1/baileys/messages/send-text",
            badge = "Bot Bridge",
            documentationMarkdown = """
                # WhatsApp Baileys Bot API
                Built for developers building automated WhatsApp bots using the Baileys socket library.
                
                ### Capabilities:
                - Automated session manager with multi-device pairing
                - Real-time inbound webhook forwarder
                - Endpoints to send text, buttons, lists, and media files
                
                ### Baileys Node.js Client:
                ```javascript
                const { default: makeWASocket, useMultiFileAuthState } = require('@whiskeysockets/baileys');
                // Synchronize with SAT Limited API Store webhook:
                fetch('https://api.satlimited.org/v1/baileys/webhook/receive', {
                  method: 'POST',
                  headers: { 'x-sat-api-key': 'SAT_KEY_XYZ' },
                  body: JSON.stringify(msgPayload)
                });
                ```
            """.trimIndent(),
            sampleRequestBody = """
                {
                  "recipientJid": "263771234567@s.whatsapp.net",
                  "messageText": "Hello! Your examination paper download link is ready: https://satlimited.org/p/123",
                  "quotedMessageId": "BAE5A26B840D04EF"
                }
            """.trimIndent(),
            sampleResponseJson = """
                {
                  "status": "delivered",
                  "messageId": "BAE5SAT789ABCD01",
                  "recipientJid": "263771234567@s.whatsapp.net",
                  "timestamp": 1730537000,
                  "baileysStatus": "ACK_SERVER"
                }
            """.trimIndent()
        ),
        StoreApiDefinition(
            id = "sat-ai-api",
            title = "SAT AI Logic API (Gemini)",
            summary = "Frontier AI API powered by Gemini 3.8 Flash for intelligent question answering, exam solution steps, and bot chat completions.",
            category = "ai",
            method = "POST",
            defaultEndpoint = "/v1/ai/generate",
            badge = "AI Intelligence",
            documentationMarkdown = """
                # SAT AI API (Powered by Gemini)
                Execute state-of-the-art AI reasoning directly from SAT Limited store.
                
                ### Features:
                - Step-by-step exam solver
                - Textbook chapter summarizer
                - WhatsApp conversational bot auto-replies
                - High-speed streaming inference
            """.trimIndent(),
            sampleRequestBody = """
                {
                  "prompt": "Solve this ZIMSEC O-Level Math question: Solve the quadratic equation 2x^2 - 5x + 3 = 0 showing all steps.",
                  "mode": "step-by-step"
                }
            """.trimIndent(),
            sampleResponseJson = """
                {
                  "status": "success",
                  "model": "gemini-3.8-flash",
                  "result": "Step 1: Identify coefficients a=2, b=-5, c=3.\nStep 2: Calculate the discriminant D = b^2 - 4ac = (-5)^2 - 4(2)(3) = 25 - 24 = 1.\nStep 3: Apply the quadratic formula x = (-b ± √D) / (2a) = (5 ± 1) / 4.\nStep 4: x1 = 6/4 = 1.5, x2 = 4/4 = 1.\nRoots: x = 1 and x = 1.5"
                }
            """.trimIndent()
        )
    )

    val sampleExamPapers: List<ExamPaper> = listOf(
        ExamPaper("ex-1", "ZIMSEC", "Mathematics", "O-Level", 2024, "November", "Paper 1 & 2", "https://satlimited.org/papers/zimsec_math_2024.pdf"),
        ExamPaper("ex-2", "ZIMSEC", "Combined Science", "O-Level", 2024, "November", "Paper 1, 2 & 3", "https://satlimited.org/papers/zimsec_science_2024.pdf"),
        ExamPaper("ex-3", "Cambridge", "Physics 0625", "IGCSE", 2024, "June", "Paper 2, 4 & 6", "https://satlimited.org/papers/cambridge_phys_2024.pdf"),
        ExamPaper("ex-4", "Cambridge", "Mathematics 0580", "IGCSE", 2024, "June", "Paper 22 & 42", "https://satlimited.org/papers/cambridge_math_2024.pdf"),
        ExamPaper("ex-5", "WAEC", "General Mathematics", "WASSCE", 2023, "May/June", "Paper 1 & 2", "https://satlimited.org/papers/waec_math_2023.pdf"),
        ExamPaper("ex-6", "SAT", "SAT Digital Math Practice", "High School", 2024, "Spring", "Module 1 & 2", "https://satlimited.org/papers/sat_digital_math.pdf"),
        ExamPaper("ex-7", "KCSE", "Mathematics Alt A", "Secondary", 2023, "November", "Paper 1 & 2", "https://satlimited.org/papers/kcse_math_2023.pdf")
    )

    val sampleTextbooks: List<SchoolTextbook> = listOf(
        SchoolTextbook("tb-1", "Step Ahead: O-Level Mathematics", "Mathematics", "Form 3 & 4", "Longman Zimbabwe", 16, "978-0-582-00120-1", "https://satlimited.org/books/step_ahead_math.pdf"),
        SchoolTextbook("tb-2", "Focus on Combined Science", "Science", "Form 3 & 4", "College Press", 20, "978-0-797-40032-4", "https://satlimited.org/books/focus_science.pdf"),
        SchoolTextbook("tb-3", "Cambridge IGCSE Physics 3rd Ed", "Physics", "Grade 10 & 11", "Hodder Education", 24, "978-1-444-17642-1", "https://satlimited.org/books/igcse_physics.pdf"),
        SchoolTextbook("tb-4", "Advanced Level Pure Mathematics", "Pure Maths", "A-Level", "Bostock & Chandler", 18, "978-0-859-50092-0", "https://satlimited.org/books/pure_maths_a_level.pdf"),
        SchoolTextbook("tb-5", "African History in the 19th & 20th Century", "History", "Secondary", "SAT Educational Series", 12, "978-1-849-55018-6", "https://satlimited.org/books/african_history.pdf")
    )

    val sampleMovies: List<MovieItem> = listOf(
        MovieItem("mv-1", "Dune: Part Two", 2024, 8.5, "Action, Adventure, Sci-Fi", "4K UHD & 1080p", "Server Alpha (High Speed)", "https://stream.satlimited.org/mv/dune2_1080p.mp4", "Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family."),
        MovieItem("mv-2", "Deadpool & Wolverine", 2024, 7.8, "Action, Comedy, Sci-Fi", "1080p Full HD", "Server Titan (Fast CDN)", "https://stream.satlimited.org/mv/dp_wolv_1080p.mp4", "Wolverine is recovering from his injuries when he crosses paths with the loudmouth Deadpool."),
        MovieItem("mv-3", "Interstellar: Collector Edition", 2014, 8.7, "Adventure, Drama, Sci-Fi", "4K IMAX Remaster", "Server Galaxy (Direct)", "https://stream.satlimited.org/mv/interstellar_4k.mp4", "A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival."),
        MovieItem("mv-4", "Gladiator II", 2024, 7.6, "Action, Drama, History", "1080p Full HD", "Server Colosseum (CDN)", "https://stream.satlimited.org/mv/gladiator2_1080p.mp4", "Years after witnessing the death of Maximus, Lucius enters the Colosseum after his home is conquered."),
        MovieItem("mv-5", "The Batman: Chronicles", 2022, 7.8, "Action, Crime, Drama", "1080p Full HD", "Server Gotham (High Speed)", "https://stream.satlimited.org/mv/the_batman_1080p.mp4", "When a sadistic serial killer begins murdering key political figures in Gotham, Batman is forced to investigate.")
    )

    val sampleYouTubeVideos: List<YouTubeVideo> = listOf(
        YouTubeVideo("yt-1", "ZIMSEC Nov 2023 Math Paper 1 Full Worked Solutions", "SAT Math Academy", "1:42:15", "125K views", "https://satlimited.org/yt/math2023_audio.mp3", "https://satlimited.org/yt/math2023_1080p.mp4", "https://satlimited.org/yt/math2023_720p.mp4"),
        YouTubeVideo("yt-2", "How to Build a WhatsApp Bot using Baileys in 10 Minutes", "DevMastery Global", "18:40", "48K views", "https://satlimited.org/yt/baileys_audio.mp3", "https://satlimited.org/yt/baileys_1080p.mp4", "https://satlimited.org/yt/baileys_720p.mp4"),
        YouTubeVideo("yt-3", "Cambridge IGCSE Physics: All Formulas & Calculations Explained", "Physics Pro Review", "55:20", "210K views", "https://satlimited.org/yt/igcse_phys_audio.mp3", "https://satlimited.org/yt/igcse_phys_1080p.mp4", "https://satlimited.org/yt/igcse_phys_720p.mp4"),
        YouTubeVideo("yt-4", "Dune 2 Desert Planet Cinematic Soundtrack [320kbps Audio]", "Sci-Fi Soundscapes", "4:12", "1.2M views", "https://satlimited.org/yt/dune_ost.mp3", "https://satlimited.org/yt/dune_ost_1080p.mp4", "https://satlimited.org/yt/dune_ost_720p.mp4")
    )
}
