package com.example.auctionsapp.notifications.presentation


sealed interface NotificationsEvent {
    data object LoadNotificationsSuccess : NotificationsEvent
    data object LoadNotificationsFailure : NotificationsEvent
    data object MarkAsReadSuccess : NotificationsEvent
    data object MarkAsReadFailure : NotificationsEvent
}