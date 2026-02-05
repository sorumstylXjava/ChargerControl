package com.chargercontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.chargercontrol.ui.screens.HomeScreen
import com.chargercontrol.ui.screens.SettingsScreen
import com.chargercontrol.ui.screens.StatusScreen
import com.chargercontrol.ui.theme.ChargerControlTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChargerControlTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = Color(0xFF1E1E1E)
                        ) {
                            NavigationBarItem(
                                icon = { Icon(Icons.Rounded.Home, contentDescription = null) },
                                label = { Text("Home") },
                                selected = currentRoute == "home",
                                onClick = {
                                    navController.navigate("home") {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF00E676),
                                    selectedTextColor = Color(0xFF00E676),
                                    indicatorColor = Color.Transparent,
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray
                                )
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Rounded.Bolt, contentDescription = null) },
                                label = { Text("Status") },
                                selected = currentRoute == "status",
                                onClick = {
                                    navController.navigate("status") {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF00E676),
                                    selectedTextColor = Color(0xFF00E676),
                                    indicatorColor = Color.Transparent,
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray
                                )
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Rounded.Settings, contentDescription = null) },
                                label = { Text("Settings") },
                                selected = currentRoute == "settings",
                                onClick = {
                                    navController.navigate("settings") {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF00E676),
                                    selectedTextColor = Color(0xFF00E676),
                                    indicatorColor = Color.Transparent,
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray
                                )
                            )
                        }
                    }
                ) { padding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(padding)
                    ) {
                        composable("home") { HomeScreen() }
                        composable("status") { StatusScreen() }
                        composable("settings") { SettingsScreen() }
                    }
                }
            }
        }
    }
}
