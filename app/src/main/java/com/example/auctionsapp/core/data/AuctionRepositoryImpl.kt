package com.example.auctionsapp.core.data

import com.example.auctionsapp.core.domain.Auction
import com.example.auctionsapp.core.domain.AuctionRepository

class AuctionRepositoryImpl(
    private val supabaseAuctionDataSource: SupabaseAuctionDataSource
): AuctionRepository {
    override suspend fun getAllAuctions(): List<Auction> {
        return supabaseAuctionDataSource.getAllAuctions()
    }

    override suspend fun getLatestAuctions(limit: Long): List<Auction> {
        return supabaseAuctionDataSource.getLatestAuctions(limit)
    }

    override suspend fun getAuctionById(id: String): Auction? {
        return supabaseAuctionDataSource.getAuctionById(id)
    }

    override suspend fun upsertAuction(auction: Auction): Auction? {
        return supabaseAuctionDataSource.upsertAuction(auction)
    }

    override suspend fun cancelAuction(auctionId: String){
        return supabaseAuctionDataSource.cancelAuction(auctionId)
    }

    override suspend fun placeBid(auctionId: String, bidderId: String, amount: Double) {
        return supabaseAuctionDataSource.placeBid(auctionId, bidderId, amount)
    }

    override suspend fun buyNow(auctionId: String, buyerId: String) {
        return supabaseAuctionDataSource.buyNow(auctionId, buyerId)
    }

    override suspend fun getAuctionsByCategoryPaged(category: String, limit: Int, offset: Int): List<Auction> {
        return supabaseAuctionDataSource.getAuctionsByCategoryPaged(category, limit, offset)
    }

    override suspend fun getAllAuctionsFromUserPaged(userId: String, limit: Int, offset: Int): List<Auction> {
        return supabaseAuctionDataSource.getAllAuctionsFromUserPaged(userId, limit, offset)
    }

    override suspend fun getAllAuctionsUserParticipated(userId: String, limit: Int, offset: Int): List<Auction> {
        return supabaseAuctionDataSource.getAllAuctionsUserParticipated(userId, limit, offset)
    }


    override suspend fun getAllAuctionsFromSearchPaged(query: String, limit: Int, offset: Int): List<Auction> {
        return supabaseAuctionDataSource.getAllAuctionsFromSearchPaged(query, limit, offset)
    }

}