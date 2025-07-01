package com.example.auctionsapp.notifications.data

import com.example.auctionsapp.core.domain.AuctionRepository
import com.example.auctionsapp.core.domain.UserRepository
import com.example.auctionsapp.notifications.domain.Notification
import com.example.auctionsapp.notifications.domain.NotificationRaw
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class SupabaseNotificationsDataSource(
    private val supabase: SupabaseClient,
    private val userRepository: UserRepository,
    private val auctionRepository: AuctionRepository
) {

     suspend fun upsertNotification(notification: Notification) {
         try {
             val notificationRaw = NotificationRaw(

                 receiverId = notification.receiver?.id ?: throw IllegalStateException("Brak odbiorcy powiadomienia!"),
                 auctionId = notification.auction?.id ?: throw IllegalStateException("Brak aukcji w powiadomieniu!"),
                 type = notification.type.name.lowercase(),
                 isRead = notification.isRead,
                 createdAt = notification.createdAt.toString(),
                 relatedUserId = notification.relatedUser?.id
             )
             supabase.from("notifications").upsert(notificationRaw)
             println("Powiadomienie utworzone: $notificationRaw.type dla użytkownika $notificationRaw.userId")
         } catch (e: Exception) {
             println("Błąd tworzenia powiadomienia: ${e.message}")
             throw e
         }

    }

    suspend fun getUserNotificationsPaged(userId: String, limit: Int = 20, offset: Int = 0): List<Notification> {
        return try {
            val result = supabase.from("notifications")
                .select {
                    filter { eq("receiver_id", userId) }
                    order("created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    range(offset.toLong(), (offset + limit - 1).toLong())
                }

            val notificationsRaw = result.decodeList<NotificationRaw>()
            println("📱 Pobrano ${notificationsRaw.size} powiadomień dla użytkownika $userId")

            
            notificationsRaw.mapNotNull { notificationRaw ->
                try {
                    val receiver = userRepository.getUserById(notificationRaw.receiverId)
                    val auction = auctionRepository.getAuctionById(notificationRaw.auctionId)
                    val relatedUser = notificationRaw.relatedUserId?.let {
                        userRepository.getUserById(it)
                    }

                    if (receiver != null && auction != null) {
                        notificationRaw.toNotification(
                            receiver = receiver,
                            auction = auction,
                            relatedUser = relatedUser ?: com.example.auctionsapp.core.domain.User.empty()
                        )
                    } else {
                        println("Nie można zmapować powiadomienia ${notificationRaw.id} - brak receiver lub auction")
                        null
                    }
                } catch (e: Exception) {
                    println("Błąd mapowania powiadomienia ${notificationRaw.id}: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            println("Błąd pobierania powiadomień: ${e.message}")
            emptyList()
        }
    }

    suspend fun markAsRead(notificationId: String) {
        try {
            supabase.from("notifications")
                .update({ set("is_read", true) }) {
                    filter { eq("id", notificationId) }
                }
            println("Powiadomienie $notificationId oznaczone jako przeczytane")
        } catch (e: Exception) {
            println("Błąd oznaczania jako przeczytane: ${e.message}")
            throw e
        }
    }
}
