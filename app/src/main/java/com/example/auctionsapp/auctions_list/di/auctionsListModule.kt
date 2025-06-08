package com.example.auctionsapp.auctions_list.di

import com.example.auctionsapp.auctions_list.presentation.AuctionsListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val auctionsListModule = module {
    viewModel { params -> AuctionsListViewModel(get(), get(), get(), savedStateHandle = params.get(),) }
}