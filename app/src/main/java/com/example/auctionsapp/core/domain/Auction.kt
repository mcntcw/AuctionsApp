package com.example.auctionsapp.core.domain

import com.example.auctionsapp.auction_details.domain.Bid
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AuctionStatus {
    @SerialName("active") ACTIVE,
    @SerialName("sold") SOLD,
    @SerialName("ended") ENDED,
    @SerialName("cancelled") CANCELLED;

    companion object {
        fun fromString(value: String?): AuctionStatus =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: ACTIVE
    }
}

@Serializable
data class Auction(
    val id: String?,
    @SerialName("status") val status: AuctionStatus,
    @SerialName("category") val category: AuctionCategory,
    val title: String,
    val description: String?,
    @SerialName("gallery_urls") val galleryUrls: List<String>,
    val seller: User,
    @SerialName("phone_number") val phoneNumber: String,
    val bids: List<Bid>,
    @SerialName("buy_now_price") val buyNowPrice: Double,
    val buyer: User?,
    @SerialName("created_at") val createdAt: Instant? = null,
    @SerialName("end_time") val endTime: Instant
) {
    companion object {
        fun empty(): Auction {
            return Auction(
                id = "",
                status = AuctionStatus.ACTIVE,
                category = AuctionCategory.OTHER,
                title = "",
                description = "",
                galleryUrls = emptyList(),
                seller = User.empty(),
                phoneNumber = "",
                bids = emptyList(),
                buyNowPrice = 0.0,
                buyer = null,
                createdAt = Instant.DISTANT_PAST,
                endTime = Instant.DISTANT_FUTURE
            )
        }
    }
}

@Serializable
data class AuctionRaw(
    val id: String? = null,
    val status: String,
    val category: String,
    val title: String,
    val description: String?,
    @SerialName("gallery_urls") val galleryUrls: List<String>,
    @SerialName("seller_id") val sellerId: String,
    @SerialName("phone_number") val phoneNumber: String,
//    @SerialName("bids") val bids: List<String>,
    @SerialName("buy_now_price") val buyNowPrice: Double,
    @SerialName("buyer_id") val buyerId: String?,
    @SerialName("created_at") val createdAt: Instant? = null,
    @SerialName("end_time") val endTime: Instant
) {
    fun toAuction(
        seller: User,
        buyer: User? = null,
        bids: List<Bid> = emptyList()
    ): Auction {
        return Auction(
            id = this.id,
            status = AuctionStatus.fromString(this.status),
            category = AuctionCategory.fromString(this.category),
            title = this.title,
            description = this.description,
            galleryUrls = this.galleryUrls,
            seller = seller,
            phoneNumber = this.phoneNumber,
            bids = bids,
            buyNowPrice = this.buyNowPrice,
            buyer = buyer,
            createdAt = this.createdAt,
            endTime = this.endTime
        )
    }
}

