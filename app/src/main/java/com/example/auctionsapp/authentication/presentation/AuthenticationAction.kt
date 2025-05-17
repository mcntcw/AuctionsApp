package com.example.auctionsapp.authentication.presentation

sealed interface AuthenticationAction {
    data object SignIn: AuthenticationAction
    data object IsSignedIn: AuthenticationAction
}
