
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.auctionsapp.auction_details.presentation.AuctionDetailsAction
import com.example.auctionsapp.auction_details.presentation.AuctionDetailsEvent
import com.example.auctionsapp.auction_details.presentation.AuctionDetailsState
import com.example.auctionsapp.overview.presentation.AuctionDetailsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AuctionDetailsScreenCore(
    viewModel: AuctionDetailsViewModel = koinViewModel(),
    auctionId: String,
) {
    LaunchedEffect(Unit) {
        println("AUCTION DETAILS")
        viewModel.event.collect { event ->
            when (event) {
                AuctionDetailsEvent.GetAuctionInfoSuccess -> {}
                AuctionDetailsEvent.GetAuctionInfoFailure -> {}
            }
        }
    }
    AuctionDetailsScreen(
        state = viewModel.state,
        onAction = viewModel::onAction,
    )

}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuctionDetailsScreen(
    state: AuctionDetailsState,
    onAction: (AuctionDetailsAction) -> Unit,
) {
    Scaffold(

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Auction Details",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
              text = state.auction.title,
                color = Color.Black,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
        }
    }
}