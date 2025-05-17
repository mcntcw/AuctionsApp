package com.example.auctionsapp.authentication.data.repository

import com.example.auctionsapp.core.data.SupabaseUserDataSource
import com.example.auctionsapp.core.domain.User
import com.example.auctionsapp.core.domain.UserRepository

class UserRepositoryImpl(
    private val supabaseUserDataSource: SupabaseUserDataSource
): UserRepository {
    override suspend fun getUserById(id: String): User? {
       return supabaseUserDataSource.getUserById(id)
    }

    override suspend fun createUser(user: User) {
        supabaseUserDataSource.createUser(user)
    }

    override suspend fun editUser(user: User): User {
        TODO("Not yet implemented")
    }

    override suspend fun deleteUser(user: User): User {
        TODO("Not yet implemented")
    }
}
