package com.example.auctionsapp.auction_form.di

import com.example.auctionsapp.auction_form.presentation.AuctionFormViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val auctionFormModule = module {
    viewModel { params -> AuctionFormViewModel(get(), get(), get(), savedStateHandle = params.get(),) }
}