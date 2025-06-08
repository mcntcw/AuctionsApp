package com.example.auctionsapp

import android.app.Application
import com.example.auctionsapp.auction_details.di.auctionDetailsModule
import com.example.auctionsapp.auction_form.di.auctionFormModule
import com.example.auctionsapp.auctions_list.di.auctionsListModule
import com.example.auctionsapp.authentication.di.authenticationModule
import com.example.auctionsapp.core.di.auctionModule
import com.example.auctionsapp.core.di.storageModule
import com.example.auctionsapp.core.di.userModule
import com.example.auctionsapp.overview.di.overviewModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(
                authenticationModule,
                userModule,
                auctionModule,
                overviewModule,
                auctionDetailsModule,
                auctionFormModule,
                storageModule,
                auctionsListModule,
            )
        }
    }


}