package com.example.auctionsapp.auction_form.presentation


sealed interface AuctionFormEvent {

    data class ShowValidationToast(val message: String) : AuctionFormEvent

    data object GetAuctionInfoSuccess: AuctionFormEvent
    data object GetAuctionInfoFailure: AuctionFormEvent

    data class SaveAuctionSuccess(val auctionId: String): AuctionFormEvent
    data object SaveAuctionFailure: AuctionFormEvent
}