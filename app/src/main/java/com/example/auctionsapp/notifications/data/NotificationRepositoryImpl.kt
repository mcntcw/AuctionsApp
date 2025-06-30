package com.example.auctionsapp.notifications.data

import com.example.auctionsapp.notifications.domain.Notification
import com.example.auctionsapp.notifications.domain.NotificationRepository

class NotificationRepositoryImpl(
    private val supabaseNotificationsDataSource: SupabaseNotificationsDataSource
): NotificationRepository {
    override suspend fun upsertNotification(notification: Notification) {
        return supabaseNotificationsDataSource.upsertNotification(notification)
    }
    override suspend fun getUserNotificationsPaged(userId: String, limit: Int, offset: Int): List<Notification> {
        return supabaseNotificationsDataSource.getUserNotificationsPaged(userId, limit, offset)
    }
    override suspend fun markAsRead(notificationId: String) {
        return supabaseNotificationsDataSource.markAsRead(notificationId)
    }
}