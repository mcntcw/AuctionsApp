package com.example.auctionsapp.authentication.presentation

import com.example.auctionsapp.core.domain.User

data class AuthenticationState(
    val isSignedIn: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

