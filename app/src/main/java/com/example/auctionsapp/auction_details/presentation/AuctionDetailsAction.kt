package com.example.auctionsapp.auction_details.presentation

sealed interface AuctionDetailsAction {
    data class GetAuctionInfo(val auctionId: String): AuctionDetailsAction
}