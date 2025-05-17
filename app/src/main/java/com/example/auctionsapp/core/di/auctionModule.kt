package com.example.auctionsapp.core.di

import com.example.auctionsapp.core.data.AuctionRepositoryImpl
import com.example.auctionsapp.core.data.SupabaseAuctionDataSource
import com.example.auctionsapp.core.domain.AuctionRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val auctionModule = module {
    single {
        SupabaseAuctionDataSource(
            get(),
            get(),
            get(),
            get(),
            androidContext()
        )
    }
    singleOf(::AuctionRepositoryImpl).bind<AuctionRepository>()
}