package com.example.auctionsapp.auctions_list.presentation

import com.example.auctionsapp.core.domain.Auction

data class AuctionsListState (
    val auctions: List<Auction> = emptyList(),
    val isLoading: Boolean = false,
    val page: Int = 0,
    val hasMore: Boolean = true,
    val category: String? = null,
    val userId: String? = null,
    val query: String? = null,
    val userDisplayName: String? = null
)