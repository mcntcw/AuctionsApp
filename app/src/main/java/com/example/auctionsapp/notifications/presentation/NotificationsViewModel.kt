package com.example.auctionsapp.notifications.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auctionsapp.authentication.domain.AuthenticationRepository
import com.example.auctionsapp.notifications.domain.NotificationRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val notificationRepository: NotificationRepository,
    private val authenticationRepository: AuthenticationRepository
) : ViewModel() {

    var state by mutableStateOf(NotificationsState())
        private set

    private val _event = MutableSharedFlow<NotificationsEvent>()
    val event: SharedFlow<NotificationsEvent> = _event

    init {
        getCurrentUser()
        onAction(NotificationsAction.LoadNotifications)
    }

    fun onAction(action: NotificationsAction) {
        when (action) {
            is NotificationsAction.LoadNotifications -> loadNotifications()
            is NotificationsAction.LoadNextPage -> loadNextPage()
            is NotificationsAction.MarkAsRead -> markAsRead(action.notificationId)
        }
    }

    private fun getCurrentUser() {
        viewModelScope.launch {
            try {
                val currentUser = authenticationRepository.getCurrentUser()
                state = state.copy(currentUserId = currentUser?.id)
            } catch (e: Exception) {
                println("❌ Błąd pobierania aktualnego użytkownika: ${e.message}")
            }
        }
    }

    private fun loadNotifications() {
        if (state.isLoading) return

        viewModelScope.launch {
            try {
                state = state.copy(isLoading = true)
                val userId = state.currentUserId ?: return@launch

                val notifications = notificationRepository.getUserNotificationsPaged(
                    userId = userId,
                    limit = 20,
                    offset = 0
                )

                state = state.copy(
                    notifications = notifications,
                    isLoading = false,
                    page = 1,
                    hasMore = notifications.size == 20,
                )

                _event.emit(NotificationsEvent.LoadNotificationsSuccess)
            } catch (e: Exception) {
                state = state.copy(isLoading = false)
                _event.emit(NotificationsEvent.LoadNotificationsFailure)
                println("❌ Błąd ładowania powiadomień: ${e.message}")
            }
        }
    }

    private fun loadNextPage() {
        if (state.isLoading || !state.hasMore) return

        viewModelScope.launch {
            try {
                state = state.copy(isLoading = true)
                val userId = state.currentUserId ?: return@launch

                val newNotifications = notificationRepository.getUserNotificationsPaged(
                    userId = userId,
                    limit = 20,
                    offset = state.notifications.size
                )

                state = state.copy(
                    notifications = state.notifications + newNotifications,
                    isLoading = false,
                    page = state.page + 1,
                    hasMore = newNotifications.size == 20
                )
            } catch (e: Exception) {
                state = state.copy(isLoading = false)
                println("❌ Błąd ładowania kolejnej strony: ${e.message}")
            }
        }
    }

    private fun refreshNotifications() {
        state = state.copy(
            notifications = emptyList(),
            page = 0,
            hasMore = true
        )
        loadNotifications()
    }

    private fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                notificationRepository.markAsRead(notificationId)
                val updatedNotifications = state.notifications.map { notification ->
                    if (notification.id == notificationId) {
                        notification.copy(isRead = true)
                    } else {
                        notification
                    }
                }

                val newUnreadCount = updatedNotifications.count { !it.isRead }

                state = state.copy(
                    notifications = updatedNotifications,
                )

                _event.emit(NotificationsEvent.MarkAsReadSuccess)
            } catch (e: Exception) {
                _event.emit(NotificationsEvent.MarkAsReadFailure)
                println("❌ Błąd oznaczania jako przeczytane: ${e.message}")
            }
        }
    }
}
