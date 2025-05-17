package com.example.auctionsapp.core.data.storage

interface StorageService {
    suspend fun upload(key: String, data: ByteArray): String  // zwraca publiczny URL
    suspend fun delete(key: String): Boolean
}
