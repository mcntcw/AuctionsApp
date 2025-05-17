package com.example.auctionsapp.overview.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auctionsapp.auction_details.presentation.AuctionDetailsAction
import com.example.auctionsapp.auction_details.presentation.AuctionDetailsEvent
import com.example.auctionsapp.auction_details.presentation.AuctionDetailsState
import com.example.auctionsapp.authentication.domain.AuthenticationRepository
import com.example.auctionsapp.core.domain.AuctionRepository
import com.example.auctionsapp.core.domain.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class AuctionDetailsViewModel(
    private val authenticationRepository: AuthenticationRepository,
    private val userRepository: UserRepository,
    private val auctionRepository: AuctionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    var state by mutableStateOf(AuctionDetailsState())

    private val _event = MutableSharedFlow<AuctionDetailsEvent>()
    val event: SharedFlow<AuctionDetailsEvent> = _event

    private val auctionId: String = checkNotNull(savedStateHandle["auctionId"])

    init {
        onAction(AuctionDetailsAction.GetAuctionInfo(auctionId))
    }

    fun onAction(action: AuctionDetailsAction) {
        when (action) {

            is AuctionDetailsAction.GetAuctionInfo -> getAuctionInfo(auctionId)
        }
}

    private fun getAuctionInfo(_auctionId: String) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            try {
                val auction = auctionRepository.getAuctionById(_auctionId)
                if (auction != null) {
                    state = state.copy(auction = auction, isLoading = false)
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
}