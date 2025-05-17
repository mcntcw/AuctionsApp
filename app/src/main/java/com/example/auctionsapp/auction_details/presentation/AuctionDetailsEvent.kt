package com.example.auctionsapp.auction_details.presentation

sealed interface AuctionDetailsEvent {

    data object GetAuctionInfoSuccess: AuctionDetailsEvent
    data object GetAuctionInfoFailure: AuctionDetailsEvent
}