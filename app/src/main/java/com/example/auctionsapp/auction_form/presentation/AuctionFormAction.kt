package com.example.auctionsapp.auction_form.presentation

import com.example.auctionsapp.core.domain.Auction


sealed interface AuctionFormAction {
    data class GetAuctionInfo(val auctionId: String): AuctionFormAction
    data class UpdateAuctionField(val newAuction: Auction) : AuctionFormAction






    data object SaveAuction : AuctionFormAction
}