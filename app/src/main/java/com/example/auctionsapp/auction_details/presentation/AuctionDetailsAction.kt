package com.example.auctionsapp.auction_details.presentation

sealed interface AuctionDetailsAction {
    data class GetAuctionInfo(val auctionId: String): AuctionDetailsAction
    data class UpdateBidValue(val newValue: Double) : AuctionDetailsAction
    data class PlaceBid(val value: Double) : AuctionDetailsAction
    data object BuyNow: AuctionDetailsAction
    data object CancelAuction: AuctionDetailsAction
}
