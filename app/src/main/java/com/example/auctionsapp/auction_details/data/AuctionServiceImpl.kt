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
        // 1. Pobierz aktualną aukcję
        val auction = auctionRepository.getAuctionById(auctionId)
            ?: throw Exception("Auction not found")

        // 2. Znajdź poprzedniego najwyższego licytanta
        val previousHighestBidder = auction.bids.maxByOrNull { it.amount }?.bidder?.id

        // 3. Pobierz dane nowego licytanta
        val newBidder = userRepository.getUserById(bidderId)
            ?: throw Exception("Bidder not found")

        // 4. Złóż licytację
        auctionRepository.placeBid(auctionId, bidderId, amount)

        // 5. Stwórz powiadomienie dla poprzedniego licytanta
        if (previousHighestBidder != null && previousHighestBidder != bidderId) {
            val previousBidder = userRepository.getUserById(previousHighestBidder)
            if (previousBidder != null) {
                val outbidNotification = Notification(
                    id = null, // Zostanie wygenerowane przez bazę danych
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

        // 6. Powiadom właściciela aukcji o nowej licytacji
        if (auction.seller.id != bidderId) {
            val newBidNotification = Notification(
                id = null, // Zostanie wygenerowane przez bazę danych
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
        // 1. Pobierz aktualną aukcję
        val auction = auctionRepository.getAuctionById(auctionId)
            ?: throw Exception("Auction not found")

        // 2. Pobierz dane kupującego
        val buyer = userRepository.getUserById(buyerId)
            ?: throw Exception("Buyer not found")

        // 3. Wykonaj zakup natychmiastowy
        auctionRepository.buyNow(auctionId, buyerId)

        // 4. Powiadom sprzedawcę o sprzedaży
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

        // 5. Powiadom wszystkich licytantów, że aukcja została sprzedana
        auction.bids.map { it.bidder.id }.distinct().forEach { bidderId ->
            if (bidderId != buyerId) { // Nie powiadamiaj kupującego
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
