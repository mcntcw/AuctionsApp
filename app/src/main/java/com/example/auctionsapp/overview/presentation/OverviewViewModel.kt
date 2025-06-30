package com.example.auctionsapp.overview.presentation

import android.annotation.SuppressLint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auctionsapp.authentication.domain.AuthenticationRepository
import com.example.auctionsapp.core.domain.AuctionRepository
import com.example.auctionsapp.core.domain.UserRepository
import com.example.auctionsapp.notifications.domain.NotificationRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class OverviewViewModel(
    private val authenticationRepository: AuthenticationRepository,
    private val userRepository: UserRepository,
    private val auctionRepository: AuctionRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {
    var state by mutableStateOf(OverviewState())

    private val _event = MutableSharedFlow<OverviewEvent>()
    val event: SharedFlow<OverviewEvent> = _event

    init {
        onAction(OverviewAction.GetUserInfo)
        onAction(OverviewAction.GetLatestAuctions)
        onAction(OverviewAction.CheckUnreadNotifications)
    }

    fun onAction(action: OverviewAction) {
        when (action) {
            OverviewAction.GetLatestAuctions ->
                viewModelScope.launch {

                    val success = getLatestAuction()
                    val event = if(success) {
                        OverviewEvent.GetLatestAuctionsSuccess
                    }
                    else {
                        OverviewEvent.GetLatsetAuctionsFailure
                    }
                    _event.emit(event)
                }
            OverviewAction.GetUserInfo -> {
                viewModelScope.launch {
                    val success = getUserInfo()
                    val event = if(success) {
                        OverviewEvent.GetUserInfoSuccess
                    }
                    else {
                        OverviewEvent.GetUserInfoFailure
                    }
                    _event.emit(event)
                }
            }
            OverviewAction.SignOut -> {
                viewModelScope.launch {
                    val success = signOut()
                    val event = if(success) {
                        OverviewEvent.SignOutSuccess
                    }
                    else {
                        OverviewEvent.SignOutFailure
                    }
                    _event.emit(event)
                }
            }

            OverviewAction.CheckUnreadNotifications -> {
                viewModelScope.launch {
                    val success = checkUnreadNotifications()
                    val event = if(success) {
                        OverviewEvent.CheckUnreadNotificationsSuccess
                    }
                    else {
                        OverviewEvent.CheckUnreadNotificationsFailure
                    }
                    _event.emit(event)
                }
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private suspend fun getLatestAuction(): Boolean {
        state = state.copy(isLatestAuctionsLoading = true)
       return try {
            val latestAuctions = auctionRepository.getLatestAuctions(20)
            state = state.copy(latestAuctions = latestAuctions)
            state = state.copy(isLatestAuctionsLoading = false)
             true

        } catch (_: Exception) {
           state = state.copy(isLatestAuctionsLoading = false)
             return false
        }

    }

    private suspend fun getUserInfo(): Boolean {
        val currentUser = authenticationRepository.getCurrentUser()
        val id = currentUser?.id?.toString() ?: return false

        return try {
            val user = userRepository.getUserById(id)
            user?.let {
                val cleanedName = it.name.trim('"')
//                val cleanedUrl = it.profilePictureUrl?.trim('"')

                state = state.copy(user = it.copy(name = cleanedName, profilePictureUrl = user.profilePictureUrl))
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun signOut(): Boolean {
        return try {
            authenticationRepository.signOut()
            true
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun checkUnreadNotifications(): Boolean {
        return try {
            val currentUser = authenticationRepository.getCurrentUser()
            if (currentUser != null) {
                val allNotifications = notificationRepository.getUserNotificationsPaged(currentUser.id!!)
                val unreadCount = allNotifications.count { !it.isRead }
                state = state.copy(hasUnreadNotifications = unreadCount > 0)
                true
            } else {
                state = state.copy(hasUnreadNotifications = false)
                false
            }
        } catch (e: Exception) {
            println("Błąd sprawdzania nieprzeczytanych powiadomień: ${e.message}")
            state = state.copy(hasUnreadNotifications = false)
            false
        }
    }




}