package com.chargercontrol

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chargercontrol.service.ChargingService
import com.chargercontrol.ui.screens.HomeScreen
import com.chargercontrol.ui.screens.SettingsScreen
import com.chargercontrol.ui.screens.StatusScreen
import com.chargercontrol.ui.theme.ChargerControlTheme
import com.chargercontrol.utils.RootUtils

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        RootUtils.isRootGranted()
        startForegroundService(Intent(this, ChargingService::class.java))

        setContent {
            ChargerControlTheme {
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Home, null) },
                                label = { Text("Home") },
                                selected = false,
                                onClick = { navController.navigate("home") }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Info, null) },
                                label = { Text("Status") },
                                selected = false,
                                onClick = { navController.navigate("status") }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Settings, null) },
                                label = { Text("Settings") },
                                selected = false,
                                onClick = { navController.navigate("settings") }
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
