package com.example.data.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout

private const val TAG = "AiApiManager"

sealed interface AiGenerationState {
    data object Idle : AiGenerationState
    data object Loading : AiGenerationState
    data class Streaming(val chunk: String, val fullText: String) : AiGenerationState
    data class Success(val fullText: String) : AiGenerationState
    data class Error(val message: String) : AiGenerationState
}

class AiApiManager {

    private val model by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    fun generateStream(prompt: String): Flow<AiGenerationState> = flow {
        emit(AiGenerationState.Loading)
        val accumulatedText = StringBuilder()
        try {
            withTimeout(60_000L) {
                model.generateContentStream(prompt).collect { chunk ->
                    val text = chunk.text.orEmpty()
                    accumulatedText.append(text)
                    emit(AiGenerationState.Streaming(text, accumulatedText.toString()))
                }
            }
            if (accumulatedText.isNotBlank()) {
                emit(AiGenerationState.Success(accumulatedText.toString()))
            } else {
                emit(AiGenerationState.Error("AI returned an empty response."))
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI streaming timed out", e)
            emit(AiGenerationState.Error("Generation timed out after 60 seconds."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI streaming failed", e)
            emit(AiGenerationState.Error(e.localizedMessage ?: "AI generation failed."))
        }
    }

    suspend fun generateUnary(prompt: String): Result<String> {
        return try {
            val response = withTimeout(60_000L) {
                model.generateContent(prompt)
            }
            val text = response.text
            if (!text.isNullOrBlank()) {
                Result.success(text)
            } else {
                Result.failure(IllegalStateException("Empty AI response received."))
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI generation timed out", e)
            Result.failure(e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI generation error", e)
            Result.failure(e)
        }
    }
}
