@file:Suppress("DEPRECATION")

package com.example.auctionsapp.auction_details.presentation

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import com.example.auctionsapp.auction_details.domain.Bid
import com.example.auctionsapp.core.domain.AuctionStatus
import com.example.auctionsapp.core.domain.CategoryChip
import com.example.auctionsapp.overview.presentation.AuctionDetailsViewModel
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Duration

@Composable
fun AuctionDetailsScreenCore(
    viewModel: AuctionDetailsViewModel = koinViewModel(),
    auctionId: String,
    onBack: () -> Unit,
    onSellerClick: (String) -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is AuctionDetailsEvent.GetAuctionInfoSuccess -> {}
                is AuctionDetailsEvent.GetAuctionInfoFailure -> {}
                is AuctionDetailsEvent.BuyNowFailure -> {}
                is AuctionDetailsEvent.BuyNowSuccess -> {
                    Toast.makeText(context, "Purchase successful! The auction has ended. Please contact the seller to finalize the transaction.", Toast.LENGTH_SHORT).show()
                }
                is AuctionDetailsEvent.PlaceBidFailure -> {}
                is AuctionDetailsEvent.PlaceBidSuccess -> {}
                is AuctionDetailsEvent.ShowValidationToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is AuctionDetailsEvent.AuctionEndedByTime -> Toast.makeText(context, "Auction has ended!", Toast.LENGTH_SHORT).show()
                AuctionDetailsEvent.CancelAuctionFailure -> {
                    Toast.makeText(context, "Cancel failed", Toast.LENGTH_SHORT).show()
                }
                AuctionDetailsEvent.CancelAuctionSuccess -> {
                    Toast.makeText(context, "The auction has been cancelled.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    AuctionDetailsScreen(
        state = viewModel.state,
        onAction = viewModel::onAction,
        onBack = onBack,
        onSellerClick = onSellerClick
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuctionDetailsScreen(
    state: AuctionDetailsState,
    onAction: (AuctionDetailsAction) -> Unit,
    onBack: ()-> Unit,
    onSellerClick: (String)-> Unit
) {

    val images = state.auction.galleryUrls

    var galleryDialogOpen by remember { mutableStateOf(false) }
    var galleryStartIndex by remember { mutableIntStateOf(0) }
    var showCancelDialog by remember { mutableStateOf(false) }

    fun openGallery(index: Int) {
        galleryStartIndex = index
        galleryDialogOpen = true
    }

    if (galleryDialogOpen) {
        GalleryDialog(
            images = images,
            startIndex = galleryStartIndex,
            onDismiss = { galleryDialogOpen = false }
        )
    }

    val pagerState = rememberPagerState()

    Scaffold(
        topBar = {
            AuctionDetailsTopBar(
                onBack = onBack,
                isOwner = state.auction.seller.id == state.currentUserId,
                auctionStatus = state.auction.status,
                onCancelAuction = { showCancelDialog = true }
            )
        },

        ) { padding ->

        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { onAction(AuctionDetailsAction.GetAuctionInfo(state.auction.id!!)) },
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Karuzela zdjęć
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.DarkGray)
                ) {
                    HorizontalPager(
                        count = images.size.coerceAtLeast(1),
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                    ) { page ->
                        AsyncImage(
                            model = images.getOrNull(page),
                            contentDescription = "Auction image",
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { openGallery(page) },
                            contentScale = ContentScale.Crop
                        )
                    }
                    if (images.size > 1) {
                        HorizontalPagerIndicator(
                            pagerState = pagerState,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(12.dp),
                            activeColor = MaterialTheme.colorScheme.primary,
                            inactiveColor = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = state.auction.title,
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.auction.description.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                    fontStyle = FontStyle.Italic
                )
                Spacer(modifier = Modifier.height(8.dp))
                CategoryChip(
                    category = state.auction.category,
                )

                Spacer(modifier = Modifier.height(32.dp))
                AuctionDetailRowWithAvatar(
                    label = "Seller",
                    value = state.auction.seller.name,
                    avatarUrl = state.auction.seller.profilePictureUrl,
                    onClick = { onSellerClick(state.auction.seller.id!!) },
                )

                AuctionDetailRow(label = "Phone", value = state.auction.phoneNumber)

                Spacer(modifier = Modifier.height(32.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 22.dp),
                ) {
                    if (state.auction.seller.id == state.currentUserId) {
                        // Sprzedawca widzi box z wszystkimi bidami
                        SellerBidsBox(
                            bids = state.auction.bids
                        )
                    } else {
                        if (state.auction.status == AuctionStatus.ACTIVE) {
                            AuctionBidsBox(
                                bids = state.auction.bids,
                                buyNowPrice = state.auction.buyNowPrice,
                                endTime = state.auction.endTime,
                                buyerName = state.auction.buyer?.name,
                                onBuyNow = { onAction(AuctionDetailsAction.BuyNow) },
                                bidAmount = state.bidValue.toString(),
                                onBidAmountChange = { newValue ->
                                    val value = newValue.toDoubleOrNull() ?: 0.0
                                    onAction(AuctionDetailsAction.UpdateBidValue(value))
                                },
                                onBidClick = { onAction(AuctionDetailsAction.PlaceBid(state.bidValue)) }
                            )
                        }

                    }
                }
            }
        }
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        val overlayText = when (state.auction.status) {
            AuctionStatus.SOLD -> "SOLD"
            AuctionStatus.CANCELLED -> "CANCELLED"
            AuctionStatus.ENDED -> "ENDED"
            else -> null
        }

        if (overlayText != null) {
            val screenHeight = LocalConfiguration.current.screenHeightDp.dp
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            ) {
                Text(
                    text = overlayText,
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = screenHeight * 0.75f - 48.dp)
                )
            }
        }

        // Dialog potwierdzenia anulowania
        CancelAuctionDialog(
            showDialog = showCancelDialog,
            onConfirm = {
                showCancelDialog = false
                onAction(AuctionDetailsAction.CancelAuction)
            },
            onDismiss = {
                showCancelDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuctionDetailsTopBar(
    onBack: () -> Unit,
    isOwner: Boolean,
    auctionStatus: AuctionStatus,
    onCancelAuction: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                text = "",
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            // Pokaż menu tylko dla właściciela aktywnej aukcji
            if (isOwner && auctionStatus == AuctionStatus.ACTIVE) {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu"
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Cancel Auction",
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            showMenu = false
                            onCancelAuction()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun CancelAuctionDialog(
    showDialog: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    "Cancel Auction",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Are you sure you want to cancel this auction? This action cannot be undone and all current bids will be invalidated.",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        "Yes, Cancel",
                        color = MaterialTheme.colorScheme.onError,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        "Keep Auction",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        )
    }
}

@Composable
fun GalleryDialog(
    images: List<String>,
    startIndex: Int,
    onDismiss: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = startIndex)

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
        ) {
            HorizontalPager(
                count = images.size.coerceAtLeast(1),
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                AsyncImage(
                    model = images.getOrNull(page),
                    contentDescription = "Gallery image",
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentScale = ContentScale.Fit
                )
            }
            // Zamknięcie dialogu
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close gallery",
                    tint = Color.White
                )
            }
            // Wskaźnik pagera na dole
            if (images.size > 1) {
                HorizontalPagerIndicator(
                    pagerState = pagerState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(24.dp),
                    activeColor = Color.White,
                    inactiveColor = Color.Gray
                )
            }
        }
    }
}

@Composable
fun AuctionDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AuctionDetailRowWithAvatar(
    label: String,
    value: String,
    avatarUrl: String? = null,
    onClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .clickable { onClick(value) }
                .padding(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            AsyncImage(
                model = avatarUrl,
                contentDescription = "Profile picture of seller",
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                onError = {
                    Log.e("AsyncImage", "Error loading image: ${it.result.throwable}")
                },
                onSuccess = {
                    Log.d("AsyncImage", "Image loaded successfully")
                }
            )
        }
    }
}

@Composable
fun AuctionBidsBox(
    bids: List<Bid>,
    buyNowPrice: Double,
    endTime: Instant,
    buyerName: String?,
    onBuyNow: () -> Unit,
    bidAmount: String,
    onBidAmountChange: (String) -> Unit,
    onBidClick: () -> Unit,
) {

    var isBidDialogOpen by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf(Clock.System.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Clock.System.now()
            delay(1000L)
        }
    }
    val timeLeft = endTime - currentTime

    val highestBid = bids.maxByOrNull { it.amount }
    val highestBidder = highestBid?.bidder?.name ?: "-"
    val highestBidAmount = highestBid?.amount ?: 0.0
    val highestBidTime = highestBid?.placedAt?.let { instant ->
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        "%04d-%02d-%02d %02d:%02d:%02d".format(
            localDateTime.year,
            localDateTime.monthNumber,
            localDateTime.dayOfMonth,
            localDateTime.hour,
            localDateTime.minute,
            localDateTime.second
        )
    } ?: "-"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp))
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Countdown
            Text(
                text = "Time left: ${formatDuration(timeLeft)}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            // Highest bid
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Highest bid: $${"%.2f".format(highestBidAmount)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "by $highestBidder at $highestBidTime",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { isBidDialogOpen = true },
                    shape = RoundedCornerShape(percent = 50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.background
                    ),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "Bids (${bids.size})",
                        color = MaterialTheme.colorScheme.background,
                        style = MaterialTheme.typography.labelLarge
                    )

                }
            }

            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Place bid",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(12.dp))
            // Place bid row
            BidInputWithButton(
                bidAmount = bidAmount,
                onBidAmountChange = onBidAmountChange,
                onBidClick = onBidClick
            )
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "or",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))
            // Buy now button
            Button(
                onClick = onBuyNow,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Buy Now ($${buyNowPrice})", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(12.dp))

            // Buyer info
            if (!buyerName.isNullOrBlank()) {
                Text(
                    text = "Buyer: $buyerName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Dialog z listą bidów
    if (isBidDialogOpen) {
        BidsDialog(
            bids = bids,
            onDismiss = { isBidDialogOpen = false }
        )
    }
}

@Composable
fun BidsDialog(
    bids: List<Bid>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .widthIn(min = 280.dp, max = 340.dp)
            ) {
                Text(
                    text = "All Bids",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(12.dp))
                if (bids.isEmpty()) {
                    Text("No bids yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant )
                } else {
                    bids.sortedByDescending { it.amount }.forEach { bid ->
                        Column(Modifier.padding(vertical = 6.dp)) {
                            Text(
                                text = "${bid.bidder.name} – $${"%.2f".format(bid.amount)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = bid.placedAt?.let { instant ->
                                    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                                    "%04d-%02d-%02d %02d:%02d:%02d".format(
                                        localDateTime.year,
                                        localDateTime.monthNumber,
                                        localDateTime.dayOfMonth,
                                        localDateTime.hour,
                                        localDateTime.minute,
                                        localDateTime.second
                                    )
                                } ?: "-",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "Close",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

fun formatDuration(duration: Duration): String {
    if (duration.isNegative()) return "Ended"
    val totalSeconds = duration.inWholeSeconds
    val days = totalSeconds / 86400
    val hours = (totalSeconds % 86400) / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return buildString {
        if (days > 0) append("${days}d ")
        if (hours > 0 || days > 0) append("${hours}h ")
        if (minutes > 0 || hours > 0 || days > 0) append("${minutes}m ")
        append("${seconds}s")
    }.trim()
}

@Composable
fun BidInputWithButton(
    bidAmount: String,
    onBidAmountChange: (String) -> Unit,
    onBidClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.2f), shape = RoundedCornerShape(60.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = bidAmount,
            onValueChange =  onBidAmountChange,
            placeholder = { Text("Your bid", color = Color.White.copy(alpha = 0.6f)) },
            leadingIcon = {
                Text(
                    "$",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.titleMedium
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier
            .width(8.dp)
        )
        IconButton(
            onClick = onBidClick,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(60.dp))
                .size(42.dp)
                .padding(6.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = "Bid",
                tint = MaterialTheme.colorScheme.background
            )
        }
    }
}

@Composable
fun SellerBidsBox(bids: List<Bid>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp))
            .padding(18.dp)
    ) {
        Column {
            if (bids.isEmpty()) {
                Text("No bids yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            } else {
                Text(
                    text = "All Bids",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                bids.sortedByDescending { it.amount }.forEach { bid ->
                    Column(Modifier.padding(vertical = 6.dp)) {

                        Text(
                            text = "${bid.bidder.name} – $${"%.2f".format(bid.amount)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = bid.placedAt
                            !!.toLocalDateTime(TimeZone.currentSystemDefault())
                                .let { "${it.dayOfMonth.toString().padStart(2, '0')}-${it.monthNumber.toString().padStart(2, '0')}-${it.year.toString().takeLast(2)} ${it.hour.toString().padStart(2, '0')}:${it.minute.toString().padStart(2, '0')}" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
