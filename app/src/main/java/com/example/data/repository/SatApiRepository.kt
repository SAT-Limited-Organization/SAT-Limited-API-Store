package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.ApiKey
import com.example.data.model.ApiLog
import com.example.data.model.CustomApi
import com.example.data.model.SavedResource
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException

class SatApiRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing SAT Limited store.")
    }

    fun observeUserProfile(): Flow<UserProfile?> = flow {
        val uid = requireUserId()
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(UserProfile::class.java)
                    } else {
                        null
                    }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.READ, path)
                    throw error
                }
        )
    }

    suspend fun ensureUserProfile(): UserProfile {
        val uid = requireUserId()
        val user = auth.currentUser
        val docRef = db.collection("users").document(uid)
        val snapshot = docRef.get().await()
        if (snapshot.exists()) {
            return snapshot.toObject(UserProfile::class.java) ?: UserProfile(userId = uid)
        }

        val newProfile = UserProfile(
            userId = uid,
            displayName = user?.displayName ?: "Developer",
            email = user?.email ?: "developer@satlimited.com",
            tier = "pro",
            totalRequests = 12L,
            quotaLimit = 10000L
        )
        val payload = mapOf(
            "userId" to uid,
            "displayName" to newProfile.displayName,
            "email" to newProfile.email,
            "tier" to newProfile.tier,
            "totalRequests" to newProfile.totalRequests,
            "quotaLimit" to newProfile.quotaLimit,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
        return newProfile
    }

    fun observeCustomApis(): Flow<List<CustomApi>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/customApis"
        emitAll(
            db.collection("users").document(uid).collection("customApis")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(CustomApi::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun createCustomApi(api: CustomApi): Result<String> {
        val uid = requireUserId()
        val generatedId = if (api.id.isNotBlank()) api.id else "api_${UUID.randomUUID().toString().take(8)}"
        val docRef = db.collection("users").document(uid).collection("customApis").document(generatedId)
        return try {
            val payload = mapOf(
                "id" to generatedId,
                "userId" to uid,
                "name" to api.name,
                "description" to api.description,
                "category" to api.category,
                "method" to api.method,
                "endpointPath" to api.endpointPath,
                "responseContentType" to api.responseContentType,
                "responsePayload" to api.responsePayload,
                "rateLimitPerMin" to api.rateLimitPerMin,
                "isActive" to api.isActive,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(generatedId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateCustomApi(api: CustomApi): Result<Unit> {
        val uid = requireUserId()
        require(api.id.isNotBlank()) { "API ID must not be blank" }
        requireNotNull(api.createdAt) { "createdAt must not be null for update" }
        val docRef = db.collection("users").document(uid).collection("customApis").document(api.id)
        return try {
            val payload = mapOf(
                "id" to api.id,
                "userId" to uid,
                "name" to api.name,
                "description" to api.description,
                "category" to api.category,
                "method" to api.method,
                "endpointPath" to api.endpointPath,
                "responseContentType" to api.responseContentType,
                "responsePayload" to api.responsePayload,
                "rateLimitPerMin" to api.rateLimitPerMin,
                "isActive" to api.isActive,
                "createdAt" to api.createdAt,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteCustomApi(apiId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("customApis").document(apiId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    fun observeApiKeys(): Flow<List<ApiKey>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/apiKeys"
        emitAll(
            db.collection("users").document(uid).collection("apiKeys")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(ApiKey::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun generateApiKey(keyName: String): Result<ApiKey> {
        val uid = requireUserId()
        val keyId = "key_${UUID.randomUUID().toString().take(8)}"
        val rawToken = "sat_live_${UUID.randomUUID().toString().replace("-", "")}"
        val masked = "${rawToken.take(8)}...${rawToken.takeLast(4)}"
        val docRef = db.collection("users").document(uid).collection("apiKeys").document(keyId)
        return try {
            val payload = mapOf(
                "id" to keyId,
                "userId" to uid,
                "keyName" to keyName,
                "apiKeyMasked" to masked,
                "apiKeyHash" to rawToken,
                "requestCount" to 0L,
                "isActive" to true,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(
                ApiKey(
                    id = keyId,
                    userId = uid,
                    keyName = keyName,
                    apiKeyMasked = masked,
                    apiKeyHash = rawToken,
                    requestCount = 0L,
                    isActive = true
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteApiKey(keyId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("apiKeys").document(keyId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    fun observeApiLogs(): Flow<List<ApiLog>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/apiLogs"
        emitAll(
            db.collection("users").document(uid).collection("apiLogs")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(ApiLog::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun logApiCall(
        apiName: String,
        endpoint: String,
        method: String,
        status: Long,
        latencyMs: Long
    ) {
        val uid = try { requireUserId() } catch (e: Exception) { return }
        val logId = "log_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}"
        val docRef = db.collection("users").document(uid).collection("apiLogs").document(logId)
        try {
            val payload = mapOf(
                "id" to logId,
                "userId" to uid,
                "apiName" to apiName,
                "endpoint" to endpoint,
                "method" to method,
                "statusCode" to status,
                "latencyMs" to latencyMs,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
        }
    }

    fun observeSavedResources(): Flow<List<SavedResource>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/savedResources"
        emitAll(
            db.collection("users").document(uid).collection("savedResources")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(SavedResource::class.java) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun saveResource(resource: SavedResource): Result<Unit> {
        val uid = requireUserId()
        val docId = if (resource.id.isNotBlank()) resource.id else "res_${UUID.randomUUID().toString().take(8)}"
        val docRef = db.collection("users").document(uid).collection("savedResources").document(docId)
        return try {
            val payload = mapOf(
                "id" to docId,
                "userId" to uid,
                "itemType" to resource.itemType,
                "title" to resource.title,
                "subtitle" to resource.subtitle,
                "resourceUrl" to resource.resourceUrl,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteSavedResource(resourceId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("savedResources").document(resourceId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }
}
