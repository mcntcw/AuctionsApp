package com.example.auctionsapp.core.domain

import com.example.auctionsapp.auction_details.domain.Bid
import com.example.auctionsapp.core.data.AuctionRepositoryImpl
import com.example.auctionsapp.core.data.SupabaseAuctionDataSource
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertTrue

class AuctionRepositoryTest {

    @MockK
    private lateinit var dataSource: SupabaseAuctionDataSource

    private lateinit var repository: AuctionRepositoryImpl

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        repository = AuctionRepositoryImpl(dataSource)
    }

    companion object {
        private val testSeller = User(
            id = "seller-123",
            name = "Test Seller",
            email = "seller@test.com",
            profilePictureUrl = null
        )

        private val testBuyer = User(
            id = "buyer-456",
            name = "Test Buyer",
            email = "buyer@test.com",
            profilePictureUrl = null
        )

        private val testBids = listOf(
            Bid(
                id = "bid-1",
                auctionId = "auction-123",
                bidder = testSeller,
                amount = 150.0,
                placedAt = Instant.parse("2025-06-30T13:00:00Z")
            )
        )

        
        private val testAuction = Auction(
            id = "auction-123",
            status = AuctionStatus.ACTIVE,
            category = AuctionCategory.ELECTRONICS,
            title = "Test Auction",
            description = "Test Description",
            galleryUrls = listOf("url1", "url2"),
            seller = testSeller,
            phoneNumber = "123456789",
            bids = testBids,
            buyNowPrice = 500.0,
            buyer = testBuyer,
            createdAt = Instant.parse("2025-06-30T12:00:00Z"),
            endTime = Instant.parse("2025-07-01T12:00:00Z")
        )

        
        private val testAuctionWithoutBuyer = testAuction.copy(buyer = null)
        private val testAuctionEnded = testAuction.copy(status = AuctionStatus.ENDED)
        private val testAuctionsList = listOf(testAuction, testAuctionWithoutBuyer)
    }

    @Test
    fun getAuctionById_withValidId_returnsAuction() = runTest {
        
        val auctionId = "auction-123"
        coEvery { dataSource.getAuctionById(auctionId) } returns testAuction

        
        val result = repository.getAuctionById(auctionId)

        
        assertNotNull(result)
        assertEquals(testAuction.id, result?.id)
        assertEquals(testAuction.title, result?.title)
        assertEquals(testSeller, result?.seller)
        assertEquals(testBuyer, result?.buyer)
        assertEquals(testBids, result?.bids)

        coVerify { dataSource.getAuctionById(auctionId) }
    }

    @Test
    fun getAuctionById_withInvalidId_returnsNull() = runTest {
        
        val invalidId = "invalid-id"
        coEvery { dataSource.getAuctionById(invalidId) } returns null

        
        val result = repository.getAuctionById(invalidId)

        
        assertNull(result)
        coVerify { dataSource.getAuctionById(invalidId) }
    }

    @Test
    fun getAuctionById_withException_throwsException() = runTest {
        
        val auctionId = "auction-123"
        coEvery { dataSource.getAuctionById(auctionId) } throws Exception("Network error")

        
        assertThrows<Exception> {
            repository.getAuctionById(auctionId)
        }
        coVerify { dataSource.getAuctionById(auctionId) }
    }

    @Test
    fun getAllAuctions_withValidData_returnsAuctionList() = runTest {
        
        coEvery { dataSource.getAllAuctions() } returns testAuctionsList

        
        val result = repository.getAllAuctions()

        
        assertEquals(2, result.size)
        assertEquals(testAuctionsList, result)
        coVerify { dataSource.getAllAuctions() }
    }

    @Test
    fun getAllAuctions_withEmptyResult_returnsEmptyList() = runTest {
        
        coEvery { dataSource.getAllAuctions() } returns emptyList()

        
        val result = repository.getAllAuctions()

        
        assertTrue(result.isEmpty())
        coVerify { dataSource.getAllAuctions() }
    }

    @Test
    fun getLatestAuctions_withValidLimit_returnsLimitedResults() = runTest {
        
        val limit = 5L
        val limitedList = listOf(testAuction)
        coEvery { dataSource.getLatestAuctions(limit) } returns limitedList

        
        val result = repository.getLatestAuctions(limit)

        
        assertEquals(1, result.size)
        assertEquals(limitedList, result)
        coVerify { dataSource.getLatestAuctions(limit) }
    }

    @Test
    fun getAuctionsByCategoryPaged_withValidParams_returnsPagedResults() = runTest {
        
        val category = "ELECTRONICS"
        val limit = 10
        val offset = 0
        val categoryAuctions = listOf(testAuction)

        coEvery { dataSource.getAuctionsByCategoryPaged(category, limit, offset) } returns categoryAuctions

        
        val result = repository.getAuctionsByCategoryPaged(category, limit, offset)

        
        assertEquals(1, result.size)
        assertEquals(categoryAuctions, result)
        coVerify { dataSource.getAuctionsByCategoryPaged(category, limit, offset) }
    }

    @Test
    fun upsertAuction_withValidAuction_returnsUpdatedAuction() = runTest {
        
        val auctionToUpdate = testAuction.copy(title = "Updated Title")
        coEvery { dataSource.upsertAuction(auctionToUpdate) } returns auctionToUpdate

        
        val result = repository.upsertAuction(auctionToUpdate)

        
        assertEquals(auctionToUpdate, result)
        coVerify { dataSource.upsertAuction(auctionToUpdate) }
    }

    @Test
    fun upsertAuction_withException_returnsNull() = runTest {
        
        coEvery { dataSource.upsertAuction(any()) } returns null

        
        val result = repository.upsertAuction(testAuction)

        
        assertNull(result)
        coVerify { dataSource.upsertAuction(testAuction) }
    }

    @Test
    fun placeBid_withValidParams_callsDataSource() = runTest {
        
        val auctionId = "auction-123"
        val bidderId = "bidder-456"
        val amount = 200.0
        coEvery { dataSource.placeBid(auctionId, bidderId, amount) } returns Unit

        
        repository.placeBid(auctionId, bidderId, amount)

        
        coVerify { dataSource.placeBid(auctionId, bidderId, amount) }
    }

    @Test
    fun buyNow_withValidParams_callsDataSource() = runTest {
        
        val auctionId = "auction-123"
        val buyerId = "buyer-456"
        coEvery { dataSource.buyNow(auctionId, buyerId) } returns Unit

        
        repository.buyNow(auctionId, buyerId)

        
        coVerify { dataSource.buyNow(auctionId, buyerId) }
    }

    @Test
    fun cancelAuction_withValidId_callsDataSource() = runTest {
        
        val auctionId = "auction-123"
        coEvery { dataSource.cancelAuction(auctionId) } returns Unit

        
        repository.cancelAuction(auctionId)

        
        coVerify { dataSource.cancelAuction(auctionId) }
    }

    @Test
    fun getAllAuctionsFromUserPaged_withValidParams_returnsUserAuctions() = runTest {
        
        val userId = "user-123"
        val limit = 10
        val offset = 0
        val userAuctions = listOf(testAuction)

        coEvery { dataSource.getAllAuctionsFromUserPaged(userId, limit, offset) } returns userAuctions

        
        val result = repository.getAllAuctionsFromUserPaged(userId, limit, offset)

        
        assertEquals(userAuctions, result)
        coVerify { dataSource.getAllAuctionsFromUserPaged(userId, limit, offset) }
    }

    @Test
    fun getAllAuctionsUserParticipated_withValidParams_returnsParticipatedAuctions() = runTest {
        
        val userId = "user-123"
        val limit = 10
        val offset = 0
        val participatedAuctions = listOf(testAuction)

        coEvery { dataSource.getAllAuctionsUserParticipated(userId, limit, offset) } returns participatedAuctions

        
        val result = repository.getAllAuctionsUserParticipated(userId, limit, offset)

        
        assertEquals(participatedAuctions, result)
        coVerify { dataSource.getAllAuctionsUserParticipated(userId, limit, offset) }
    }

    @Test
    fun getAllAuctionsFromSearchPaged_withValidQuery_returnsSearchResults() = runTest {
        
        val query = "electronics"
        val limit = 10
        val offset = 0
        val searchResults = listOf(testAuction)

        coEvery { dataSource.getAllAuctionsFromSearchPaged(query, limit, offset) } returns searchResults

        
        val result = repository.getAllAuctionsFromSearchPaged(query, limit, offset)

        
        assertEquals(searchResults, result)
        coVerify { dataSource.getAllAuctionsFromSearchPaged(query, limit, offset) }
    }
}
