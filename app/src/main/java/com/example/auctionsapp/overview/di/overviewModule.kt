package com.example.auctionsapp.overview.di

import com.example.auctionsapp.overview.presentation.OverviewViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val overviewModule = module {
    viewModel { OverviewViewModel(get(), get(), get(), get()) }
}