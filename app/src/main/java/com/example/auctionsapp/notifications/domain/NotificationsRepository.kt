
package com.example.auctionsapp.notifications.domain

interface NotificationRepository {


    suspend fun upsertNotification(notification: Notification)
    suspend fun getUserNotificationsPaged(userId: String, limit: Int = 20, offset: Int = 0): List<Notification>
    suspend fun markAsRead(notificationId: String)
}
