package com.example.auctionsapp.authentication.data

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import java.security.MessageDigest
import java.util.UUID

class GoogleAuthenticationDataSource(private val context: Context) {
    private val credentialManager = CredentialManager.create(context)
    val rawNonce = UUID.randomUUID().toString()
    private val bytes = rawNonce.toByteArray()
    private val md = MessageDigest.getInstance("SHA-256")
    private val hashedNonce = md.digest(bytes).fold("") { str, it -> str + "%02x".format(it) }

    suspend fun getGoogleCredential(): GetCredentialResponse {
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("757438019812-1uhac2tgbgnuav7fdp4ujbi7h6dcj1f4.apps.googleusercontent.com")
            .setNonce(hashedNonce)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return credentialManager.getCredential(request = request, context = context)
    }

    suspend fun clearGoogleCredentialState() {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }

    fun parseGoogleIdToken(credentialResponse: GetCredentialResponse): GoogleIdTokenCredential? {
        val credential = credentialResponse.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                println("display name: ${googleIdTokenCredential.displayName}")
                println("email: ${googleIdTokenCredential.id}")
                println("image: ${googleIdTokenCredential.profilePictureUri}")

                return  googleIdTokenCredential

            } catch (e: GoogleIdTokenParsingException) {
                e.printStackTrace()
                return  null
            }
        }
        return null
    }
}