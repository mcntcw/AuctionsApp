package com.example.auctionsapp.core.domain

interface AuctionRepository {
    suspend fun getAllAuctions(): List<Auction>
    suspend fun getLatestAuctions(): List<Auction>
    suspend fun getAuctionById(id: String): Auction?
    suspend fun upsertAuction(auction: Auction): Auction?
    suspend fun deleteAuction(auction: Auction): Auction
}