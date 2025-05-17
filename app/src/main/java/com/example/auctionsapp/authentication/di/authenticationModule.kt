

package com.example.auctionsapp.authentication.di


import com.example.auctionsapp.authentication.data.AuthenticationRepositoryImpl
import com.example.auctionsapp.authentication.data.GoogleAuthenticationDataSource
import com.example.auctionsapp.authentication.data.SupabaseAuthenticationDataSource
import com.example.auctionsapp.authentication.domain.AuthenticationRepository
import com.example.auctionsapp.authentication.presentation.AuthenticationViewModel
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val authenticationModule = module {
    single { get<Postgrest>() }

    single {
        createSupabaseClient(
            supabaseUrl = "https://vtncymngmnzsamnbboiy.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InZ0bmN5bW5nbW56c2FtbmJib2l5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzE1ODg2NzcsImV4cCI6MjA0NzE2NDY3N30.EYir6FewEQ3--rCa9wSfn54Rs8F5UlyxSehO8YsMmPc"
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
        }
    }

    single { GoogleAuthenticationDataSource(get()) }
    single { SupabaseAuthenticationDataSource(get()) }
    singleOf(::AuthenticationRepositoryImpl).bind<AuthenticationRepository>()
    viewModel { AuthenticationViewModel(get(), get()) }


}