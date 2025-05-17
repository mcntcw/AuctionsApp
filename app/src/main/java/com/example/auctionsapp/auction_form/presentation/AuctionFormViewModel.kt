package com.example.auctionsapp.auction_form.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auctionsapp.authentication.domain.AuthenticationRepository
import com.example.auctionsapp.core.domain.Auction
import com.example.auctionsapp.core.domain.AuctionRepository
import com.example.auctionsapp.core.domain.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch


class AuctionFormViewModel (
    private val authenticationRepository: AuthenticationRepository,
    private val userRepository: UserRepository,
    private val auctionRepository: AuctionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    var state by mutableStateOf(AuctionFormState())

    private val _event = MutableSharedFlow<AuctionFormEvent>()
    val event: SharedFlow<AuctionFormEvent> = _event

    private val auctionId: String = checkNotNull(savedStateHandle["auctionId"])


    init {
        if (!auctionId.isNullOrEmpty()) {
            AuctionFormAction.GetAuctionInfo(auctionId)
        }
    }

    fun onAction(action: AuctionFormAction) {
        when (action) {
            is AuctionFormAction.GetAuctionInfo -> getAuctionInfo(action.auctionId)
            is AuctionFormAction.SaveAuction -> saveAuction()
            is AuctionFormAction.UpdateAuctionField -> updateAuctionField { _ ->
                action.newAuction
            }
        }
    }

    private fun updateAuctionField(update: (Auction) -> Auction) {
        state = state.copy(
            auction = update(state.auction)
        )
        println(state.auction)
    }

    private fun getAuctionInfo(id: String) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            try {
                val auction = auctionRepository.getAuctionById(id)
                if (auction != null) {
                    state = state.copy(
                        auction = auction,
                        isEditMode = true,
                        isLoading = false
                    )
                } else {
                    state = state.copy(
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false
                )
            }
        }
    }

    fun saveAuction() {
        viewModelScope.launch {
            val error = validateAuction(state.auction)
            if (error != null) {
                _event.emit(AuctionFormEvent.ShowValidationToast(error))
                return@launch
            }
            state = state.copy(isLoading = true)
            try {
                val result = auctionRepository.upsertAuction(state.auction)
                state = state.copy(isLoading = false)
                _event.emit(AuctionFormEvent.SaveAuctionSuccess(result?.id ?: ""))
            } catch (e: Exception) {
                state = state.copy(isLoading = false)
                _event.emit(AuctionFormEvent.SaveAuctionFailure)
            }
        }
    }

    private fun validateAuction(auction: Auction): String? {
        if (auction.title.isBlank()) {
            return "Title cannot be empty"
        }
        if (auction.description.isNullOrBlank()) {
            return "Description cannot be empty"
        }
        if (auction.buyNowPrice <= 0.0) {
            return "Price must be greater than zero"
        }
        if (auction.galleryUrls.isEmpty()) {
            return "Add at least one image"
        }
        if (auction.phoneNumber.isBlank()) {
            return "Enter a phone number"
        }
        if (!auction.phoneNumber.matches(Regex("^\\d{9}$"))) {
            return "Phone number must have exactly 9 digits"
        }
        // Walidacja daty: nie może być pusta, nie może być DistantFuture, musi być w przyszłości, ale nie za daleko
        val now = kotlinx.datetime.Clock.System.now()
        val maxAllowedYear = 2100 // przykładowy limit, możesz zmienić
        if (auction.endTime == null ||
            auction.endTime == kotlinx.datetime.Instant.DISTANT_FUTURE ||
            auction.endTime <= now
        ) {
            return "End date must be a valid future date"
        }
        val endYear = auction.endTime.toString().take(4).toIntOrNull() ?: 0
        if (endYear > maxAllowedYear) {
            return "End date is too far in the future"
        }
        return null // OK!
    }







}