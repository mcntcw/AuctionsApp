package com.example.auctionsapp.overview.presentation


import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.auctionsapp.core.domain.Auction
import com.example.auctionsapp.core.presentation.ui.theme.AuctionsAppTheme
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@Composable
fun OverviewScreenCore(
    viewModel: OverviewViewModel = koinViewModel(),
    onBackAfterSignOut: () -> Unit,
    onAuctionClick: (String) -> Unit,
    onFloatingButtonClick: () -> Unit
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
            }
        }
    }

    LaunchedEffect(true) {
        viewModel.onAction(OverviewAction.GetLatestAuctions)
    }


    OverviewScreen(
        state = viewModel.state,
        onAction = viewModel::onAction,
        onAuctionClick = onAuctionClick,
        onFloatingButtonClick = onFloatingButtonClick
    )
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    state: OverviewState,
    onAction: (OverviewAction) -> Unit,
    onAuctionClick: (String) -> Unit,
    onFloatingButtonClick: () -> Unit
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
                            text = "Auctions",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(end = 18.dp)
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
            Column (
                modifier = Modifier
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            )
            {
                LatestAuctions(modifier = Modifier.padding(paddingValues), state = state, onAuctionClick = onAuctionClick) }

        } }
    }
}

@Composable
private fun LatestAuctions(
    modifier: Modifier = Modifier,
    state: OverviewState,
    onAuctionClick: (String) -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Latest",
            style = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
            modifier = Modifier.padding(top = 12.dp, start = 16.dp, bottom = 8.dp)
        )

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
                    itemsIndexed(state.latestAuctions) { _, auction ->
                        AuctionItem(auction = auction, onClick = { onAuctionClick(auction.id!!) })
                    }
                }
            }
        }
    }
}


@Composable
fun AuctionItem(auction: Auction, onClick: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 16.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(26.dp)
        ) {
            Text(
                text = auction.title,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
//            Text(
//                text = "$${auction.bids[1].userId}",
//                style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
//            )

        }

    }
}

@Preview
@Composable
private fun OverviewScreenPreview() {
    AuctionsAppTheme {
        OverviewScreen(
            state = OverviewState(),
            onAction = {},
            onAuctionClick = {},
            onFloatingButtonClick = {}

        )
    }
}

