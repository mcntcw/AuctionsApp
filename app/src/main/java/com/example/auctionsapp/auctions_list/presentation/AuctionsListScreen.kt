package com.example.auctionsapp.auctions_list.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.auctionsapp.core.domain.Auction
import com.example.auctionsapp.core.domain.AuctionStatus
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AuctionsListScreenCore(
    viewModel: AuctionsListViewModel = koinViewModel(),
    mode: String,
    category: String,
    userId: String,
    query: String,
    onBack: () -> Unit,
    onAuctionClick: (String) -> Unit
) {
    AuctionsListScreen(
        state = viewModel.state,
        onAction = viewModel::onAction,
        onAuctionClick = { auction -> onAuctionClick(auction.id ?: "") },
        onLoadMore = { viewModel.onAction(AuctionsListAction.LoadNextPage) },
        onBack = onBack,
        mode = mode,
        category = category,
        query = query
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuctionsListScreen(
    state: AuctionsListState,
    onAction: (AuctionsListAction) -> Unit,
    onLoadMore: () -> Unit,
    onAuctionClick: (Auction) -> Unit,
    onBack: () -> Unit,
    mode: String,
    category: String,
    query: String
) {
    val listState = rememberLazyListState()

    // Tytuł w zależności od trybu
    val title = when (mode) {
        "category" -> category.replace('_', '/').replaceFirstChar { it.uppercase() }
        "user" -> "${state.userDisplayName ?: "User"} Auctions"
        "search" -> "Search: $query"
        "bids" -> "My Bids"
        "latest" -> "Latest"
        else -> "All Auctions"
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(0f),
                )
            )
        }
    ) { innerPadding ->

        if (state.auctions.isEmpty() && !state.isLoading) {
            // Wyświetl informację o pustej liście
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                EmptyListMessage(mode = mode, query = query, category = category)
            }
        }

        else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(state.auctions) { index, auction ->
                    AuctionListItem(
                        auction = auction,
                        onClick = { onAuctionClick(auction) }
                    )
                }

                if (state.isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .collect { layoutInfo ->
                val totalItems = layoutInfo.totalItemsCount
                val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

                if (state.hasMore) {
                    onLoadMore()
                }
            }
    }
}

@Composable
fun AuctionListItem(
    auction: Auction,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Większy placeholder dla zdjęcia
            AsyncImage(
                model = auction.galleryUrls.firstOrNull(),
                contentDescription = "Auction image",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop,

            )

            // Główna zawartość
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tytuł i status w jednym wierszu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = auction.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    AuctionStatusBadge(status = auction.status)
                }

                // Cena
                PriceRow(
                    currentPrice = auction.getCurrentPrice(),
                    isActive = auction.status == AuctionStatus.ACTIVE
                )

                // Sprzedawca
                SellerRow(sellerName = auction.seller.name)

                // Czas do końca (tylko dla aktywnych aukcji)
                if (auction.status == AuctionStatus.ACTIVE) {
                    TimeLeftRow(endTime = auction.endTime)
                }
            }
        }
    }
}

@Composable
fun ImagePlaceholder() {
    Box(
        modifier = Modifier
            .size(120.dp) // Większy rozmiar
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Auction image",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(40.dp) // Większa ikona
        )
    }
}

@Composable
fun PriceRow(
    currentPrice: Double,
    isActive: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = formatPrice(currentPrice),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (isActive) {
            Text(
                text = "Current bid",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SellerRow(sellerName: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Seller",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "by $sellerName",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TimeLeftRow(endTime: kotlinx.datetime.Instant) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = "Time left",
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = getTimeLeft(endTime),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun AuctionStatusBadge(status: AuctionStatus) {
    val (backgroundColor, textColor, text) = when (status) {
        AuctionStatus.ACTIVE -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "ACTIVE"
        )
        AuctionStatus.ENDED -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "ENDED"
        )
        AuctionStatus.SOLD -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            "SOLD"
        )
        AuctionStatus.CANCELLED -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "CANCELLED"
        )
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        modifier = Modifier.wrapContentSize()
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// Pomocnicze funkcje
private fun formatPrice(price: Double): String {
    return NumberFormat.getCurrencyInstance(Locale.getDefault()).format(price)
}

private fun getTimeLeft(endTime: kotlinx.datetime.Instant): String {
    val now = kotlinx.datetime.Clock.System.now()
    val timeLeft = endTime - now

    return when {
        timeLeft.inWholeDays > 0 -> "${timeLeft.inWholeDays}d ${timeLeft.inWholeHours % 24}h left"
        timeLeft.inWholeHours > 0 -> "${timeLeft.inWholeHours}h ${timeLeft.inWholeMinutes % 60}m left"
        timeLeft.inWholeMinutes > 0 -> "${timeLeft.inWholeMinutes}m left"
        timeLeft.inWholeSeconds > 0 -> "Less than 1m left"
        else -> "Auction ended"
    }
}

// Extension function dla Auction
private fun Auction.getCurrentPrice(): Double {
    return bids.maxByOrNull { it.amount }?.amount ?: 0.0
}

@Composable
fun EmptyListMessage(
    mode: String,
    query: String,
    category: String
) {
    val message = when (mode) {
        "category" -> "No auctions found in ${category.replace('_', ' ')} category"
        "user" -> "This user has no auctions yet"
        "search" -> "No results found for \"$query\""
        "bids" -> "You haven't bid on any auctions yet"
        else -> "No auctions available"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(32.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Try adjusting your search or check back later",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}
