package com.example.auctionsapp.core.domain

import com.example.auctionsapp.auction_details.domain.Bid
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Auction(
    val id: String?,
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
    val title: String,
    val description: String?,
    @SerialName("gallery_urls") val galleryUrls: List<String>,
    @SerialName("seller_id") val sellerId: String,
    @SerialName("phone_number") val phoneNumber: String,
    @SerialName("bids") val bids: List<String>,
    @SerialName("buy_now_price") val buyNowPrice: Double,
    @SerialName("buyer_id") val buyerId: String?,
    @SerialName("created_at") val createdAt: Instant? = null,
    @SerialName("end_time") val endTime: Instant
)

{
    fun toAuction(seller: User, buyer: User? = null, bids: List<Bid> = emptyList()): Auction {
        return Auction(
            id = this.id,
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

    @Serializable
    enum class AuctionCategory {
        @SerialName("electronics") ELECTRONICS,
        @SerialName("furniture") FURNITURE,
        @SerialName("kitchen") KITCHEN,
        @SerialName("fashion") FASHION,
        @SerialName("sports") SPORTS,
        @SerialName("other") OTHER
    }


}