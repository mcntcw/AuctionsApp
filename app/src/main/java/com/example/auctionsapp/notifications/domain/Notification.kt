package com.example.auctionsapp.notifications.domain

import com.example.auctionsapp.core.domain.Auction
import com.example.auctionsapp.core.domain.User
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


enum class NotificationType {
    MY_AUCTION_NEW_BID,
    MY_AUCTION_SOLD,
    MY_AUCTION_ENDED,
    BID_OUTBID,
    AUCTION_WON,
    AUCTION_ENDED,
    AUCTION_SOLD,
    OTHER;

    companion object {
        fun fromString(value: String?): NotificationType =
            NotificationType.entries.find { it.name.equals(value, ignoreCase = true) } ?: OTHER
    }
}

data class Notification(
    val id: String?,
    val receiver: User?,
    val auction: Auction?,
    val type: NotificationType,
    val isRead: Boolean,
    val createdAt: Instant,
    val relatedUser: User?,
) {
    val message: String
        get() = when (type) {
            NotificationType.MY_AUCTION_NEW_BID ->
                "New bid on your auction ${auction?.title}"
            NotificationType.MY_AUCTION_SOLD ->
                "Your auction ${auction?.title} has been sold"
            NotificationType.MY_AUCTION_ENDED ->
                "Your auction ${auction?.title} has ended"
            NotificationType.BID_OUTBID ->
                "Your bid on ${auction?.title} has been outbid"
            NotificationType.AUCTION_WON ->
                "Congratulations! You won the auction ${auction?.title}"
            NotificationType.AUCTION_ENDED ->
                "Auction ${auction?.title} you participated in has ended"
            NotificationType.AUCTION_SOLD ->
                "Auction ${auction?.title} was sold to another bidder"
            NotificationType.OTHER ->
                "Other"
        }
}

@Serializable
data class NotificationRaw(
    val id: String? = null,
    @SerialName("receiver_id") val receiverId: String,
    @SerialName("auction_id") val auctionId: String,
    val type: String,
    @SerialName("is_read") val isRead: Boolean,
    @SerialName("created_at") val createdAt: String,
    @SerialName("related_user_id") val relatedUserId: String? = null,
) {
    fun toNotification(
        receiver: User,
        auction: Auction,
        relatedUser: User,
    ): Notification {
        return Notification(
            id = this.id,
            receiver = receiver,
            auction = auction,
            type = NotificationType.fromString(this.type),
            isRead = isRead,
            createdAt = Instant.parse(createdAt),
            relatedUser = relatedUser,
        )
    }
}


