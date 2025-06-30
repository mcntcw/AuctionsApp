package com.example.auctionsapp.notifications.presentation

sealed interface NotificationsAction {
    data object LoadNotifications : NotificationsAction
    data object LoadNextPage : NotificationsAction
    data class MarkAsRead(val notificationId: String) : NotificationsAction
}