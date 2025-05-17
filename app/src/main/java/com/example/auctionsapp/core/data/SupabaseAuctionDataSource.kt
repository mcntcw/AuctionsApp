package com.example.auctionsapp.core.data

import android.content.Context
import com.example.auctionsapp.auction_details.domain.Bid
import com.example.auctionsapp.authentication.domain.AuthenticationRepository
import com.example.auctionsapp.core.data.storage.StorageService
import com.example.auctionsapp.core.domain.Auction
import com.example.auctionsapp.core.domain.AuctionRaw
import com.example.auctionsapp.core.domain.User
import com.example.auctionsapp.core.domain.UserRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class SupabaseAuctionDataSource(
    private val supabase: SupabaseClient,
    private val userRepository: UserRepository,
    private val authenticationRepository: AuthenticationRepository,
    private val storageService: StorageService,
    private val context: Context) {

    suspend fun getAllAuctions(): List<Auction> {
        try {
            val result = supabase.from("auctions").select()
            println("REZULTAT JSON: ${result.data}")
            try {
                val auctionsWithIds = result.decodeList<AuctionRaw>()
                println("REZULTAT AUKCJI PO DEKODOWANIU: $auctionsWithIds")

                val auctions = auctionsWithIds.map { auctionRaw ->
                    try {
                        val sellerUser =
                            userRepository.getUserById(auctionRaw.sellerId) ?: User.empty()
                        val bids = emptyList<Bid>()
                        println("USER KTORY STWORZYL AUKCJE: $sellerUser")
                        auctionRaw.toAuction(sellerUser)
                    } catch (e: Exception) {
                        println("Błąd pobierania sprzedawcy dla aukcji ${auctionRaw.id}: ${e.message}")
                        auctionRaw.toAuction(User.empty())
                    }
                }

                println("REZULTAT POBRANIA AUCTIONS Z SUPABASE $auctions")
                return auctions
            } catch (e: Exception) {
                println("Błąd podczas pobierania i dekodowania aukcji: ${e.message}")
                return emptyList()
            }
        } catch (e: Exception) {
            println("Błąd podczas pobierania użytkownika: ${e.message}")
            return emptyList()
            throw e
        }
    }

    suspend fun getLatestAuctions(): List<Auction> {
        return try {
            val result = supabase.from("auctions")
                .select {
                    order("created_at", order = Order.DESCENDING)
                    limit(count = 20)
                }

            println("REZULTAT JSON: ${result.data}")

            val auctionsWithIds = result.decodeList<AuctionRaw>()
            println("REZULTAT AUKCJI PO DEKODOWANIU: $auctionsWithIds")

            auctionsWithIds.map { auctionRaw ->
                try {
                    val sellerUser = userRepository.getUserById(auctionRaw.sellerId) ?: User.empty()
                    println("USER KTORY STWORZYL AUKCJE: $sellerUser")
                    auctionRaw.toAuction(sellerUser)
                } catch (e: Exception) {
                    println("Błąd pobierania sprzedawcy dla aukcji ${auctionRaw.id}: ${e.message}")
                    auctionRaw.toAuction(User.empty())
                }
            }.also {
                println("REZULTAT POBRANIA AUCTIONS Z SUPABASE $it")
            }
        } catch (e: Exception) {
            println("Błąd podczas pobierania aukcji: ${e.message}")
            emptyList()
        }
    }

    suspend fun getAuctionById(auctionId: String): Auction? {
        return try {
            val result = supabase.from("auctions").select {
                filter {
                    eq("id", auctionId)
                }
            }
            val auctionRaw = result.decodeSingle<AuctionRaw>()
            val sellerUser = userRepository.getUserById(auctionRaw.sellerId) ?: User.empty()
            auctionRaw.toAuction(sellerUser)
        } catch (e: Exception) {
            println("Błąd podczas pobierania aukcji o id $auctionId: ${e.message}")
            null
        }
    }

    suspend fun upsertAuction(auction: Auction): Auction? {
        try {
            println("Start upsertAuction dla aukcji: $auction")
            val currentUser = authenticationRepository.getCurrentUser()
            println("Pobrano currentUser: $currentUser")
            val sellerId =
                currentUser?.id ?: throw IllegalStateException("Brak zalogowanego użytkownika!")

            val oldGalleryUrls = if (!auction.id.isNullOrEmpty()) {
                getAuctionById(auction.id)?.galleryUrls ?: emptyList()
            } else {
                emptyList()
            }

            println("oldGalleryUrls: $oldGalleryUrls")
            println("Nowe galleryUrls do uploadu: ${auction.galleryUrls}")

            val finalGalleryUrls = processGalleryImagesForUpsert(
                oldUrls = oldGalleryUrls,
                newUrls = auction.galleryUrls,
                storageService = storageService,
                context = context
            )
            println("finalGalleryUrls po przetworzeniu: $finalGalleryUrls")

            val auctionRaw = AuctionRaw(
//                id = auction.id,
                title = auction.title,
                description = auction.description,
                galleryUrls = finalGalleryUrls,
                sellerId = sellerId,
                phoneNumber = auction.phoneNumber,
                bids = emptyList(),
                buyNowPrice = auction.buyNowPrice,
                buyerId = auction.buyer?.id,
                endTime = auction.endTime
            )
            println("auctionRaw do upsert: $auctionRaw")

            val result = supabase.from("auctions").upsert(auctionRaw) { select() }
            val updatedAuctionRaw = result.decodeSingle<AuctionRaw>()

            val sellerUser = userRepository.getUserById(updatedAuctionRaw.sellerId) ?: User.empty()
            val buyerUser = updatedAuctionRaw.buyerId?.let { userRepository.getUserById(it) }

            val finalAuction = updatedAuctionRaw.toAuction(sellerUser, buyerUser)
            println("Zwracana aukcja: $finalAuction")
            return finalAuction
        } catch (e: Exception) {
            println("Wyjątek podczas upsertAuction: ${e.message}")
            return null
        }
    }

    private suspend fun processGalleryImagesForUpsert(
        oldUrls: List<String>,
        newUrls: List<String>,
        storageService: StorageService,
        context: Context
    ): List<String> {
        println("Start processGalleryImagesForUpsert")
        val toDelete = oldUrls.filter { it.startsWith("http") && it !in newUrls }
        println("Zdjęcia do usunięcia: $toDelete")
        toDelete.forEach { url ->
            val key = url.substringAfter("/object/public/")
            println("Usuwam plik: $key")
            storageService.delete(key)
        }

        val (remoteUrls, localUris) = newUrls.partition { it.startsWith("http") }
        println("remoteUrls: $remoteUrls")
        println("localUris: $localUris")
        val uploadedUrls = localUris.mapNotNull { localUri ->
            try {
                println("Przetwarzam lokalne URI: $localUri")
                val uri = android.net.Uri.parse(localUri)
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes == null) {
                    println("Nie udało się odczytać bajtów z URI: $localUri")
                }
                if (bytes != null) {
                    val fileName = "public/${java.util.UUID.randomUUID()}.jpg"
                    println("Uploaduję plik: $fileName")
                    val url = storageService.upload(fileName, bytes)
                    println("Upload zakończony, URL: $url")
                    url
                } else null
            } catch (e: Exception) {
                println("Błąd uploadu zdjęcia $localUri: ${e.message}")
                null
            }
        }
        val finalUrls = remoteUrls + uploadedUrls
        println("Zwracam finalne URL-e: $finalUrls")
        return finalUrls
    }
}