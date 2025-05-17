package com.example.auctionsapp.core.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class  User(
    val id: String?,
    val email: String,
    val name: String,
    @SerialName("profile_picture_url") val profilePictureUrl: String?,
    val auctions: List<Auction> = emptyList<Auction>(),
    val biddings: List<Auction> = emptyList<Auction>(),
) {
    companion object {
        fun empty(): User {
            return User(
                id = null,
                email = "",
                name = "",
                profilePictureUrl = null,
            )
        }
    }
}
