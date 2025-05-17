package com.example.auctionsapp.authentication.domain

import com.example.auctionsapp.core.domain.User

interface   AuthenticationRepository {
    suspend fun signIn():Boolean
    suspend fun signOut()
    suspend fun isSignedIn(): Boolean
    suspend fun getCurrentUser(): User?
}