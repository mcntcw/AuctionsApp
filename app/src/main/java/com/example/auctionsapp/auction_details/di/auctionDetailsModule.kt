package com.example.auctionsapp.auction_details.di

import com.example.auctionsapp.overview.presentation.AuctionDetailsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val auctionDetailsModule = module {
    viewModel { params -> AuctionDetailsViewModel(get(), get(), get(), savedStateHandle = params.get(),) }
}