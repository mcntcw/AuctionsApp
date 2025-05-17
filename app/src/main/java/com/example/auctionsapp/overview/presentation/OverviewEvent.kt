package com.example.auctionsapp.overview.presentation

sealed interface OverviewEvent {


    data object GetUserInfoSuccess: OverviewEvent
    data object GetUserInfoFailure: OverviewEvent


    data object GetLatestAuctionsSuccess: OverviewEvent
    data object GetLatsetAuctionsFailure: OverviewEvent


    data object SignOutSuccess: OverviewEvent
    data object SignOutFailure: OverviewEvent
}