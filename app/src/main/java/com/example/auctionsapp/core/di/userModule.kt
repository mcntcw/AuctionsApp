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
//    single<Postgrest> { get<io.github.jan.supabase.SupabaseClient>().pluginManager.getPlugin(Postgrest) }
//
//    single {
//        createSupabaseClient(
//            supabaseUrl = "https://vtncymngmnzsamnbboiy.supabase.co",
//            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InZ0bmN5bW5nbW56c2FtbmJib2l5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzE1ODg2NzcsImV4cCI6MjA0NzE2NDY3N30.EYir6FewEQ3--rCa9wSfn54Rs8F5UlyxSehO8YsMmPc"
//        ) {
//            install(Auth)
//            install(Postgrest)
//        }
//    }
    single { SupabaseUserDataSource(get()) }
    singleOf(::UserRepositoryImpl).bind<UserRepository>()
    single { CreateUserUseCase(get()) }
}