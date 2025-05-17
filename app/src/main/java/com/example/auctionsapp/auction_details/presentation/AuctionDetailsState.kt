package com.example.auctionsapp.auction_details.presentation

import com.example.auctionsapp.core.domain.Auction

data class AuctionDetailsState(
    val auction: Auction = Auction.empty(),
    val isLoading: Boolean = false,
)
