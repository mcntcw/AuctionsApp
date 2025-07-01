package com.example.auctionsapp.auctions_list.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auctionsapp.authentication.domain.AuthenticationRepository
import com.example.auctionsapp.core.domain.AuctionRepository
import com.example.auctionsapp.core.domain.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch


class AuctionsListViewModel (
    private val authenticationRepository: AuthenticationRepository,
    private val userRepository: UserRepository,
    private val auctionRepository: AuctionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    var state by mutableStateOf(AuctionsListState())

    private val _event = MutableSharedFlow<AuctionsListEvent>()
    val event: SharedFlow<AuctionsListEvent> = _event
    val mode = savedStateHandle.get<String>("mode") ?: "all"

    init {
        when (mode) {
            "category" -> {
                val category = savedStateHandle.get<String>("category") ?: ""
                println("Kategoria z savedStateHandle: $category")
                state = state.copy(category = category)
                onAction(AuctionsListAction.LoadAuctionsByCategory)
            }
            "user" -> {
                val userId = savedStateHandle.get<String>("userId") ?: ""
                state = state.copy(userId = userId)

                
                viewModelScope.launch {
                    try {
                        val user = userRepository.getUserById(userId)
                        state = state.copy(userDisplayName = user?.name)
                    } catch (e: Exception) {
                        
                    }
                }

                onAction(AuctionsListAction.LoadAllAuctionsFromUser)
            }

            "search" -> {
                val query = savedStateHandle.get<String>("query") ?: ""
                state = state.copy(query = query)
                onAction(AuctionsListAction.LoadAllAuctionsFromSearchQuery)
            }
            "bids" -> {
                val userId = savedStateHandle.get<String>("userId") ?: ""
                state = state.copy(userId = userId)
                onAction(AuctionsListAction.LoadAllAuctionsUserParticipated)
            }
            "latest" -> {
                onAction(AuctionsListAction.LoadLatestAuctions)
            }
        }
    }

    fun onAction(action: AuctionsListAction) {
        when (action) {
            is AuctionsListAction.LoadNextPage -> loadNextPage()
            is AuctionsListAction.LoadAuctionsByCategory -> loadAuctionsByCategory()
            is AuctionsListAction.LoadAllAuctionsFromUser -> loadAuctionsByUser()
            is AuctionsListAction.LoadAllAuctionsFromSearchQuery -> loadAllAuctionsBySearchQuery()
            is AuctionsListAction.LoadAllAuctionsUserParticipated -> loadAllAuctionsUserParticipated()
            is AuctionsListAction.LoadLatestAuctions -> loadLatestAuctions()
        }
    }

    private fun loadAuctionsByCategory() {
        viewModelScope.launch {
            try {
                state = state.copy(isLoading = true)
                val auctions = auctionRepository.getAuctionsByCategoryPaged(state.category!!, 20, 0)
                state = state.copy(auctions = auctions, isLoading = false, page = 1, hasMore = auctions.size == 20)
            } catch (e: Exception) {
                state = state.copy(isLoading = false)
            }
        }
    }

    private fun loadAuctionsByUser() {
        viewModelScope.launch {
            try {
                state = state.copy(isLoading = true)
                val auctions = auctionRepository.getAllAuctionsFromUserPaged(state.userId!!, 20, 0)
                state = state.copy(auctions = auctions, isLoading = false, page = 1, hasMore = auctions.size == 20)
            } catch (e: Exception) {
                state = state.copy(isLoading = false)
            }
        }
    }

    private fun loadAllAuctionsUserParticipated() {
        viewModelScope.launch {
            try {
                state = state.copy(isLoading = true)
                val auctions = auctionRepository.getAllAuctionsUserParticipated(state.userId!!, 20, 0)
                state = state.copy(auctions = auctions, isLoading = false, page = 1, hasMore = auctions.size == 20)
            } catch (e: Exception) {
                state = state.copy(isLoading = false)
            }
        }
    }


    private fun loadAllAuctionsBySearchQuery() {
        viewModelScope.launch {
            try {
                state = state.copy(isLoading = true)
                val auctions = auctionRepository.getAllAuctionsFromSearchPaged(state.query!!, 20, 0)
                state = state.copy(auctions = auctions, isLoading = false, page = 1, hasMore = auctions.size == 20)
            } catch (e: Exception) {
                state = state.copy(isLoading = false)
            }
        }
    }

    private fun loadLatestAuctions() {
        viewModelScope.launch {
            try {
                state = state.copy(isLoading = true)
                val auctions = auctionRepository.getLatestAuctions(1000)
                state = state.copy(auctions = auctions, isLoading = false, page = 1, hasMore = auctions.size == 20)
            } catch (e: Exception) {
                state = state.copy(isLoading = false)
            }
        }
    }



    private fun loadNextPage() {
        if (state.isLoading || !state.hasMore) return
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            val pageSize = 20
            val nextPage = state.page + 1
            val newAuctions = when (mode) {
                "category" -> auctionRepository.getAuctionsByCategoryPaged(
                    category = state.category ?: "",
                    offset = state.auctions.size,
                    limit = 20,
                )
                "user" -> auctionRepository.getAllAuctionsFromUserPaged(
                    userId = state.userId ?: "",
                    limit = pageSize,
                    offset = state.auctions.size
                )
                "search" -> auctionRepository.getAllAuctionsFromSearchPaged(
                    query = state.query ?: "",
                    limit = pageSize,
                    offset = state.auctions.size
                )




                else -> emptyList()
            }
            state = state.copy(
                auctions = state.auctions + newAuctions,
                isLoading = false,
                page = nextPage,
                hasMore = newAuctions.size == pageSize
            )
        }
    }


}

