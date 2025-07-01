

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
            supabaseUrl = "SUPABASE_URL",
            supabaseKey = "SUPABASE_KEY"
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