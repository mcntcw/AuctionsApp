package com.example.auctionsapp.core.di

import com.example.auctionsapp.authentication.data.repository.UserRepositoryImpl
import com.example.auctionsapp.authentication.data.AuthenticationRepositoryImpl
import com.example.auctionsapp.authentication.data.SupabaseAuthenticationDataSource
import com.example.auctionsapp.authentication.domain.AuthenticationRepository
import com.example.auctionsapp.core.data.SupabaseUserDataSource
import com.example.auctionsapp.core.domain.CreateUserUseCase
import com.example.auctionsapp.core.domain.UserRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val userModule = module {











    single { SupabaseUserDataSource(get()) }
    singleOf(::UserRepositoryImpl).bind<UserRepository>()
    single { CreateUserUseCase(get()) }
}