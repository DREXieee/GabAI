package com.gabai.data.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.gabai.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

sealed class GoogleSignInResult {
    data class Success(val idToken: String) : GoogleSignInResult()
    data class Failure(val message: String) : GoogleSignInResult()
    data object Cancelled : GoogleSignInResult()
    data object NotConfigured : GoogleSignInResult()
}

// Uses Credential Manager (the current recommended API) rather than the
// deprecated GoogleSignInClient/GoogleSignInOptions. The resulting ID token is
// handed to gabai-backend's /api/auth/google, which verifies it with Supabase
// (signInWithIdToken) -- this app never talks to Supabase directly, matching
// the rest of the auth flow (see auth.service.ts).
object GoogleAuthHelper {
    suspend fun signIn(context: Context): GoogleSignInResult {
        if (BuildConfig.GOOGLE_WEB_CLIENT_ID.isBlank()) {
            return GoogleSignInResult.NotConfigured
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val result = CredentialManager.create(context).getCredential(context, request)
            val credential = GoogleIdTokenCredential.createFrom(result.credential.data)
            GoogleSignInResult.Success(credential.idToken)
        } catch (e: GetCredentialCancellationException) {
            GoogleSignInResult.Cancelled
        } catch (e: GetCredentialException) {
            GoogleSignInResult.Failure(e.message ?: "Google sign-in failed")
        } catch (e: GoogleIdTokenParsingException) {
            GoogleSignInResult.Failure("Received an invalid Google credential")
        }
    }
}
