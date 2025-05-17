package com.example.auctionsapp.authentication.presentation

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.auctionsapp.R
import com.example.auctionsapp.core.presentation.ui.theme.AuctionsAppTheme
import com.example.auctionsapp.core.presentation.ui.theme.ClashDisplayFontFamily
import org.koin.androidx.compose.koinViewModel


@Composable
    fun AuthenticationScreenCore(
    viewModel: AuthenticationViewModel = koinViewModel(),
    onNavigateAfterAuthentication: () -> Unit
) {
    val context = LocalContext.current
    Log.d("AuthenticationScreen", "authscreen dziala")
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is AuthenticationEvent.SignInSuccess -> {
                    Log.d("AuthenticationScreen", "UDALO SIE ZALOGOWAC")
                    onNavigateAfterAuthentication()
                    Toast.makeText(context, "Succesfully logged in!", Toast.LENGTH_SHORT).show()
                }
                is AuthenticationEvent.SignInFailure -> {
                    Log.d("AuthenticationScreen", "NIE UDALO SIE ZALOGOWAC")
                    Toast.makeText(context, "Login failed.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(true) {
        viewModel.onAction(AuthenticationAction.IsSignedIn)
    }

    AuthenticationScreen(
        state = viewModel.state,
        onAction = viewModel::onAction
    )
}

@Composable
fun AuthenticationScreen(
    state: AuthenticationState,
    onAction: (AuthenticationAction) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {

        when {
            state.isLoading -> CircularProgressIndicator()
            !state.isSignedIn -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(36.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "App Logo",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .size(72.dp)
                    )

                    Box(
                        modifier = Modifier,
                        contentAlignment = Alignment.Center
                    ) {
                                Button(
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(16.dp),
                                    onClick = { onAction(AuthenticationAction.SignIn) }
                                ) {
                                    Text(
                                        "Login with Google",
                                        fontFamily = ClashDisplayFontFamily,
                                        fontWeight = FontWeight.Normal
                                    )
                                }

                        state.errorMessage?.let { errorMessage ->
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.align(Alignment.BottomCenter)
                            )
                        }
                    }
                }
            }
        }
    }
}



@Preview
@Composable
private fun AuthenticationScreenPreview() {
    AuctionsAppTheme {
        AuthenticationScreen(
            state = AuthenticationState(),
            onAction = {}
        )
    }
}