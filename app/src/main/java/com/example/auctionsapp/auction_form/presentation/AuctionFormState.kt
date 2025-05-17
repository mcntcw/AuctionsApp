package com.example.auctionsapp.auction_form.presentation

import com.example.auctionsapp.core.domain.Auction

data class AuctionFormState (
    val auction: Auction = Auction.empty(),
//    val title: String = "",
//    val description: String? = null,
//    val galleryUrls: List<String> = emptyList(),
//    val phoneNumber: String = "",
//    val currentPrice: Double = 0.0,
//    val buyNowPrice: Double = 0.0,
//    val endTime: Instant = Instant.DISTANT_FUTURE,
    val isEditMode: Boolean = false,
    val isSaving: Boolean = false,
    val isLoading: Boolean = false,
)