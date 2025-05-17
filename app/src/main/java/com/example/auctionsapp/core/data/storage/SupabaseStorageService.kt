package com.example.auctionsapp.core.data.storage

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import java.io.IOException

class SupabaseStorageService(
    private val supabase: SupabaseClient
) : StorageService {

    private val bucket = supabase.storage.from(StorageConfig.BUCKET_AUCTIONS)


    override suspend fun upload(key: String, data: ByteArray): String {
        try {
            bucket.upload(key, data)
        } catch (e: Exception) {
            throw IOException("Upload failed: ${e.message}", e)
        }
        return bucket.publicUrl(key)
    }

    override suspend fun delete(key: String): Boolean {
        return try {
            val res = bucket.delete(listOf(key))
            true
        } catch (_: Exception) {
            false
        }
    }
}
