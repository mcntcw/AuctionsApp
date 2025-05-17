package com.example.auctionsapp.core.di

import com.example.auctionsapp.core.data.storage.StorageService
import com.example.auctionsapp.core.data.storage.SupabaseStorageService
import org.koin.dsl.module

val storageModule = module {
    single<StorageService> { SupabaseStorageService(get()) }
}
