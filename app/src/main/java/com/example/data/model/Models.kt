package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val tier: String = "free",
    val totalRequests: Long = 0L,
    val quotaLimit: Long = 1000L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CustomApi(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "custom",
    val method: String = "GET",
    val endpointPath: String = "",
    val responseContentType: String = "application/json",
    val responsePayload: String = "{}",
    val rateLimitPerMin: Long = 60L,
    @get:PropertyName("isActive") @set:PropertyName("isActive") var isActive: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ApiKey(
    val id: String = "",
    val userId: String = "",
    val keyName: String = "",
    val apiKeyMasked: String = "",
    val apiKeyHash: String = "",
    val requestCount: Long = 0L,
    @get:PropertyName("isActive") @set:PropertyName("isActive") var isActive: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ApiLog(
    val id: String = "",
    val userId: String = "",
    val apiName: String = "",
    val endpoint: String = "",
    val method: String = "GET",
    val statusCode: Long = 200L,
    val latencyMs: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SavedResource(
    val id: String = "",
    val userId: String = "",
    val itemType: String = "paper", // paper, textbook, movie, video, snippet
    val title: String = "",
    val subtitle: String = "",
    val resourceUrl: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

/**
 * Pre-configured Store API catalog item in SAT Limited API store
 */
data class StoreApiDefinition(
    val id: String,
    val title: String,
    val summary: String,
    val category: String,
    val method: String,
    val defaultEndpoint: String,
    val badge: String,
    val documentationMarkdown: String,
    val sampleRequestParams: Map<String, String> = emptyMap(),
    val sampleRequestBody: String = "",
    val sampleResponseJson: String = ""
)

/**
 * Domain representations for store data items
 */
data class ExamPaper(
    val id: String,
    val examBoard: String, // Cambridge, ZIMSEC, WAEC, SAT, KCSE, GCSE
    val subject: String,
    val level: String, // O-Level, A-Level, Secondary, High School
    val year: Int,
    val season: String, // June, Nov, Spring
    val paperNumber: String,
    val downloadUrl: String,
    val markingSchemeAvailable: Boolean = true
)

data class SchoolTextbook(
    val id: String,
    val title: String,
    val subject: String,
    val gradeLevel: String,
    val authorOrPublisher: String,
    val totalChapters: Int,
    val isbn: String,
    val readUrl: String
)

data class MovieItem(
    val id: String,
    val title: String,
    val year: Int,
    val rating: Double,
    val genre: String,
    val quality: String, // 4K UHD, 1080p Full HD, 720p
    val streamServer: String,
    val downloadUrl: String,
    val synopsis: String
)

data class YouTubeVideo(
    val id: String,
    val title: String,
    val channel: String,
    val duration: String,
    val views: String,
    val audioDownloadUrl: String,
    val video1080pUrl: String,
    val video720pUrl: String
)

data class BaileysWebhookEvent(
    val eventType: String,
    val senderJid: String,
    val messageText: String,
    val timestamp: Long,
    val messageId: String
)
