    package com.example.auctionsapp.core.presentation

    import AuctionDetailsScreenCore
    import AuctionFormScreenCore
    import android.os.Bundle
    import android.util.Log
    import androidx.activity.ComponentActivity
    import androidx.activity.compose.setContent
    import androidx.activity.enableEdgeToEdge
    import androidx.compose.runtime.Composable
    import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
    import androidx.navigation.NavType
    import androidx.navigation.compose.NavHost
    import androidx.navigation.compose.composable
    import androidx.navigation.compose.rememberNavController
    import androidx.navigation.navArgument
    import com.example.auctionsapp.authentication.presentation.AuthenticationScreenCore
    import com.example.auctionsapp.authentication.presentation.AuthenticationViewModel
    import com.example.auctionsapp.core.presentation.ui.theme.AuctionsAppTheme
    import com.example.auctionsapp.core.presentation.util.Screen
    import com.example.auctionsapp.overview.presentation.OverviewScreenCore
    import org.koin.androidx.viewmodel.ext.android.viewModel


    class MainActivity : ComponentActivity() {

        private val authenticationViewModel: AuthenticationViewModel by viewModel()

        override fun onCreate(savedInstanceState: Bundle?) {

            installSplashScreen()
                .apply {
                    setKeepOnScreenCondition{
                        authenticationViewModel.isSplashScreenAppear.value
                    }
                }

            super.onCreate(savedInstanceState)
            if (savedInstanceState != null) {
                Log.d("MainActivity", "onCreate - restored from savedInstanceState")
            } else {
                Log.d("MainActivity", "onCreate - new instance")
            }
            enableEdgeToEdge()
            setContent {
                AuctionsAppTheme {
                    AppNavigation()
                }
            }
        }

        override fun onStart() {
            super.onStart()
            Log.d("MainActivity", "onStart - Aktywność widoczna, ale nie w pełni aktywna")
        }

        override fun onResume() {
            super.onResume()
            Log.d("MainActivity", "onResume - Aktywność w pełni aktywna")

            // Ustawienie interfejsu użytkownika, jeśli powrócisz do aplikacji
            setContent {
                AuctionsAppTheme {
                    AppNavigation()
                }
            }
        }

        override fun onPause() {
            super.onPause()
            Log.d("MainActivity", "onPause - Aktywność traci fokus")
        }

        override fun onStop() {
            super.onStop()
            Log.d("MainActivity", "onStop - Aktywność przestała być widoczna")
        }

        override fun onDestroy() {
            super.onDestroy()
            Log.d("MainActivity", "onDestroy - Aktywność zniszczona")
        }

    }


    @Composable
    fun AppNavigation() {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = Screen.AuthenticationScreen,
        ) {
            composable<Screen.AuthenticationScreen> {
                AuthenticationScreenCore(
                    onNavigateAfterAuthentication = {
                        navController.navigate(Screen.OverviewScreen) {
                            popUpTo(Screen.AuthenticationScreen) { inclusive = true }
                        }
                    }
                )
            }
            composable<Screen.OverviewScreen> {
                OverviewScreenCore(
                    onBackAfterSignOut = {
                        navController.navigate(Screen.AuthenticationScreen) {
                            popUpTo(Screen.OverviewScreen) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onAuctionClick = { auctionId ->
                        navController.navigate("AuctionDetailsScreen/$auctionId")
                    },
                    onFloatingButtonClick = {
                        navController.navigate("AuctionFormScreen/new")
                    }
                )
            }

            composable(
                route = "AuctionDetailsScreen/{auctionId}",
                arguments = listOf(navArgument("auctionId") { type = NavType.StringType })
            ) { backStackEntry ->
                val auctionId = backStackEntry.arguments?.getString("auctionId") ?: ""
                AuctionDetailsScreenCore(auctionId = auctionId)
            }
            composable(
                route = "AuctionFormScreen/{auctionId}",
                arguments = listOf(navArgument("auctionId") { type = NavType.StringType })
            ) { backStackEntry ->
                val auctionId = backStackEntry.arguments?.getString("auctionId") ?: ""
                AuctionFormScreenCore(
                    auctionId = auctionId,
                    onCancel = { navController.popBackStack() },
                    onNavigateAfterSuccess = { newAuctionId ->
                        navController.navigate("AuctionDetailsScreen/$newAuctionId") {
                            popUpTo("AuctionFormScreen/$auctionId") { inclusive = true }
                        }

            }
                )
            }

        }
    }













