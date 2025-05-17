package com.example.auctionsapp.authentication.data

import com.example.auctionsapp.core.domain.User
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken

class SupabaseAuthenticationDataSource(private val supabase: SupabaseClient) {



   suspend fun isSignedIn(): Boolean {
        supabase.auth.awaitInitialization()
        val currentSession = supabase.auth.currentSessionOrNull()
        println("AKTUALNA SESJA NA STARCIE: $currentSession")
        return currentSession?.user != null

    }

    suspend fun signInWithIdToken(idToken: String, nonce: String): Boolean {
        supabase.auth.signInWith(IDToken) {
            this.idToken = idToken
            provider = Google
            this.nonce = nonce
        }
        val currentSession = supabase.auth.currentSessionOrNull()
        println("AKTUALNA SESJA PO ZALOGOWANIU: $currentSession")
        return supabase.auth.currentSessionOrNull()?.user != null

    }

    suspend fun signOut() {
        supabase.auth.signOut()
        val currentSession = supabase.auth.currentSessionOrNull()
        println("AKTUALNA SESJA PO WYLOGOWANIU: $currentSession")
    }

     fun getCurrentUser(): User? {
        val currentSession = supabase.auth.currentSessionOrNull()
        val user = currentSession?.user
        return user?.let {
            User(
            id = it.id,
            email = it.email ?: "",
            name = (it.userMetadata?.get("name")).toString(),
            profilePictureUrl = it.userMetadata?.get("avatar_url").toString(),
        )
        }
    }


}