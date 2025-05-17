package com.example.auctionsapp.auction_form.presentation

import com.example.auctionsapp.core.domain.Auction


sealed interface AuctionFormAction {
    data class GetAuctionInfo(val auctionId: String): AuctionFormAction
    data class UpdateAuctionField(val newAuction: Auction) : AuctionFormAction
//    data class UpdateTitle(val newTitle: String) : AuctionFormAction
//    data class UpdateDescription(val newDescription: String?) : AuctionFormAction
//    data class UpdateGalleryUrls(val newGalleryUrls: List<String>) : AuctionFormAction
//    data class UpdatePhoneNumber(val newPhoneNumber: String) : AuctionFormAction
//    data class UpdateBuyNowPrice(val newBuyNowPrice: Double) : AuctionFormAction
//    data class UpdateEndTime(val newEndTime: Instant) : AuctionFormAction
    data object SaveAuction : AuctionFormAction
}