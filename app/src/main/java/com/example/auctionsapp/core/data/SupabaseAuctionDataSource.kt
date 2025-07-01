package com.example.auctionsapp.core.data

import android.content.Context
import androidx.core.net.toUri
import com.example.auctionsapp.auction_details.domain.Bid
import com.example.auctionsapp.auction_details.domain.BidRaw
import com.example.auctionsapp.authentication.domain.AuthenticationRepository
import com.example.auctionsapp.core.data.storage.StorageService
import com.example.auctionsapp.core.domain.Auction
import com.example.auctionsapp.core.domain.AuctionRaw
import com.example.auctionsapp.core.domain.User
import com.example.auctionsapp.core.domain.UserRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.TextSearchType
import java.util.UUID


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

    suspend fun getLatestAuctions(limit: Long): List<Auction> {
        return try {
            val result = supabase.from("auctions")
                .select {
                    filter { eq("status", "active") }
                    order("created_at", order = Order.DESCENDING)
                    limit(limit)
                }

            val auctionsWithIds = result.decodeList<AuctionRaw>()

            auctionsWithIds.map { auctionRaw ->
                try {
                    val sellerUser = userRepository.getUserById(auctionRaw.sellerId) ?: User.empty()
                    val bids = getBidsForAuction(auctionRaw.id ?: "")
                    auctionRaw.toAuction(sellerUser, bids = bids)
                } catch (e: Exception) {
                    println("Błąd pobierania sprzedawcy lub bidów dla aukcji ${auctionRaw.id}: ${e.message}")
                    auctionRaw.toAuction(User.empty(), bids = emptyList())
                }
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
            val sellerUser = userRepository.getUserById(auctionRaw.sellerId)?.let { user ->
                user.copy(name = cleanUserName(user.name))
            } ?: User.empty()



            val bids = getBidsForAuction(auctionId)
            auctionRaw.toAuction(sellerUser, bids = bids)
        } catch (e: Exception) {
            println("Błąd podczas pobierania aukcji o id $auctionId: ${e.message}")
            null
        }
    }


    private suspend fun getBidsForAuction(auctionId: String): List<Bid> {
        return try {
            val bidsRaw = supabase.from("bids").select {
                filter {
                    eq("auction_id", auctionId)
                }
            }.decodeList<BidRaw>()

            bidsRaw.map { bidRaw ->
                val bidder = userRepository.getUserById(bidRaw.bidderId)?.let { user ->
                    user.copy(name = cleanUserName(user.name))
                } ?: User.empty()

                bidRaw.toBid(bidder)
            }
        } catch (e: Exception) {
            println("Błąd podczas pobierania bidów dla aukcji $auctionId: ${e.message}")
            emptyList()
        }
    }

    private fun cleanUserName(name: String): String {
        return name.replace("\"", "")
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

                status = auction.status.name.lowercase(),
                category = auction.category.name.lowercase(),
                title = auction.title,
                description = auction.description,
                galleryUrls = finalGalleryUrls,
                sellerId = sellerId,
                phoneNumber = auction.phoneNumber,

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
                val uri = localUri.toUri()
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes == null) {
                    println("Nie udało się odczytać bajtów z URI: $localUri")
                }
                if (bytes != null) {
                    val fileName = "public/${UUID.randomUUID()}.jpg"
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

    suspend fun cancelAuction(auctionId: String) {
        try {
            supabase.from("auctions")
                .update(
                    mapOf(
                        "status" to "cancelled"
                    )
                ) {
                    filter { eq("id", auctionId) }
                }
        } catch (e: Exception) {
            println("Błąd podczas buyNow: ${e.message}")
        }
    }

    suspend fun placeBid(
        auctionId: String,
        bidderId: String,
        amount: Double
    ) {
        try {
            val newBid = BidRaw(

                auctionId = auctionId,
                bidderId = bidderId,
                amount = amount,

            )

            supabase.from("bids").insert(newBid)
        } catch (e: Exception) {
            println("Błąd podczas placeBid: ${e.message}")
        }
    }

    suspend fun buyNow(auctionId: String, buyerId: String) {
        try {
            supabase.from("auctions")
                .update(
                    mapOf(
                        "buyer_id" to buyerId,
                        "status" to "sold"
                    )
                ) {
                    filter { eq("id", auctionId) }
                }
        } catch (e: Exception) {
            println("Błąd podczas buyNow: ${e.message}")
        }
    }

    suspend fun getAuctionsByCategoryPaged(
        category: String,
        limit: Int = 20,
        offset: Int = 0
    ): List<Auction> {
        println("PAGINACJA: category=$category, offset=$offset, limit=$limit")
        return try {
            val result = supabase.from("auctions")
                .select {
                    filter { eq("category", category) }
                    order("created_at", order = Order.DESCENDING)
                    range(offset.toLong(), (offset + limit - 1).toLong())
                }
            val auctionsRaw = result.decodeList<AuctionRaw>()
            println("PAGINACJA: pobrano ${auctionsRaw.size} rekordów")
            auctionsRaw.map { auctionRaw ->
                val sellerUser = userRepository.getUserById(auctionRaw.sellerId) ?: User.empty()
                val bids = getBidsForAuction(auctionRaw.id ?: "")
                auctionRaw.toAuction(sellerUser, bids = bids)
            }
        } catch (e: Exception) {
            println("Błąd podczas paginacji aukcji po kategorii: ${e.message}")
            emptyList()
        }
    }

    suspend fun getAllAuctionsFromUserPaged(
        userId: String,
        limit: Int = 20,
        offset: Int = 0
    ): List<Auction> {
        println("PAGINACJA USER: userId=$userId, offset=$offset, limit=$limit")
        return try {
            val result = supabase.from("auctions")
                .select {
                    filter { eq("seller_id", userId) }
                    order("created_at", order = Order.DESCENDING)
                    range(offset.toLong(), (offset + limit - 1).toLong())
                }
            val auctionsRaw = result.decodeList<AuctionRaw>()
            println("PAGINACJA USER: pobrano ${auctionsRaw.size} rekordów dla userId=$userId")

            auctionsRaw.map { auctionRaw ->
                val sellerUser = userRepository.getUserById(auctionRaw.sellerId) ?: User.empty()
                val bids = getBidsForAuction(auctionRaw.id ?: "")
                auctionRaw.toAuction(sellerUser, bids = bids)
            }
        } catch (e: Exception) {
            println("Błąd podczas pobierania aukcji użytkownika $userId: ${e.message}")
            emptyList()
        }
    }

    suspend fun getAllAuctionsFromSearchPaged(
        query: String,
        limit: Int = 20,
        offset: Int = 0
    ): List<Auction> {
        println("PAGINACJA SEARCH: query=$query, offset=$offset, limit=$limit")
        return try {
            val result = supabase.from("auctions")
                .select {
                    filter {
                        and {
                            eq("status", "active") 
                            or {
                                textSearch("title", query, TextSearchType.WEBSEARCH)
                                textSearch("description", query, TextSearchType.WEBSEARCH)
                            }
                        }
                    }
                    order("created_at", order = Order.DESCENDING)
                    range(offset.toLong(), (offset + limit - 1).toLong())
                }
            val auctionsRaw = result.decodeList<AuctionRaw>()
            println("PAGINACJA SEARCH: pobrano ${auctionsRaw.size} rekordów")
            auctionsRaw.map { auctionRaw ->
                val sellerUser = userRepository.getUserById(auctionRaw.sellerId) ?: User.empty()
                val bids = getBidsForAuction(auctionRaw.id ?: "")
                auctionRaw.toAuction(sellerUser, bids = bids)
            }
        } catch (e: Exception) {
            println("Błąd podczas paginacji aukcji po wyszukiwaniu: ${e.message}")
            emptyList()
        }
    }

    suspend fun getAllAuctionsUserParticipated(
        userId: String,
        limit: Int = 20,
        offset: Int = 0
    ): List<Auction> {
        println("PAGINACJA USER BIDS: userId=$userId, offset=$offset, limit=$limit")
        return try {
            
            val bidsResult = supabase.from("bids")
                .select(columns = Columns.list("auction_id")) {
                    filter { eq("bidder_id", userId) }
                }

            val auctionIds = bidsResult.decodeList<Map<String, String>>()
                .mapNotNull { it["auction_id"] }
                .distinct()

            if (auctionIds.isEmpty()) {
                println("PAGINACJA USER BIDS: użytkownik nie licytował w żadnej aukcji")
                return emptyList()
            }

            
            val result = supabase.from("auctions")
                .select {
                    filter {
                        isIn("id", auctionIds)
                    }
                    order("created_at", order = Order.DESCENDING)
                    range(offset.toLong(), (offset + limit - 1).toLong())
                }


            val auctionsRaw = result.decodeList<AuctionRaw>()
            println("PAGINACJA USER BIDS: pobrano ${auctionsRaw.size} rekordów")

            auctionsRaw.map { auctionRaw ->
                val sellerUser = userRepository.getUserById(auctionRaw.sellerId) ?: User.empty()
                val bids = getBidsForAuction(auctionRaw.id ?: "")
                auctionRaw.toAuction(sellerUser, bids = bids)
            }
        } catch (e: Exception) {
            println("Błąd podczas pobierania aukcji z licytacjami użytkownika $userId: ${e.message}")
            emptyList()
        }
    }








}