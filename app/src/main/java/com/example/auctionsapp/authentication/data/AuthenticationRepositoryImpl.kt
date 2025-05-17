package com.example.auctionsapp.authentication.data

import com.example.auctionsapp.authentication.domain.AuthenticationRepository
import com.example.auctionsapp.core.domain.User

class AuthenticationRepositoryImpl(
    private val googleAuthDataSource: GoogleAuthenticationDataSource,
    private val supabaseAuthDataSource: SupabaseAuthenticationDataSource
) : AuthenticationRepository {

    override suspend fun signIn(): Boolean {
        if (isSignedIn()) {
            println("FUNKCJA SPRAWDZANIA LOGOWANIA PRZED LOGOWANIEM W TOKU")
            return true
        }

        try {
            println("ROZPOCZYNAM LOGOWANIE")
            val credentialResponse = googleAuthDataSource.getGoogleCredential()
            val googleIdTokenCredential = googleAuthDataSource.parseGoogleIdToken(credentialResponse)
                ?: throw IllegalArgumentException("Invalid Google credential")

            val idToken = googleIdTokenCredential.idToken
            val nonce = googleAuthDataSource.rawNonce

            return supabaseAuthDataSource.signInWithIdToken(idToken, nonce)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    override suspend fun signOut() {
        googleAuthDataSource.clearGoogleCredentialState()
        supabaseAuthDataSource.signOut()
    }


    override suspend fun isSignedIn(): Boolean {
        println("CZY ZALOGOWANY: ${supabaseAuthDataSource.isSignedIn()}")
        return supabaseAuthDataSource.isSignedIn()
    }

    override suspend fun getCurrentUser(): User? {
        return supabaseAuthDataSource.getCurrentUser()
    }






}
