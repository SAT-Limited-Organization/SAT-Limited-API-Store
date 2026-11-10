package com.example.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestoreException

enum class OperationType {
    CREATE,
    READ,
    UPDATE,
    DELETE,
    LIST
}

private const val TAG = "FirestoreError"

/**
 * Structured error handler for Firestore operations per Firebase guidelines.
 */
fun handleFirestoreError(
    exception: Exception,
    operationType: OperationType,
    path: String
) {
    val code = if (exception is FirebaseFirestoreException) exception.code.name else "UNKNOWN"
    val message = exception.localizedMessage ?: "Unknown error"
    
    Log.e(
        TAG,
        """
        {
          "status": "error",
          "operation": "$operationType",
          "path": "$path",
          "code": "$code",
          "message": "$message"
        }
        """.trimIndent(),
        exception
    )
}
