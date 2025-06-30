    package com.example.auctionsapp.core.presentation

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
import com.example.auctionsapp.auction_details.presentation.AuctionDetailsScreenCore
    import com.example.auctionsapp.auctions_list.presentation.AuctionsListScreenCore
    import com.example.auctionsapp.authentication.presentation.AuthenticationScreenCore
import com.example.auctionsapp.authentication.presentation.AuthenticationViewModel
import com.example.auctionsapp.core.presentation.ui.theme.AuctionsAppTheme
import com.example.auctionsapp.core.presentation.util.Screen
    import com.example.auctionsapp.notifications.presentation.NotificationsScreenCore
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
                        navController.navigate("com.example.auctionsapp.auction_details.presentation.AuctionDetailsScreen/$auctionId")
                    },
                    onFloatingButtonClick = {
                        navController.navigate("AuctionFormScreen/new")
                    },
                    onCategoryClick = { categoryName ->
                        println(categoryName)
                        navController.navigate("auctions_list?mode=category&category=${categoryName}")
                    },
                    onNavigateToUserAuctions = { sellerId ->
                        navController.navigate("auctions_list?mode=user&category=&userId=${sellerId}&query=")
                    },
                    onNavigateToUserBids = { sellerId ->
                        navController.navigate("auctions_list?mode=bids&category=&userId=${sellerId}&query=")
                    },
                    onNavigateToNotifications = {
                        navController.navigate("notifications_screen")
                    },
                    onNavigateAfterSearch = { query ->
                        navController.navigate("auctions_list?mode=search&category=&userId=&query=${query}")
                    },
                    onLatestClick = {
                        navController.navigate("auctions_list?mode=latest&category=&userId=&query=")
                    },
                )
            }

            composable(
                route = "com.example.auctionsapp.auction_details.presentation.AuctionDetailsScreen/{auctionId}",
                arguments = listOf(navArgument("auctionId") { type = NavType.StringType })
            ) { backStackEntry ->
                val auctionId = backStackEntry.arguments?.getString("auctionId") ?: ""
                AuctionDetailsScreenCore(
                    auctionId = auctionId,
                    onBack = { navController.popBackStack() },
                    onSellerClick = { sellerId ->
                        navController.navigate("auctions_list?mode=user&category=&userId=${sellerId}&query=")
                    }
                )
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
                        navController.navigate("com.example.auctionsapp.auction_details.presentation.AuctionDetailsScreen/$newAuctionId") {
                            popUpTo("AuctionFormScreen/$auctionId") { inclusive = true }
                        }

            }
                )
            }

            composable(
                route = "auctions_list?mode={mode}&category={category}&userId={userId}&query={query}",
                arguments = listOf(
                    navArgument("mode") { type = NavType.StringType; defaultValue = "" },
                    navArgument("category") { type = NavType.StringType; defaultValue = "" },
                    navArgument("userId") { type = NavType.StringType; defaultValue = "" },
                    navArgument("query") { type = NavType.StringType; defaultValue = "" },
                )
            ) { backStackEntry ->
                val mode = backStackEntry.arguments?.getString("mode") ?: ""
                val category = backStackEntry.arguments?.getString("category") ?: ""
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                val query = backStackEntry.arguments?.getString("query") ?: ""
                AuctionsListScreenCore(
                    mode = mode,
                    category = category,
                    userId = userId,
                    query = query,
                    onBack = { navController.popBackStack() },
                    onAuctionClick = { auctionId ->
                        navController.navigate("com.example.auctionsapp.auction_details.presentation.AuctionDetailsScreen/$auctionId")
                    }
                )
            }

            composable("notifications_screen") {
                NotificationsScreenCore(
                    onBack = {
                        navController.popBackStack()
                    },
                    onAuctionClick = { auctionId ->
                        navController.navigate("com.example.auctionsapp.auction_details.presentation.AuctionDetailsScreen/$auctionId")
                    }
                )
            }




        }
    }













