package com.example.auctionsapp.overview.presentation

import com.example.auctionsapp.core.domain.Auction
import com.example.auctionsapp.core.domain.User

data class OverviewState (
    val user: User = User.empty(),
    val latestAuctions: List<Auction> = emptyList(),
    val isLatestAuctionsLoading: Boolean = false,
)

