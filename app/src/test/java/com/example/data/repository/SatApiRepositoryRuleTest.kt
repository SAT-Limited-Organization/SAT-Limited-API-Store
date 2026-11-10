package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.CustomApi
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class SatApiRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createCustomApi_authenticatedUser_succeeds() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = SatApiRepository(firestore)

        val apiId = "api_${UUID.randomUUID().toString().take(6)}"
        val customApi = CustomApi(
            id = apiId,
            name = "Zimsec Test API",
            description = "Math exam papers",
            category = "exams",
            method = "GET",
            endpointPath = "/v1/exams/math",
            isActive = true
        )

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createCustomApi(customApi)
        }
        assertTrue("Expected createCustomApi to succeed", result.isSuccess)
        assertEquals(apiId, result.getOrThrow())
    }

    @Test
    fun observeCustomApis_authenticatedOwner_returnsCreatedApi() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = SatApiRepository(firestore)

        val apiId = "api_${UUID.randomUUID().toString().take(6)}"
        val customApi = CustomApi(
            id = apiId,
            name = "Textbooks API",
            category = "textbooks",
            method = "GET",
            endpointPath = "/v1/books/physics",
            isActive = true
        )
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createCustomApi(customApi).getOrThrow()
        }

        val apis = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeCustomApis().first { list -> list.any { it.id == apiId } }
        }
        assertNotNull(apis)
        assertTrue(apis.any { it.id == apiId })
    }

    @Test
    fun observeCustomApis_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = SatApiRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeCustomApis().first()
            }
            fail("Expected exception when unauthenticated")
        } catch (e: Exception) {
            // Unwrapping or checking IllegalStateException or PERMISSION_DENIED
            val isExpected = e is IllegalStateException || (e.unwrapFirestoreException()?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED)
            assertTrue("Expected auth exception or PERMISSION_DENIED, got $e", isExpected)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice@satlimited.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
