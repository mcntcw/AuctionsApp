package com.example.auctionsapp.notifications.presentation


import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.auctionsapp.notifications.domain.Notification
import com.example.auctionsapp.notifications.domain.NotificationType
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel

@Composable
fun NotificationsScreenCore(
    viewModel: NotificationsViewModel = koinViewModel(),
    onBack: () -> Unit,
    onAuctionClick: (String) -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is NotificationsEvent.LoadNotificationsSuccess -> {}
                is NotificationsEvent.LoadNotificationsFailure -> {
                    Toast.makeText(context, "Failed to load notifications", Toast.LENGTH_SHORT).show()
                }
                is NotificationsEvent.MarkAsReadSuccess -> {}
                is NotificationsEvent.MarkAsReadFailure -> {
                    Toast.makeText(context, "Failed to mark as read", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    NotificationsScreen(
        state = viewModel.state,
        onAction = viewModel::onAction,
        onBack = onBack,
        onAuctionClick = onAuctionClick
    )
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    state: NotificationsState,
    onAction: (NotificationsAction) -> Unit,
    onBack: () -> Unit,
    onAuctionClick: (String) -> Unit
) {
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "NOTIFICATIONS",
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
                    containerColor = MaterialTheme.colorScheme.background,
                )
            )
        }
    ) { innerPadding ->

        if (state.notifications.isEmpty() && !state.isLoading) {
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                EmptyNotificationsMessage()
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(state.notifications) { index, notification ->
                    NotificationListItem(
                        notification = notification,
                        onClick = {
                            if (!notification.isRead) {
                                onAction(NotificationsAction.MarkAsRead(notification.id ?: ""))
                            }
                            onAuctionClick(notification.auction?.id ?: "")
                        }
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

                if (lastVisible == totalItems - 1 && !state.isLoading && state.hasMore) {
                    onAction(NotificationsAction.LoadNextPage)
                }
            }
    }
}

@Composable
fun NotificationListItem(
    notification: Notification,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead)
                Color.White.copy(0.2f)
            else
               Color.Black.copy(0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            NotificationTypeIcon(type = notification.type)

            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                
                notification.auction?.title?.let { title ->
                    Text(
                        text = "Auction: $title",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                
                Text(
                    text = formatNotificationTime(notification.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            
            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
fun NotificationTypeIcon(type: NotificationType) {
    val (icon, color) = when (type) {
        NotificationType.MY_AUCTION_NEW_BID -> Icons.Default.Notifications to MaterialTheme.colorScheme.primary
        NotificationType.MY_AUCTION_SOLD -> Icons.Default.Notifications to MaterialTheme.colorScheme.tertiary
        NotificationType.MY_AUCTION_ENDED -> Icons.Default.Notifications to MaterialTheme.colorScheme.secondary
        NotificationType.BID_OUTBID -> Icons.Default.Notifications to MaterialTheme.colorScheme.error
        NotificationType.AUCTION_WON -> Icons.Default.Notifications to MaterialTheme.colorScheme.tertiary
        NotificationType.AUCTION_ENDED -> Icons.Default.Notifications to MaterialTheme.colorScheme.secondary
        NotificationType.AUCTION_SOLD -> Icons.Default.Notifications to MaterialTheme.colorScheme.secondary
        NotificationType.OTHER -> Icons.Default.Notifications to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Notification type",
            tint = color,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun EmptyNotificationsMessage() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(32.dp)
    ) {
        Text(
            text = "No notifications yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Text(
            text = "You'll see notifications about your auctions and bids here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}


private fun formatNotificationTime(createdAt: kotlinx.datetime.Instant): String {
    val localDateTime = createdAt.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.dayOfMonth.toString().padStart(2, '0')}-${localDateTime.monthNumber.toString().padStart(2, '0')}-${localDateTime.year.toString().takeLast(2)} ${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"
}
