package com.example.auctionsapp.core.domain

interface UserRepository {
    suspend fun getUserById(id: String): User?
    suspend fun createUser(user: User)
    suspend fun editUser(user: User): User
    suspend fun deleteUser(user: User): User

}