package com.example.auctionsapp.auction_details.domain

interface AuctionService {
    suspend fun placeBid(auctionId: String, bidderId: String, amount: Double)
    suspend fun buyNow(auctionId: String, buyerId: String)
}