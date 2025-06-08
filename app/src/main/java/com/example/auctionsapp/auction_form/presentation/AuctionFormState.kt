package com.example.auctionsapp.auction_form.presentation

import com.example.auctionsapp.core.domain.Auction

data class AuctionFormState (
    val auction: Auction = Auction.empty(),
    val isEditMode: Boolean = false,
    val isSaving: Boolean = false,
    val isLoading: Boolean = false,
)