package com.example.auctionsapp.notifications.presentation

import com.example.auctionsapp.notifications.domain.Notification

data class NotificationsState(
    val notifications: List<Notification> = emptyList(),
    val isLoading: Boolean = false,
    val page: Int = 0,
    val hasMore: Boolean = true,
    val currentUserId: String? = null
)