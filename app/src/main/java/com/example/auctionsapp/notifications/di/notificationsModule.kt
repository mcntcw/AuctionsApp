package com.example.auctionsapp.notifications.di

import com.example.auctionsapp.notifications.data.NotificationRepositoryImpl
import com.example.auctionsapp.notifications.data.SupabaseNotificationsDataSource
import com.example.auctionsapp.notifications.domain.NotificationRepository
import com.example.auctionsapp.notifications.presentation.NotificationsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val notificationsModule = module {
    single { SupabaseNotificationsDataSource(get(), get(), get()) }
    single<NotificationRepository> { NotificationRepositoryImpl(get()) }
    viewModel { params -> NotificationsViewModel(get(), get()) }
}