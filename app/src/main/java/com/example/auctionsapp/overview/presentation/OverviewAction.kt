package com.example.auctionsapp.overview.presentation

sealed interface OverviewAction {
    data object SignOut: OverviewAction
    data object GetUserInfo: OverviewAction
    data object GetLatestAuctions: OverviewAction
    data object CheckUnreadNotifications: OverviewAction
}