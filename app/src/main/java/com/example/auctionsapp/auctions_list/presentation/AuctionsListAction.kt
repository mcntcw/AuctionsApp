package com.example.auctionsapp.auctions_list.presentation


sealed interface AuctionsListAction {
    data object LoadAuctionsByCategory : AuctionsListAction
    data object LoadAllAuctionsFromUser : AuctionsListAction
    data object LoadAllAuctionsUserParticipated : AuctionsListAction
    data object LoadAllAuctionsFromSearchQuery : AuctionsListAction
    data object LoadLatestAuctions : AuctionsListAction


    data object LoadNextPage : AuctionsListAction
}