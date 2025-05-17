package com.example.auctionsapp.authentication.presentation

sealed interface AuthenticationEvent {
    data object SignInSuccess: AuthenticationEvent
    data object SignInFailure: AuthenticationEvent
}