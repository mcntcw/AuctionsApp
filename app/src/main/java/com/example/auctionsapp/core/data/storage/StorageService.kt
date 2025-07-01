package com.example.auctionsapp.core.data.storage

interface StorageService {
    suspend fun upload(key: String, data: ByteArray): String  
    suspend fun delete(key: String): Boolean
}
