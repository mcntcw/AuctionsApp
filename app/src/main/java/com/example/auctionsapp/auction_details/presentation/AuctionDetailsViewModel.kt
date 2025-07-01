package com.example.auctionsapp.overview.presentation

import android.annotation.SuppressLint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auctionsapp.auction_details.domain.AuctionService
import com.example.auctionsapp.auction_details.presentation.AuctionDetailsAction
import com.example.auctionsapp.auction_details.presentation.AuctionDetailsEvent
import com.example.auctionsapp.auction_details.presentation.AuctionDetailsState
import com.example.auctionsapp.authentication.domain.AuthenticationRepository
import com.example.auctionsapp.core.domain.Auction
import com.example.auctionsapp.core.domain.AuctionRepository
import com.example.auctionsapp.core.domain.AuctionStatus
import com.example.auctionsapp.core.domain.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class AuctionDetailsViewModel(
    private val authenticationRepository: AuthenticationRepository,
    private val userRepository: UserRepository,
    private val auctionRepository: AuctionRepository,
    private val auctionService: AuctionService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    var state by mutableStateOf(AuctionDetailsState())

    private val _event = MutableSharedFlow<AuctionDetailsEvent>()
    val event: SharedFlow<AuctionDetailsEvent> = _event

    private val auctionId: String = checkNotNull(savedStateHandle["auctionId"])

    private var auctionEndJob: Job? = null
    override fun onCleared() {
        super.onCleared()
        auctionEndJob?.cancel()
        println("🧹 ViewModel cleared - timer cancelled")
    }
    fun cancelTimers() {
        auctionEndJob?.cancel()
    }

    init {
        viewModelScope.launch {
            val user = authenticationRepository.getCurrentUser()
            state = state.copy(currentUserId = user?.id)
            onAction(AuctionDetailsAction.GetAuctionInfo(auctionId))
        }
    }


    fun onAction(action: AuctionDetailsAction) {
        when (action) {

            is AuctionDetailsAction.GetAuctionInfo -> getAuctionInfo(auctionId)
            is AuctionDetailsAction.BuyNow -> buyNow()
            is AuctionDetailsAction.PlaceBid -> placeBid(state.bidValue)
            is AuctionDetailsAction.UpdateBidValue -> updateBidValue { _ ->
                action.newValue
            }
            is AuctionDetailsAction.CancelAuction -> cancelAuction()
        }
}

    private fun getAuctionInfo(_auctionId: String) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            try {
                val auction = auctionRepository.getAuctionById(_auctionId)
                if (auction != null) {
                    val bidAmount = auction.bids.maxByOrNull { it.amount }?.amount ?: 0.0
                    state = state.copy(auction = auction, isLoading = false, bidValue = bidAmount)
                    refreshAuctionAfterEnd(auction)
                    _event.emit(AuctionDetailsEvent.GetAuctionInfoSuccess)
                } else {
                    state = state.copy(
                        isLoading = false,
                        )
                    _event.emit(AuctionDetailsEvent.GetAuctionInfoFailure)
                }
            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false,
                )
                _event.emit(AuctionDetailsEvent.GetAuctionInfoFailure)
            }
        }
    }

    private fun refreshAuctionAfterEnd(auction: Auction) {
        val millisLeft =
            auction.endTime.toEpochMilliseconds() - kotlinx.datetime.Clock.System.now()
                .toEpochMilliseconds()
        if (auction.status == AuctionStatus.ACTIVE && millisLeft > 0) {
            auctionEndJob = viewModelScope.launch {
                delay(millisLeft)
                delay(1000L)
                getAuctionInfo(auction.id!!)
            }
        }
    }

    private fun placeBid(
        bidAmount: Double,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val auction = state.auction
        val currentUserId = state.currentUserId

        if (currentUserId == null) {
            val msg = "You must be logged in to place a bid"
            onError(msg)
            viewModelScope.launch {
                _event.emit(AuctionDetailsEvent.ShowValidationToast(msg))
                _event.emit(AuctionDetailsEvent.PlaceBidFailure)
            }
            return
        }

        val error = validateBid(bidAmount, auction, currentUserId)
        if (error != null) {
            onError(error)
            viewModelScope.launch {
                _event.emit(AuctionDetailsEvent.ShowValidationToast(error))
                _event.emit(AuctionDetailsEvent.PlaceBidFailure)
            }
            return
        }

        viewModelScope.launch {
            try {
                
                auctionService.placeBid(
                    auctionId = auction.id ?: "",
                    bidderId = currentUserId,
                    amount = bidAmount
                )
                getAuctionInfo(auction.id ?: "")
                onSuccess()
                _event.emit(AuctionDetailsEvent.PlaceBidSuccess)
            } catch (e: Exception) {
                val msg = e.message ?: "Failed to place bid. Please try again."
                onError(msg)
                _event.emit(AuctionDetailsEvent.ShowValidationToast(msg))
                _event.emit(AuctionDetailsEvent.PlaceBidFailure)
            }
        }
    }


    private fun buyNow(
    ) {
        val auction = state.auction
        val currentUserId = state.currentUserId

        if (currentUserId == null) {
            val msg = "You must be logged in to buy now"
            viewModelScope.launch {
                _event.emit(AuctionDetailsEvent.ShowValidationToast(msg))
                _event.emit(AuctionDetailsEvent.BuyNowFailure)
            }
            return
        }
        viewModelScope.launch {
            try {
                auctionService.buyNow(
                    auctionId = auction.id ?: "",
                    buyerId = currentUserId
                )
                getAuctionInfo(auction.id ?: "")
                _event.emit(AuctionDetailsEvent.BuyNowSuccess)
            } catch (e: Exception) {
                val msg = e.message ?: "Failed to buy now. Please try again."
                _event.emit(AuctionDetailsEvent.ShowValidationToast(msg))
                _event.emit(AuctionDetailsEvent.BuyNowFailure)
            }
        }
    }

    private fun cancelAuction(
    ) {
        val auction = state.auction

        viewModelScope.launch {
            try {
                auctionRepository.cancelAuction(auction.id ?: "")
                getAuctionInfo(auction.id ?: "")
                _event.emit(AuctionDetailsEvent.CancelAuctionSuccess)
            } catch (e: Exception) {
                _event.emit(AuctionDetailsEvent.CancelAuctionFailure)
            }
        }
    }



    private fun updateBidValue(update: (Double) -> Double) {
        state = state.copy(
            bidValue = update(state.bidValue)
        )
        println(state.bidValue)
    }

    @SuppressLint("DefaultLocale")
    private fun validateBid(
        bidAmount: Double,
        auction: Auction,
        currentUserId: String
    ): String? {
        if (bidAmount <= 0.0) {
            return "Bid amount must be greater than 0"
        }
        if (auction.endTime < kotlinx.datetime.Clock.System.now()) {
            return "The auction has already ended"
        }
        if (auction.seller.id == currentUserId) {
            return "You cannot bid on your own auction"
        }
        val highestBid = auction.bids.maxByOrNull { it.amount }?.amount ?: 0.0
        if (bidAmount <= highestBid) {
            return "Your bid must be higher than the current highest bid ($${String.format("%.2f", highestBid)})"
        }
        
        if (bidAmount >= auction.buyNowPrice) {
            return "Your bid ($${String.format("%.2f", bidAmount)}) is equal to or higher than Buy Now price ($${String.format("%.2f", auction.buyNowPrice)}). Consider using Buy Now instead."
        }
        return null
    }


}