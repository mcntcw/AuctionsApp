package com.example.auctionsapp.auction_details.data

import com.example.auctionsapp.auction_details.domain.AuctionService
import com.example.auctionsapp.core.domain.AuctionRepository
import com.example.auctionsapp.core.domain.UserRepository
import com.example.auctionsapp.notifications.domain.Notification
import com.example.auctionsapp.notifications.domain.NotificationRepository
import com.example.auctionsapp.notifications.domain.NotificationType
import kotlinx.datetime.Clock

class AuctionServiceImpl(
    private val auctionRepository: AuctionRepository,
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository
) : AuctionService {

    override suspend fun placeBid(auctionId: String, bidderId: String, amount: Double) {
        
        val auction = auctionRepository.getAuctionById(auctionId)
            ?: throw Exception("Auction not found")

        
        val previousHighestBidder = auction.bids.maxByOrNull { it.amount }?.bidder?.id

        
        val newBidder = userRepository.getUserById(bidderId)
            ?: throw Exception("Bidder not found")

        
        auctionRepository.placeBid(auctionId, bidderId, amount)

        
        if (previousHighestBidder != null && previousHighestBidder != bidderId) {
            val previousBidder = userRepository.getUserById(previousHighestBidder)
            if (previousBidder != null) {
                val outbidNotification = Notification(
                    id = null, 
                    receiver = previousBidder,
                    auction = auction,
                    type = NotificationType.BID_OUTBID,
                    isRead = false,
                    createdAt = Clock.System.now(),
                    relatedUser = newBidder
                )
                notificationRepository.upsertNotification(outbidNotification)
            }
        }

        
        if (auction.seller.id != bidderId) {
            val newBidNotification = Notification(
                id = null, 
                receiver = auction.seller,
                auction = auction,
                type = NotificationType.MY_AUCTION_NEW_BID,
                isRead = false,
                createdAt = Clock.System.now(),
                relatedUser = newBidder
            )
            notificationRepository.upsertNotification(newBidNotification)
        }
    }

    override suspend fun buyNow(auctionId: String, buyerId: String) {
        
        val auction = auctionRepository.getAuctionById(auctionId)
            ?: throw Exception("Auction not found")

        
        val buyer = userRepository.getUserById(buyerId)
            ?: throw Exception("Buyer not found")

        
        auctionRepository.buyNow(auctionId, buyerId)

        
        val sellerNotification = Notification(
            id = null,
            receiver = auction.seller,
            auction = auction,
            type = NotificationType.MY_AUCTION_SOLD,
            isRead = false,
            createdAt = Clock.System.now(),
            relatedUser = buyer
        )
        notificationRepository.upsertNotification(sellerNotification)

        
        auction.bids.map { it.bidder.id }.distinct().forEach { bidderId ->
            if (bidderId != buyerId) { 
                val bidder = bidderId?.let { userRepository.getUserById(it) }
                if (bidder != null) {
                    val bidderNotification = Notification(
                        id = null,
                        receiver = bidder,
                        auction = auction,
                        type = NotificationType.AUCTION_SOLD,
                        isRead = false,
                        createdAt = Clock.System.now(),
                        relatedUser = buyer
                    )
                    notificationRepository.upsertNotification(bidderNotification)
                }
            }
        }
    }
}
