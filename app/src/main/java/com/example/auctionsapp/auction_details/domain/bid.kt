package com.example.auctionsapp.auction_details.domain

import com.example.auctionsapp.core.domain.User
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Bid(
    val id: String?,
    val user: User,
    val price: Double,
    @SerialName("placed_at") val placedAt: Instant? = null,
)
