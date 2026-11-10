package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiApiManager
import com.example.data.ai.AiGenerationState
import com.example.data.catalog.SatApiCatalog
import com.example.data.model.ApiKey
import com.example.data.model.ApiLog
import com.example.data.model.CustomApi
import com.example.data.model.SavedResource
import com.example.data.model.StoreApiDefinition
import com.example.data.model.UserProfile
import com.example.data.repository.SatApiRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject

enum class AppNavTab {
    CATALOG,
    PLAYGROUND,
    BUILDER,
    KEYS_AND_STATS,
    SAVED
}

data class PlaygroundExecutionResult(
    val statusCode: Int = 200,
    val latencyMs: Long = 0L,
    val responseBody: String = "",
    val headers: Map<String, String> = emptyMap(),
    val error: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SatApiRepository(application)
    private val aiManager = AiApiManager()

    private val _selectedTab = MutableStateFlow(AppNavTab.CATALOG)
    val selectedTab: StateFlow<AppNavTab> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _categoryFilter = MutableStateFlow("all")
    val categoryFilter: StateFlow<String> = _categoryFilter.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(Firebase.auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    // State flows from Firestore
    val userProfile: StateFlow<UserProfile?> = repository.observeUserProfile()
        .catch { emit(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val customApis: StateFlow<List<CustomApi>> = repository.observeCustomApis()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val apiKeys: StateFlow<List<ApiKey>> = repository.observeApiKeys()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val apiLogs: StateFlow<List<ApiLog>> = repository.observeApiLogs()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedResources: StateFlow<List<SavedResource>> = repository.observeSavedResources()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Playground state
    private val _selectedStoreApi = MutableStateFlow<StoreApiDefinition?>(SatApiCatalog.storeApis.first())
    val selectedStoreApi: StateFlow<StoreApiDefinition?> = _selectedStoreApi.asStateFlow()

    private val _selectedCustomApi = MutableStateFlow<CustomApi?>(null)
    val selectedCustomApi: StateFlow<CustomApi?> = _selectedCustomApi.asStateFlow()

    private val _playgroundEndpoint = MutableStateFlow(SatApiCatalog.storeApis.first().defaultEndpoint)
    val playgroundEndpoint: StateFlow<String> = _playgroundEndpoint.asStateFlow()

    private val _playgroundMethod = MutableStateFlow(SatApiCatalog.storeApis.first().method)
    val playgroundMethod: StateFlow<String> = _playgroundMethod.asStateFlow()

    private val _playgroundBody = MutableStateFlow(SatApiCatalog.storeApis.first().sampleRequestBody)
    val playgroundBody: StateFlow<String> = _playgroundBody.asStateFlow()

    private val _isExecutingRequest = MutableStateFlow(false)
    val isExecutingRequest: StateFlow<Boolean> = _isExecutingRequest.asStateFlow()

    private val _executionResult = MutableStateFlow<PlaygroundExecutionResult?>(null)
    val executionResult: StateFlow<PlaygroundExecutionResult?> = _executionResult.asStateFlow()

    private val _aiStreamState = MutableStateFlow<AiGenerationState>(AiGenerationState.Idle)
    val aiStreamState: StateFlow<AiGenerationState> = _aiStreamState.asStateFlow()

    // Feedback banner message
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        val authListener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { auth ->
            _currentUser.value = auth.currentUser
            if (auth.currentUser != null) {
                viewModelScope.launch {
                    try {
                        repository.ensureUserProfile()
                    } catch (e: Exception) {
                        // handled gracefully
                    }
                }
            }
        }
        Firebase.auth.addAuthStateListener(authListener)
    }

    fun setTab(tab: AppNavTab) {
        _selectedTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String) {
        _categoryFilter.value = category
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun selectStoreApiForPlayground(api: StoreApiDefinition) {
        _selectedStoreApi.value = api
        _selectedCustomApi.value = null
        _playgroundEndpoint.value = api.defaultEndpoint
        _playgroundMethod.value = api.method
        _playgroundBody.value = api.sampleRequestBody
        _executionResult.value = null
        _aiStreamState.value = AiGenerationState.Idle
        _selectedTab.value = AppNavTab.PLAYGROUND
    }

    fun selectCustomApiForPlayground(api: CustomApi) {
        _selectedCustomApi.value = api
        _selectedStoreApi.value = null
        _playgroundEndpoint.value = api.endpointPath
        _playgroundMethod.value = api.method
        _playgroundBody.value = api.responsePayload
        _executionResult.value = null
        _aiStreamState.value = AiGenerationState.Idle
        _selectedTab.value = AppNavTab.PLAYGROUND
    }

    fun updatePlaygroundParams(endpoint: String, method: String, body: String) {
        _playgroundEndpoint.value = endpoint
        _playgroundMethod.value = method
        _playgroundBody.value = body
    }

    /**
     * Executes the playground request against SAT Limited store engine or Gemini AI
     */
    fun executePlaygroundRequest() {
        val endpoint = _playgroundEndpoint.value
        val method = _playgroundMethod.value
        val body = _playgroundBody.value
        val isAiApi = _selectedStoreApi.value?.category == "ai" || endpoint.contains("/v1/ai")

        _isExecutingRequest.value = true
        _executionResult.value = null

        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            try {
                if (isAiApi) {
                    val prompt = try {
                        val json = JSONObject(body)
                        json.optString("prompt", "Hello Gemini AI from SAT Limited store")
                    } catch (e: Exception) {
                        body.ifBlank { "Explain the SAT Limited API Store capabilities" }
                    }

                    val aiResult = aiManager.generateUnary(prompt)
                    val latency = System.currentTimeMillis() - startTime
                    if (aiResult.isSuccess) {
                        val text = aiResult.getOrThrow()
                        val formattedJson = JSONObject().apply {
                            put("status", "success")
                            put("engine", "Firebase AI Logic (gemini-3.8-flash)")
                            put("endpoint", endpoint)
                            put("prompt", prompt)
                            put("completion", text)
                            put("tokens_latency_ms", latency)
                        }.toString(2)

                        _executionResult.value = PlaygroundExecutionResult(
                            statusCode = 200,
                            latencyMs = latency,
                            responseBody = formattedJson,
                            headers = mapOf(
                                "content-type" to "application/json",
                                "x-sat-engine" to "gemini-3.8-flash",
                                "x-ratelimit-remaining" to "998"
                            )
                        )
                        repository.logApiCall("SAT AI API", endpoint, method, 200L, latency)
                    } else {
                        val errMsg = aiResult.exceptionOrNull()?.localizedMessage ?: "AI generation failed"
                        _executionResult.value = PlaygroundExecutionResult(
                            statusCode = 500,
                            latencyMs = latency,
                            responseBody = """{"status": "error", "message": "$errMsg"}""",
                            error = errMsg
                        )
                        repository.logApiCall("SAT AI API", endpoint, method, 500L, latency)
                    }
                } else if (_selectedCustomApi.value != null) {
                    // Custom API execution simulation
                    val custom = _selectedCustomApi.value!!
                    kotlinx.coroutines.delay(120) // Realistic network latency
                    val latency = System.currentTimeMillis() - startTime
                    _executionResult.value = PlaygroundExecutionResult(
                        statusCode = 200,
                        latencyMs = latency,
                        responseBody = custom.responsePayload.ifBlank { """{"message": "Custom API OK", "id": "${custom.id}"}""" },
                        headers = mapOf(
                            "content-type" to custom.responseContentType,
                            "x-sat-custom-id" to custom.id,
                            "x-sat-author" to (userProfile.value?.displayName ?: "Dev")
                        )
                    )
                    repository.logApiCall(custom.name, endpoint, method, 200L, latency)
                } else {
                    // Store catalog mock endpoint execution
                    val api = _selectedStoreApi.value
                    kotlinx.coroutines.delay(85) // Realistic network roundtrip
                    val latency = System.currentTimeMillis() - startTime
                    val responseJson = api?.sampleResponseJson ?: """{"status": "success", "endpoint": "$endpoint"}"""
                    _executionResult.value = PlaygroundExecutionResult(
                        statusCode = 200,
                        latencyMs = latency,
                        responseBody = responseJson,
                        headers = mapOf(
                            "content-type" to "application/json",
                            "x-sat-store-version" to "v1.2",
                            "cache-control" to "public, max-age=3600"
                        )
                    )
                    repository.logApiCall(api?.title ?: "Store API", endpoint, method, 200L, latency)
                }
            } catch (e: Exception) {
                val latency = System.currentTimeMillis() - startTime
                _executionResult.value = PlaygroundExecutionResult(
                    statusCode = 500,
                    latencyMs = latency,
                    responseBody = """{"status": "error", "message": "${e.localizedMessage}"}""",
                    error = e.localizedMessage
                )
            } finally {
                _isExecutingRequest.value = false
            }
        }
    }

    fun createCustomApi(
        name: String,
        description: String,
        category: String,
        method: String,
        endpointPath: String,
        payload: String
    ) {
        viewModelScope.launch {
            val api = CustomApi(
                name = name,
                description = description,
                category = category,
                method = method,
                endpointPath = endpointPath,
                responsePayload = payload,
                isActive = true
            )
            val result = repository.createCustomApi(api)
            if (result.isSuccess) {
                _userMessage.value = "Custom API '$name' created successfully!"
            } else {
                _userMessage.value = "Failed to create API: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun deleteCustomApi(apiId: String) {
        viewModelScope.launch {
            val result = repository.deleteCustomApi(apiId)
            if (result.isSuccess) {
                _userMessage.value = "Custom API deleted"
            }
        }
    }

    fun generateNewApiKey(keyName: String) {
        viewModelScope.launch {
            val result = repository.generateApiKey(keyName)
            if (result.isSuccess) {
                _userMessage.value = "API Key '${keyName}' generated successfully!"
            } else {
                _userMessage.value = "Failed to generate key: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun deleteApiKey(keyId: String) {
        viewModelScope.launch {
            repository.deleteApiKey(keyId)
            _userMessage.value = "API key revoked"
        }
    }

    fun bookmarkResource(type: String, title: String, subtitle: String, url: String) {
        viewModelScope.launch {
            val resource = SavedResource(
                itemType = type,
                title = title,
                subtitle = subtitle,
                resourceUrl = url
            )
            val result = repository.saveResource(resource)
            if (result.isSuccess) {
                _userMessage.value = "Saved to your library!"
            }
        }
    }

    fun removeSavedResource(id: String) {
        viewModelScope.launch {
            repository.deleteSavedResource(id)
            _userMessage.value = "Item removed from library"
        }
    }
}
