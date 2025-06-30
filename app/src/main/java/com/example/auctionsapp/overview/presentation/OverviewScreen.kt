package com.example.auctionsapp.overview.presentation


import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.auctionsapp.core.domain.Auction
import com.example.auctionsapp.core.domain.AuctionCategory
import com.example.auctionsapp.core.domain.CategoryChip
import com.example.auctionsapp.core.presentation.ui.theme.AuctionsAppTheme
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@Composable
fun OverviewScreenCore(
    viewModel: OverviewViewModel = koinViewModel(),
    onBackAfterSignOut: () -> Unit,
    onAuctionClick: (String) -> Unit,
    onFloatingButtonClick: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onNavigateToUserAuctions: (String) -> Unit,
    onNavigateToUserBids: (String) -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateAfterSearch: (String) -> Unit,
    onLatestClick: () -> Unit
) {
    var backPressedTime by remember { mutableStateOf(0L) }
    val context = LocalContext.current



    BackHandler {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            (context as Activity).finish()
        } else {
            Toast.makeText(
                context,
                "Press again to exit",
                Toast.LENGTH_SHORT
            ).show()
        }
        backPressedTime = System.currentTimeMillis()
    }
    LaunchedEffect(Unit) {

        viewModel.event.collect { event ->
            when (event) {
                OverviewEvent.GetLatestAuctionsSuccess -> {}
                OverviewEvent.GetLatsetAuctionsFailure -> {}
                OverviewEvent.GetUserInfoFailure -> {}
                OverviewEvent.GetUserInfoSuccess -> {}
                OverviewEvent.SignOutFailure -> {}
                OverviewEvent.SignOutSuccess -> {
                    onBackAfterSignOut()
                }
                OverviewEvent.CheckUnreadNotificationsFailure -> {}
                OverviewEvent.CheckUnreadNotificationsSuccess -> {}
            }
        }
    }

    LaunchedEffect(true) {
        viewModel.onAction(OverviewAction.GetLatestAuctions)
        viewModel.onAction(OverviewAction.CheckUnreadNotifications)
    }


    OverviewScreen(
        state = viewModel.state,
        onAction = viewModel::onAction,
        onAuctionClick = onAuctionClick,
        onFloatingButtonClick = onFloatingButtonClick,
        onCategoryClick =  onCategoryClick,
        onNavigateToUserAuctions = onNavigateToUserAuctions,
        onNavigateToUserBids = onNavigateToUserBids,
        onNavigateAfterSearch = onNavigateAfterSearch,
        onLatestClick = onLatestClick,
        onNavigateToNotifications = onNavigateToNotifications,
    )
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    state: OverviewState,
    onAction: (OverviewAction) -> Unit,
    onAuctionClick: (String) -> Unit,
    onFloatingButtonClick: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onNavigateToUserAuctions: (String) -> Unit,
    onNavigateToUserBids: (String) -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateAfterSearch: (String) -> Unit,
    onLatestClick: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState()
    )

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()



    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Klikalny tekst do aukcji użytkownika
                    Text(
                        text = "My Auctions",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable {
                                onNavigateToUserAuctions(state.user.id!!)
                            }
                            .padding(16.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,


                    )
                    Text(
                        text = "My Bids",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable {
                                onNavigateToUserBids(state.user.id!!)
                            }
                            .padding(16.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,


                        )

                    Row(
                        modifier = Modifier
                            .clickable {
                                onNavigateToNotifications()
                            }
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Notifications",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )

                        // Niebieska kropka dla nieprzeczytanych powiadomień
                        if (state.hasUnreadNotifications) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }


                    Spacer(Modifier.weight(1f))

                    Button(onClick = { onAction(OverviewAction.SignOut) }) {
                        Text("Sign out")
                    }
                }
            }
        },
    ) {


        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

            topBar = {
                TopAppBar(
                    title = {
                        state.user.profilePictureUrl?.let { url ->
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .clickable {
                                        scope.launch { drawerState.open() }
                                    },
                                contentAlignment = Alignment.Center

                            ) {
                                Box {
                                    AsyncImage(
                                        model = url,
                                        contentDescription = "Profile picture of ${state.user.name}",
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .border(shape = CircleShape, width = 4.dp, color = MaterialTheme.colorScheme.primary)
                                            .background(MaterialTheme.colorScheme.primary),
                                        onError = {
                                            Log.e("AsyncImage", "Error loading image: ${it.result.throwable}")
                                        },
                                        onSuccess = {
                                            Log.d("AsyncImage", "Image loaded successfully")
                                        }
                                    )

                                    // Kropka dla nieprzeczytanych powiadomień
                                    if (state.hasUnreadNotifications) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary)
                                                .align(Alignment.TopEnd)
                                        )
                                    }
                                }

                            }
                        }
                    },

                    actions = {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                        Text(
                            text = "HAMMERUSH",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(end = 18.dp),
                            fontWeight = FontWeight.Bold
                        ) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background.copy(0f),
                    )
                )
            },

            floatingActionButton = {
                FloatingActionButton(
                    onClick = onFloatingButtonClick,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add new auction"
                    )
                }
            }
        ) { paddingValues ->
            PullToRefreshBox(
                isRefreshing = state.isLatestAuctionsLoading,
                onRefresh = { onAction(OverviewAction.GetLatestAuctions) },
                modifier = Modifier.fillMaxSize()
            ) {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    horizontalAlignment = Alignment.Start
                ) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    item {
                        SearchBar(onSearch = onNavigateAfterSearch)
                    }
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    item {
                        CategoryGrid(
                            modifier = Modifier
                                .fillMaxWidth().heightIn(max = 400.dp),

                            onCategoryClick = onCategoryClick
                        )
                    }

                    item {
                        LatestAuctions(
                            modifier = Modifier.fillMaxWidth(),
                            state = state,
                            onAuctionClick = onAuctionClick,
                            onLatestClick = onLatestClick,
                        )
                    }
                }


        }
        }
    }
}

@Composable
private fun LatestAuctions(
    modifier: Modifier = Modifier,
    state: OverviewState,
    onAuctionClick: (String) -> Unit,
    onLatestClick: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .clickable { onLatestClick() }
                .padding(end = 16.dp)
        ){
            Text(
                text = "Latest",
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                modifier = Modifier.padding(top = 12.dp, start = 16.dp, bottom = 8.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )

        }


        when {
            state.isLatestAuctionsLoading -> {
//                Box(
//                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp, start = 16.dp, bottom = 8.dp),
//                    contentAlignment = Alignment.Center
//                ) {
//                    androidx.compose.material3.CircularProgressIndicator(
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                }
            }
            state.latestAuctions.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp, start = 16.dp, bottom = 8.dp),
                    contentAlignment = Alignment.Center,

                ) {
                    Text(text = "No auctions available", color = Color.White)
                }
            }
            else -> {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp, start = 16.dp)
                ) {
                    itemsIndexed(state.latestAuctions) { index, auction ->
                        AuctionItem(auction = auction, onClick = { onAuctionClick(auction.id!!) }, index = index,
                            itemCount = state.latestAuctions.size)
                    }
                }
            }
        }
    }
}

@Composable
fun AuctionItem(
    auction: Auction,
    onClick: () -> Unit,
    index: Int,
    itemCount: Int
) {
    val imageUrl = auction.galleryUrls.firstOrNull()
    val leftPadding = if (index == 0) 0.dp else 8.dp
    val rightPadding = if (index == itemCount - 1) 16.dp else 0.dp

    // Stan na aktualny czas, odświeżany co sekundę
    var currentTime by remember { mutableStateOf(kotlinx.datetime.Clock.System.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = kotlinx.datetime.Clock.System.now()
            kotlinx.coroutines.delay(1000L)
        }
    }

    Card(
        modifier = Modifier
            .width(240.dp)
            .height(420.dp)
            .padding(start = leftPadding, end = rightPadding)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Auction image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.7f))
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = auction.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = auction.description.orEmpty(),
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                val currentPrice = auction.bids.maxByOrNull { it.amount }?.amount ?: 0.0
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    val timeLeft = auction.endTime - currentTime
                    Text(
                        text = formatDuration(timeLeft),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        fontWeight = FontWeight.Normal,
                    )
                    Text(
                    text = "${"%.0f".format(currentPrice)} / ${"%.0f".format(auction.buyNowPrice)} $",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    )


                }

            }
        }
    }
}

// Funkcja pomocnicza do formatowania czasu
fun formatDuration(duration: kotlin.time.Duration): String {
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

@Preview
@Composable
private fun OverviewScreenPreview() {
    AuctionsAppTheme {
        OverviewScreen(
            state = OverviewState(),
            onAction = {},
            onAuctionClick = {},
            onFloatingButtonClick = {},
            onCategoryClick = {},
            onNavigateToUserAuctions = {},
            onNavigateAfterSearch = {},
            onNavigateToUserBids = {},
            onLatestClick = {},
            onNavigateToNotifications = {},
        )
    }
}

@Composable
fun CategoryGrid(
    modifier: Modifier = Modifier,
    onCategoryClick: (String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 122.dp),
        modifier = modifier
            .padding(horizontal = 16.dp),
        userScrollEnabled = false,
        contentPadding = PaddingValues(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(AuctionCategory.entries.toList(), key = { it.ordinal }) { category ->
            CategoryChip(
                category = category,
                modifier = Modifier.fillMaxWidth(),
                onClick = { onCategoryClick(category.name.lowercase()) }
            )
        }
    }


}

@Composable
fun SearchBar(
    onSearch: (String) -> Unit
) {
    var searchText by remember { mutableStateOf("") }

    Surface(
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
        shape = RoundedCornerShape(60.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(48.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(8.dp))

            BasicTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Normal),
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurfaceVariant),
                decorationBox = { innerTextField ->
                    if (searchText.isEmpty()) {
                        Text(
                            text = "Search...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    innerTextField()
                }
            )

            // Strzałka pojawia się tylko gdy jest tekst
            if (searchText.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        onSearch(searchText)
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

