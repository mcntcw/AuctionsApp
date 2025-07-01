package com.example.auctionsapp.authentication.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auctionsapp.authentication.domain.AuthenticationRepository
import com.example.auctionsapp.core.domain.CreateUserUseCase
import com.example.auctionsapp.core.domain.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthenticationViewModel(
    private val authenticationRepository: AuthenticationRepository,
    private val createUserUseCase: CreateUserUseCase
) : ViewModel() {
    var state by mutableStateOf(AuthenticationState())

    private val _event = MutableSharedFlow<AuthenticationEvent>() 
    val event: SharedFlow<AuthenticationEvent> = _event

    private var _isSplashScreenAppear = MutableStateFlow(true)
    val isSplashScreenAppear = _isSplashScreenAppear.asStateFlow()


    init {
        onAction(AuthenticationAction.IsSignedIn)
    }

    open fun onAction(action: AuthenticationAction) {
        when (action) {
            is AuthenticationAction.SignIn -> {
                viewModelScope.launch {
                val success = signIn()
                val event = if (success) {
                    val currentUser = authenticationRepository.getCurrentUser()
                    if (currentUser != null) {

                        createUserUseCase(
                            User(
                                id = currentUser.id,
                                email = currentUser.email,
                                name = currentUser.name,
                                profilePictureUrl = currentUser.profilePictureUrl
                            )
                        )
                    }
                    AuthenticationEvent.SignInSuccess
                }
                    else AuthenticationEvent.SignInFailure
                _event.emit(event)
                }
            }
            is AuthenticationAction.IsSignedIn -> {
                viewModelScope.launch {
                    val isSignedIn = isSignedIn()
                    if (isSignedIn) {
                        _isSplashScreenAppear.value = false
                        _event.emit(AuthenticationEvent.SignInSuccess)
                    } else {
                        _isSplashScreenAppear.value = false
                        _event.emit(AuthenticationEvent.SignInFailure)
                    }

                }
            }
        }
    }



    private suspend fun isSignedIn(): Boolean {
        return try {
            state = state.copy(isLoading = true)
            val isSignedIn = authenticationRepository.isSignedIn()
            state = state.copy(isSignedIn = isSignedIn, isLoading = false)
            isSignedIn
        } catch (_: Exception) {
            state = state.copy(isLoading = false)
            false
        }
    }

    private suspend fun signIn(): Boolean {
        return try {
            state = state.copy(isLoading = true)
            val success = authenticationRepository.signIn()
            state = state.copy(isSignedIn = success, isLoading = false)
            success
        } catch (_: Exception) {
            state = state.copy(isLoading = false)
            false
        }
    }


}
