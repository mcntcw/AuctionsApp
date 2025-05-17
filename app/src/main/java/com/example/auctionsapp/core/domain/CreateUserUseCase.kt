package com.example.auctionsapp.core.domain

class CreateUserUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(user: User) {
        userRepository.createUser(user)
    }
}