package com.example.auctionsapp.core.presentation.util

sealed interface Screen {


        @kotlinx.serialization.Serializable
        data object AuthenticationScreen: Screen

        @kotlinx.serialization.Serializable
        data object OverviewScreen: Screen

        @kotlinx.serialization.Serializable
        data class AuctionDetailsScreen(val auctionId: String) : Screen

}