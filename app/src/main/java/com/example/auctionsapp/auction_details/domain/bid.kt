package com.example.auctionsapp.auction_details.domain

import com.example.auctionsapp.core.domain.User
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Bid(
    val id: String?,
    @SerialName("auction_id") val auctionId: String,
    val bidder: User,
    val amount: Double,
    @SerialName("placed_at") val placedAt: Instant? = null,
) {
    companion object {
        fun empty(): Bid {
            return Bid(
                id = "",
                auctionId = "",
                bidder = User.empty(),
                amount = 0.0,
                placedAt = Instant.DISTANT_PAST
            )
        }
    }
}
@Serializable
data class BidRaw(
    val id: String? = null,
    @SerialName("auction_id") val auctionId: String,
    @SerialName("bidder_id") val bidderId: String,
    val amount: Double,
    @SerialName("placed_at") val placedAt: Instant? = null,
) {
    fun toBid(bidder: User): Bid {
        return Bid(
            id = this.id,
            auctionId = this.auctionId,
            bidder = bidder,
            amount = this.amount,
            placedAt = this.placedAt
        )
    }
}