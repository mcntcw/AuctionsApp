package com.example.auctionsapp.auction_details.presentation

sealed interface AuctionDetailsEvent {
    data class ShowValidationToast(val message: String) : AuctionDetailsEvent

    data object GetAuctionInfoSuccess: AuctionDetailsEvent
    data object GetAuctionInfoFailure: AuctionDetailsEvent

    data object PlaceBidSuccess: AuctionDetailsEvent
    data object PlaceBidFailure: AuctionDetailsEvent

    data object BuyNowSuccess: AuctionDetailsEvent
    data object BuyNowFailure: AuctionDetailsEvent

    data object AuctionEndedByTime : AuctionDetailsEvent
}