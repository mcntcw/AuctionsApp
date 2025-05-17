package com.example.auctionsapp.core.data

import com.example.auctionsapp.core.domain.Auction
import com.example.auctionsapp.core.domain.AuctionRepository

class AuctionRepositoryImpl(
    private val supabaseAuctionDataSource: SupabaseAuctionDataSource
): AuctionRepository {
    override suspend fun getAllAuctions(): List<Auction> {
        return supabaseAuctionDataSource.getAllAuctions()
    }

    override suspend fun getLatestAuctions(): List<Auction> {
        return supabaseAuctionDataSource.getLatestAuctions()
    }

    override suspend fun getAuctionById(id: String): Auction? {
        return supabaseAuctionDataSource.getAuctionById(id)
    }

    override suspend fun upsertAuction(auction: Auction): Auction? {
        return supabaseAuctionDataSource.upsertAuction(auction)
    }

    override suspend fun deleteAuction(auction: Auction): Auction {
        TODO("Not yet implemented")
    }
}