package com.example.auctionsapp.core.domain

interface AuctionRepository {
    suspend fun getAllAuctions(): List<Auction>
    suspend fun getLatestAuctions(limit: Long): List<Auction>
    suspend fun getAuctionById(id: String): Auction?
    suspend fun upsertAuction(auction: Auction): Auction?
    suspend fun cancelAuction(auctionId: String)
    suspend fun placeBid(auctionId: String, bidderId: String, amount: Double)
    suspend fun buyNow(auctionId: String, buyerId: String)
    suspend fun getAuctionsByCategoryPaged(category: String, limit: Int, offset: Int): List<Auction>
    suspend fun getAllAuctionsFromUserPaged(userId: String, limit: Int, offset: Int) : List<Auction>
    suspend fun getAllAuctionsUserParticipated(userId: String, limit: Int, offset: Int) : List<Auction>
    suspend fun getAllAuctionsFromSearchPaged(query: String, limit: Int, offset: Int) : List<Auction>
}
