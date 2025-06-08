package com.example.auctionsapp.auction_details.presentation

import com.example.auctionsapp.core.domain.Auction

data class AuctionDetailsState(
    val auction: Auction = Auction.empty(),
    val currentUserId: String? = null,
    val isLoading: Boolean = false,
    val bidValue: Double = 0.0,
)
