package com.example.auctionsapp.core.data

import com.example.auctionsapp.core.domain.User
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from


class SupabaseUserDataSource(private val supabase: SupabaseClient) {

    suspend fun createUser(user: User) {
        try {
            supabase.from("users").upsert(user)
        } catch (e: Exception) {
            println("Błąd podczas dodawania użytkownika: ${e.message}")
            throw e
        }
    }

    suspend fun getUserById(id: String): User? {
        try {
          val result = supabase.from("users").select() {
                filter {
                    eq("id", id)
                }
            }
//            println("REZULTAT JSON: ${result.data}")
            try {
                val user = result.decodeSingle<User>()
//                println("REZULTAT POBRANIA USERA Z SUPABASE ${user}")
                return user
            } catch (e: Exception) {
                println(e.message)
                return null
            }
DEFAULT_BUFFER_SIZE




        } catch (e: Exception) {
            return User.empty()
            println("Błąd podczas pobierania użytkownika: ${e.message}")
            throw e
        }

    }
}

