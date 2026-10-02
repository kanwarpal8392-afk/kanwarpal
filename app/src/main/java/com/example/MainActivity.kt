package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.data.HistoryRepository
import com.example.ui.components.CalcoraBottomNav
import com.example.ui.components.CalcoraTopBar
import com.example.ui.components.NavDestination
import com.example.ui.screens.*
import com.example.ui.theme.CalcoraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = HistoryRepository.getInstance(applicationContext)

        setContent {
            val preferences by repository.preferences.collectAsState()

            val isDarkTheme = when (preferences.darkMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            CalcoraTheme(darkTheme = isDarkTheme) {
                var currentDestination by remember { mutableStateOf(NavDestination.HOME) }
                var selectedHubCategory by remember { mutableStateOf(HubCategory.FINANCE) }
                var selectedConverterCategory by remember { mutableStateOf("length") }
                var pendingAiQuery by remember { mutableStateOf("") }

                // System BackHandler: if not on HOME, return to HOME
                BackHandler(enabled = currentDestination != NavDestination.HOME) {
                    currentDestination = NavDestination.HOME
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CalcoraTopBar(
                            title = "CALCORA",
                            subtitle = "Calculate Anything. Convert Everything.",
                            showBack = currentDestination != NavDestination.HOME,
                            onBackClick = { currentDestination = NavDestination.HOME },
                            onSearchClick = { currentDestination = NavDestination.HOME },
                            onSettingsClick = { currentDestination = NavDestination.HISTORY },
                            onAiClick = {
                                selectedHubCategory = HubCategory.AI_SOLVER
                                currentDestination = NavDestination.HUBS
                            }
                        )
                    },
                    bottomBar = {
                        CalcoraBottomNav(
                            currentDestination = currentDestination,
                            onNavigate = { destination ->
                                currentDestination = destination
                            }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentDestination) {
                            NavDestination.HOME -> {
                                HomeScreen(
                                    repository = repository,
                                    onNavigateDestination = { dest ->
                                        currentDestination = dest
                                    },
                                    onNavigateHubCategory = { cat ->
                                        selectedHubCategory = cat
                                        currentDestination = NavDestination.HUBS
                                    },
                                    onNavigateConverter = { catId ->
                                        selectedConverterCategory = catId
                                        currentDestination = NavDestination.CONVERT
                                    }
                                )
                            }
                            NavDestination.CALCULATOR -> {
                                CalculatorScreen(
                                    repository = repository,
                                    onOpenAiSolverWithQuery = { q ->
                                        pendingAiQuery = q
                                        selectedHubCategory = HubCategory.AI_SOLVER
                                        currentDestination = NavDestination.HUBS
                                    }
                                )
                            }
                            NavDestination.GRAPHING -> {
                                GraphingScreen()
                            }
                            NavDestination.CONVERT -> {
                                ConverterScreen(
                                    repository = repository,
                                    initialCategoryId = selectedConverterCategory
                                )
                            }
                            NavDestination.HUBS -> {
                                HubsScreen(
                                    repository = repository,
                                    initialTab = selectedHubCategory,
                                    initialAiQuery = pendingAiQuery
                                )
                            }
                            NavDestination.HISTORY -> {
                                HistorySettingsScreen(
                                    repository = repository
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
