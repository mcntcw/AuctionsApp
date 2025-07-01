
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.auctionsapp.auction_form.presentation.AuctionFormAction
import com.example.auctionsapp.auction_form.presentation.AuctionFormEvent
import com.example.auctionsapp.auction_form.presentation.AuctionFormState
import com.example.auctionsapp.auction_form.presentation.AuctionFormViewModel
import com.example.auctionsapp.core.domain.AuctionCategory
import com.example.auctionsapp.overview.presentation.OverviewAction
import com.example.auctionsapp.overview.presentation.OverviewViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel

@Composable
fun AuctionFormScreenCore(
    viewModel: AuctionFormViewModel = koinViewModel(),
    auctionId: String,
    onCancel: () -> Unit,
    onNavigateAfterSuccess: (String) -> Unit,
    overviewViewModel: OverviewViewModel = koinViewModel(),
) {

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is AuctionFormEvent.SaveAuctionSuccess -> {
                    overviewViewModel.onAction(OverviewAction.GetLatestAuctions)
                    onNavigateAfterSuccess(event.auctionId)
                    val message = if (viewModel.state.isEditMode) {
                        "Auction successfully edited"
                    } else {
                        "Auction successfully created"
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
                is AuctionFormEvent.SaveAuctionFailure -> {}
                is AuctionFormEvent.GetAuctionInfoSuccess -> {}
                is AuctionFormEvent.GetAuctionInfoFailure -> {}
                is AuctionFormEvent.ShowValidationToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    AuctionFormScreen(
        state = viewModel.state,
        onAction = viewModel::onAction,
        onCancel = onCancel,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuctionFormScreen(
    state: AuctionFormState,
    onAction: (AuctionFormAction) -> Unit,
    onCancel: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.isEditMode) "Edit Auction" else "Create New Auction",
                        style = MaterialTheme.typography.displayLarge,

                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(0f),
                )
            )
        },

    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            CategoryDropdown(
                selected = state.auction.category,
                onCategorySelected = { newCategory ->
                    onAction(AuctionFormAction.UpdateAuctionField(state.auction.copy(category = newCategory)))
                }
            )


            AuctionOutlinedTextField(
                value = state.auction.title,
                onValueChange = { newTitle ->
                    onAction(AuctionFormAction.UpdateAuctionField(state.auction.copy(title = newTitle)))
                },
                label = "Title",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            AuctionOutlinedTextField(
                value = state.auction.description ?: "",
                onValueChange = { newDescription ->
                    onAction(AuctionFormAction.UpdateAuctionField(state.auction.copy(description = newDescription)))
                },
                label = "Description",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(vertical = 8.dp),
                singleLine = false
            )

            
            fun formatPhoneNumber(number: String): String {
                return number.chunked(3).joinToString(" ")
            }
            AuctionOutlinedTextField(
                value = formatPhoneNumber(state.auction.phoneNumber),
                onValueChange = { formattedInput ->
                    
                    val rawNumber = formattedInput.replace(" ", "")
                    onAction(AuctionFormAction.UpdateAuctionField(state.auction.copy(phoneNumber = rawNumber)))
                },
                label = "Phone Number",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            
            AuctionOutlinedTextField(
                value = if (state.auction.buyNowPrice > 0) state.auction.buyNowPrice.toString() else "",
                onValueChange = { newBuyNowPrice ->
                    val price = newBuyNowPrice.toDoubleOrNull() ?: 0.0
                    onAction(AuctionFormAction.UpdateAuctionField(state.auction.copy(buyNowPrice = price)))
                },
                label = "Buy Now Price",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )


        
            AuctionEndDatePicker(
                endTime = state.auction.endTime,
                onDateSelected = { newEndTime ->
                    onAction(AuctionFormAction.UpdateAuctionField(state.auction.copy(endTime = newEndTime)))
                }
            )

            GallerySection(
                galleryUrls = state.auction.galleryUrls,
                onAddImage = { imageUrl ->
                    val updatedUrls = state.auction.galleryUrls.toMutableList().apply { add(imageUrl) }
                    onAction(AuctionFormAction.UpdateAuctionField(state.auction.copy(galleryUrls = updatedUrls)))
                },
                onRemoveImage = { imageUrl ->
                    val updatedUrls = state.auction.galleryUrls.toMutableList().apply { remove(imageUrl) }
                    onAction(AuctionFormAction.UpdateAuctionField(state.auction.copy(galleryUrls = updatedUrls)))
                }
            )


            Spacer(modifier = Modifier.weight(1f))

            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary),
                    )
                }

                Button(
                    onClick = { onAction(AuctionFormAction.SaveAuction) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (state.isEditMode) "Update" else "Create",
                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.background, fontWeight = FontWeight.Bold),
                    )

                }
            }

            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun AuctionOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    OutlinedTextField(
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Normal),
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), fontWeight = FontWeight.Light),
            )
        },

        modifier = modifier,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedLabelColor = MaterialTheme.colorScheme.primary
        )
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuctionEndDatePicker(
    endTime: Instant,
    onDateSelected: (Instant) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    val textStyle = MaterialTheme.typography.bodyLarge

    val textColor = if (endTime == Instant.DISTANT_FUTURE) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
    } else {
        MaterialTheme.colorScheme.primary
    }

    
    val endTimeString = remember(endTime) {
        if (endTime == Instant.DISTANT_FUTURE) {
            "End Date"
        } else {
            val localDate = endTime.toLocalDateTime(TimeZone.currentSystemDefault()).date
            "%04d-%02d-%02d".format(
                localDate.year,
                localDate.monthNumber,
                localDate.dayOfMonth
            )
        }
    }

    OutlinedButton(
        onClick = { showDatePicker = true },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(endTimeString, style = textStyle, color = textColor)
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            val localDate = Instant.fromEpochMilliseconds(selectedMillis)
                                .toLocalDateTime(TimeZone.currentSystemDefault()).date
                            val localDateTime = LocalDateTime(
                                year = localDate.year,
                                monthNumber = localDate.monthNumber,
                                dayOfMonth = localDate.dayOfMonth,
                                hour = 23,
                                minute = 59,
                                second = 59
                            )
                            val selectedInstant = localDateTime.toInstant(TimeZone.currentSystemDefault())
                            onDateSelected(selectedInstant)
                        }
                        showDatePicker = false
                    }
                ) { Text("OK", style = textStyle) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", style = textStyle) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}


@Composable
fun GallerySection(
    galleryUrls: List<String>,
    onAddImage: (String) -> Unit,
    onRemoveImage: (String) -> Unit
) {
    val context = LocalContext.current
    var lastPickedImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            onAddImage(it.toString())
            lastPickedImageUri = it
        }
    }

    Text(
        text = "Gallery",
        style = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(vertical = 8.dp)
    ) {
        items(galleryUrls) { imageUrl ->
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .padding(end = 8.dp)
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Gallery image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                )
                IconButton(
                    onClick = { onRemoveImage(imageUrl) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(24.dp)
                        .background(Color.White.copy(alpha = 0.7f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove image",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        item {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .padding(end = 8.dp)
                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                    .clickable {
                        
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )

                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add image",
                    tint = Color.Gray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    selected: AuctionCategory,
    onCategorySelected: (AuctionCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val categories = AuctionCategory.values()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        OutlinedTextField(
            readOnly = true,
            value = selected.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() },
            onValueChange = {},
            label = { Text("Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = {
                        Text(category.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() })
                    },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}