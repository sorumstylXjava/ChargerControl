package com.chargercontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chargercontrol.ui.components.FloatingNavBar
import com.chargercontrol.ui.screens.*
import com.chargercontrol.ui.theme.ChargerControlTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChargerControlTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    Scaffold(
        containerColor = Color(0xFF121212),
        bottomBar = { FloatingNavBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "status",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen() }
            composable("status") { StatusScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}
