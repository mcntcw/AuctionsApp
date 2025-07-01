package com.example.auctionsapp.auction_details.presentation

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.auctionsapp.auction_details.domain.AuctionService
import com.example.auctionsapp.auction_details.domain.Bid
import com.example.auctionsapp.authentication.domain.AuthenticationRepository
import com.example.auctionsapp.core.domain.Auction
import com.example.auctionsapp.core.domain.AuctionCategory
import com.example.auctionsapp.core.domain.AuctionRepository
import com.example.auctionsapp.core.domain.AuctionStatus
import com.example.auctionsapp.core.domain.User
import com.example.auctionsapp.core.domain.UserRepository
import com.example.auctionsapp.overview.presentation.AuctionDetailsViewModel
import com.example.auctionsapp.testutils.MainDispatcherExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.time.Duration.Companion.hours

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MainDispatcherExtension::class)
class AuctionDetailsViewModelTest {
    private lateinit var authenticationRepository: AuthenticationRepository
    private lateinit var userRepository: UserRepository
    private lateinit var auctionRepository: AuctionRepository
    private lateinit var auctionService: AuctionService
    private lateinit var savedStateHandle: SavedStateHandle

    private lateinit var viewModel: AuctionDetailsViewModel

    
    private val testAuctionId = "test-auction-id"
    private val testUserId = "test-user-id"
    private val testSellerId = "test-seller-id"

    private val testUser = User(
        id = testUserId,
        email = "test@example.com",
        name = "Test User",
        profilePictureUrl = null
    )

    private val testSeller = User(
        id = testSellerId,
        email = "seller@example.com",
        name = "Test Seller",
        profilePictureUrl = null
    )

    private val testBid = Bid(
        id = "bid1",
        auctionId = testAuctionId,
        bidder = testUser,
        amount = 150.0,
        placedAt = Clock.System.now()
    )

    
    private val testAuctionEnded = Auction(
        id = testAuctionId,
        status = AuctionStatus.ENDED, 
        category = AuctionCategory.ELECTRONICS,
        title = "Test Auction",
        description = "Test Description",
        galleryUrls = listOf("url1", "url2"),
        seller = testSeller,
        phoneNumber = "123456789",
        bids = listOf(testBid),
        buyNowPrice = 500.0,
        buyer = null,
        createdAt = Clock.System.now(),
        endTime = Clock.System.now().minus(1.hours) 
    )

    
    private val testAuctionActive = Auction(
        id = testAuctionId,
        status = AuctionStatus.ACTIVE,
        category = AuctionCategory.ELECTRONICS,
        title = "Test Auction Active",
        description = "Test Description",
        galleryUrls = listOf("url1", "url2"),
        seller = testSeller,
        phoneNumber = "123456789",
        bids = listOf(testBid),
        buyNowPrice = 500.0,
        buyer = null,
        createdAt = Clock.System.now(),
        endTime = Clock.System.now().plus(1.hours)
    )

    private val testAuctionActiveNoBids = Auction(
        id = testAuctionId,
        status = AuctionStatus.ACTIVE,
        category = AuctionCategory.ELECTRONICS,
        title = "Test Auction Active",
        description = "Test Description",
        galleryUrls = listOf("url1", "url2"),
        seller = testSeller,
        phoneNumber = "123456789",
        bids = emptyList(), 
        buyNowPrice = 500.0,
        buyer = null,
        createdAt = Clock.System.now(),
        endTime = Clock.System.now().plus(1.hours) 
    )

    @BeforeEach
    fun setup() {
        
        authenticationRepository = mockk(relaxed = true)
        userRepository = mockk(relaxed = true)
        auctionRepository = mockk(relaxed = true)
        auctionService = mockk(relaxed = true)
        savedStateHandle = mockk(relaxed = true)

        
        every { savedStateHandle.get<String>("auctionId") } returns testAuctionId
        coEvery { authenticationRepository.getCurrentUser() } returns testUser
    }

    private fun createViewModel(): AuctionDetailsViewModel {
        return AuctionDetailsViewModel(
            authenticationRepository = authenticationRepository,
            userRepository = userRepository,
            auctionRepository = auctionRepository,
            auctionService = auctionService,
            savedStateHandle = savedStateHandle
        )
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    fun initWithEndedAuction_setsCurrentUserAndLoadsAuction() = runTest {
        
        coEvery { auctionRepository.getAuctionById(testAuctionId) } returns testAuctionEnded

        try {
            
            viewModel = createViewModel()

            
            assertEquals(testUserId, viewModel.state.currentUserId)
            assertEquals(testAuctionEnded, viewModel.state.auction)
            assertFalse(viewModel.state.isLoading)

            
            coVerify { authenticationRepository.getCurrentUser() }
            coVerify { auctionRepository.getAuctionById(testAuctionId) }

        } finally {
            
            coroutineContext.cancelChildren()
        }
    }


    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    fun initWithNullCurrentUser_setsCurrentUserIdToNull() = runTest {
        
        coEvery { authenticationRepository.getCurrentUser() } returns null
        coEvery { auctionRepository.getAuctionById(testAuctionId) } returns testAuctionEnded

        try {
            
            viewModel = createViewModel()

            
            assertEquals(null, viewModel.state.currentUserId)
            assertEquals(testAuctionEnded, viewModel.state.auction)

        } finally {
            coroutineContext.cancelChildren()
        }
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    fun getAuctionInfo_withValidId_updatesStateAndEmitsSuccess() = runTest {
        
        coEvery { auctionRepository.getAuctionById(testAuctionId) } returns testAuctionEnded

        try {
            viewModel = createViewModel()

            
            viewModel.event.test {
                viewModel.onAction(AuctionDetailsAction.GetAuctionInfo(testAuctionId))

                
                assertEquals(AuctionDetailsEvent.GetAuctionInfoSuccess, awaitItem())
                cancelAndConsumeRemainingEvents()
            }

            
            assertEquals(testAuctionEnded, viewModel.state.auction)
            assertFalse(viewModel.state.isLoading)

            
            coVerify { auctionRepository.getAuctionById(testAuctionId) }

        } finally {
            
            viewModel.cancelTimers()
            coroutineContext.cancelChildren()
        }
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    fun getAuctionInfo_withNullAuction_emitsFailure() = runTest {
        
        coEvery { auctionRepository.getAuctionById(testAuctionId) } returns null

        try {
            viewModel = createViewModel()

            
            viewModel.event.test {
                viewModel.onAction(AuctionDetailsAction.GetAuctionInfo(testAuctionId))

                
                assertEquals(AuctionDetailsEvent.GetAuctionInfoFailure, awaitItem())
                cancelAndConsumeRemainingEvents()
            }

            
            assertEquals(Auction.empty(), viewModel.state.auction)
            assertFalse(viewModel.state.isLoading)

            
            coVerify(exactly = 2) { auctionRepository.getAuctionById(testAuctionId) }
        } finally {
            
            viewModel.cancelTimers()
            coroutineContext.cancelChildren()
        }
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    fun getAuctionInfo_withException_emitsFailure() = runTest {
        coEvery { auctionRepository.getAuctionById(testAuctionId) } throws Exception("Network error")

        try {
            viewModel = createViewModel()

            
            viewModel.event.test {
                viewModel.onAction(AuctionDetailsAction.GetAuctionInfo(testAuctionId))

                
                assertEquals(AuctionDetailsEvent.GetAuctionInfoFailure, awaitItem())
                cancelAndConsumeRemainingEvents()
            }

            
            assertEquals(Auction.empty(), viewModel.state.auction)
            assertFalse(viewModel.state.isLoading)

            
            coVerify(exactly = 2) { auctionRepository.getAuctionById(testAuctionId) }
        } finally {
            
            viewModel.cancelTimers()
            coroutineContext.cancelChildren()
        }
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    fun placeBid_withZeroAmount_rejectsWithValidationError() = runTest {
        coEvery { auctionRepository.getAuctionById(testAuctionId) } returns testAuctionActiveNoBids

        try {
            viewModel = createViewModel()

            viewModel.event.test {
                viewModel.onAction(AuctionDetailsAction.UpdateBidValue(0.0))
                viewModel.onAction(AuctionDetailsAction.PlaceBid(0.0))

                val validationEvent = awaitItem() as AuctionDetailsEvent.ShowValidationToast
                assertEquals("Bid amount must be greater than 0", validationEvent.message)

                assertEquals(AuctionDetailsEvent.PlaceBidFailure, awaitItem())
                cancelAndConsumeRemainingEvents()
            }

            coVerify(exactly = 0) { auctionService.placeBid(any(), any(), any()) }

        } finally {
            viewModel.cancelTimers()
            coroutineContext.cancelChildren()
        }
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    fun placeBid_onEndedAuction_rejectsWithValidationError() = runTest {
        coEvery { auctionRepository.getAuctionById(testAuctionId) } returns testAuctionEnded

        viewModel = createViewModel()

        
        viewModel.event.test {
            viewModel.onAction(AuctionDetailsAction.UpdateBidValue(200.0))
            viewModel.onAction(AuctionDetailsAction.PlaceBid(200.0))

            val validationEvent = awaitItem() as AuctionDetailsEvent.ShowValidationToast
            assertEquals("The auction has already ended", validationEvent.message)

            assertEquals(AuctionDetailsEvent.PlaceBidFailure, awaitItem())
            cancelAndConsumeRemainingEvents()
        }

        
        coVerify(exactly = 0) { auctionService.placeBid(any(), any(), any()) }
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    fun placeBid_onOwnAuction_rejectsWithValidationError() = runTest {
        coEvery { auctionRepository.getAuctionById(testAuctionId) } returns testAuctionActiveNoBids
        coEvery { authenticationRepository.getCurrentUser() } returns testSeller

        viewModel = createViewModel()
        viewModel.cancelTimers()
        
        viewModel.event.test {
            viewModel.onAction(AuctionDetailsAction.UpdateBidValue(200.0))
            viewModel.onAction(AuctionDetailsAction.PlaceBid(200.0))

            val validationEvent = awaitItem() as AuctionDetailsEvent.ShowValidationToast
            assertEquals("You cannot bid on your own auction", validationEvent.message)

            assertEquals(AuctionDetailsEvent.PlaceBidFailure, awaitItem())
            cancelAndConsumeRemainingEvents()
        }

        coVerify(exactly = 0) { auctionService.placeBid(any(), any(), any()) }
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    fun placeBid_lowerThanHighestBid_rejectsWithValidationError() = runTest {
        coEvery { auctionRepository.getAuctionById(testAuctionId) } returns testAuctionActive

        try {
            viewModel = createViewModel()

            
            viewModel.event.test {
                viewModel.onAction(AuctionDetailsAction.UpdateBidValue(140.0))
                viewModel.onAction(AuctionDetailsAction.PlaceBid(140.0))

                val validationEvent = awaitItem() as AuctionDetailsEvent.ShowValidationToast
                assertTrue(validationEvent.message.contains("Your bid must be higher than the current highest bid"))

                assertEquals(AuctionDetailsEvent.PlaceBidFailure, awaitItem())
                cancelAndConsumeRemainingEvents()
            }

            
            coVerify(exactly = 0) { auctionService.placeBid(any(), any(), any()) }

        } finally {
            viewModel.cancelTimers()
            coroutineContext.cancelChildren()
        }
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    fun placeBid_equalToBuyNowPrice_suggestsBuyNow() = runTest {
        assertEquals(500.0, testAuctionActive.buyNowPrice)
        coEvery { auctionRepository.getAuctionById(testAuctionId) } returns testAuctionActive

        try {
            viewModel = createViewModel()
            viewModel.onAction(AuctionDetailsAction.UpdateBidValue(500.0))

            viewModel.event.test {


                viewModel.onAction(AuctionDetailsAction.PlaceBid(500.0))

                val validationEvent = awaitItem() as AuctionDetailsEvent.ShowValidationToast
                assertTrue(validationEvent.message.contains("Consider using Buy Now instead"))

                assertEquals(AuctionDetailsEvent.PlaceBidFailure, awaitItem())
                cancelAndConsumeRemainingEvents()
            }

            
            coVerify(exactly = 0) { auctionService.placeBid(any(), any(), any()) }

        } finally {
            viewModel.cancelTimers()
            coroutineContext.cancelChildren()
        }
    }



    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    fun placeBid_withValidBid_callsServiceAndEmitsSuccess() = runTest {
        
        coEvery { auctionRepository.getAuctionById(testAuctionId) } returns testAuctionActive
        coEvery { authenticationRepository.getCurrentUser() } returns testUser
        coEvery { auctionService.placeBid(any(), any(), any()) } returns Unit

        try {
            viewModel = createViewModel()
            viewModel.cancelTimers()
            
            viewModel.onAction(AuctionDetailsAction.UpdateBidValue(200.0))

            
            viewModel.event.test {
                viewModel.onAction(AuctionDetailsAction.PlaceBid(0.0)) 

                val successEvent = awaitItem()
                assertEquals(AuctionDetailsEvent.PlaceBidSuccess, successEvent)

                cancelAndConsumeRemainingEvents()
            }

            
            coVerify(exactly = 1) {
                auctionService.placeBid(testAuctionId, testUser.id!!, 200.0)
            }

        } finally {
            viewModel.cancelTimers()
            coroutineContext.cancelChildren()
        }
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    fun placeBid_withServiceException_emitsErrorToast() = runTest {
        
        coEvery { auctionRepository.getAuctionById(testAuctionId) } returns testAuctionActive
        coEvery { authenticationRepository.getCurrentUser() } returns testUser
        
        coEvery { auctionService.placeBid(any(), any(), any()) } throws Exception("Network error")

        try {
            viewModel = createViewModel()
            viewModel.cancelTimers()

            viewModel.onAction(AuctionDetailsAction.UpdateBidValue(200.0))

            
            viewModel.event.test {
                viewModel.onAction(AuctionDetailsAction.PlaceBid(0.0))

                
                val errorToast = awaitItem() as AuctionDetailsEvent.ShowValidationToast
                assertTrue(errorToast.message.contains("error") || errorToast.message.contains("failed"))

                
                assertEquals(AuctionDetailsEvent.PlaceBidFailure, awaitItem())

                cancelAndConsumeRemainingEvents()
            }

            
            coVerify(exactly = 1) {
                auctionService.placeBid(testAuctionId, testUser.id!!, 200.0)
            }

        } finally {
            viewModel.cancelTimers()
            coroutineContext.cancelChildren()
        }
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    fun buyNow_withValidUser_callsServiceAndEmitsSuccess() = runTest {
        
        coEvery { auctionRepository.getAuctionById(testAuctionId) } returns testAuctionActive
        coEvery { authenticationRepository.getCurrentUser() } returns testUser
        coEvery { auctionService.buyNow(any(), any()) } returns Unit

        try {
            viewModel = createViewModel()
            viewModel.cancelTimers()
            
            viewModel.event.test {
                viewModel.onAction(AuctionDetailsAction.BuyNow)

                val successEvent = awaitItem()
                assertEquals(AuctionDetailsEvent.BuyNowSuccess, successEvent)

                cancelAndConsumeRemainingEvents()
            }

            
            coVerify(exactly = 1) {
                auctionService.buyNow(testAuctionId, testUser.id!!)
            }

        } finally {
            viewModel.cancelTimers()
            coroutineContext.cancelChildren()
        }
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    fun buyNow_withNullUser_emitsAuthenticationError() = runTest {
        
        coEvery { auctionRepository.getAuctionById(testAuctionId) } returns testAuctionActive
        coEvery { authenticationRepository.getCurrentUser() } returns null 

        viewModel = createViewModel()
        viewModel.cancelTimers()
        
        viewModel.event.test {
            viewModel.onAction(AuctionDetailsAction.BuyNow)

            val validationEvent = awaitItem() as AuctionDetailsEvent.ShowValidationToast
            assertTrue(validationEvent.message.contains("You must be logged in") ||
                    validationEvent.message.contains("authentication") ||
                    validationEvent.message.contains("login"))

            assertEquals(AuctionDetailsEvent.BuyNowFailure, awaitItem())
            cancelAndConsumeRemainingEvents()
        }

        
        coVerify(exactly = 0) { auctionService.buyNow(any(), any()) }
    }






}
