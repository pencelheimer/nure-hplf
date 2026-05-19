package com.pz3.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pz3.app.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme { MainApp() }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == "hello",
                    onClick = {
                        navController.navigate("hello") {
                            launchSingleTop = true
                            popUpTo("hello") { inclusive = false }
                        }
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Привіт") }
                )
                NavigationBarItem(
                    selected = currentRoute == "todo",
                    onClick = { navController.navigate("todo") { launchSingleTop = true } },
                    icon = { Icon(Icons.Default.CheckBox, contentDescription = null) },
                    label = { Text("ToDo") }
                )
                NavigationBarItem(
                    selected = currentRoute == "weather",
                    onClick = { navController.navigate("weather") { launchSingleTop = true } },
                    icon = { Icon(Icons.Default.Cloud, contentDescription = null) },
                    label = { Text("Погода") }
                )
                NavigationBarItem(
                    selected = currentRoute == "recipes",
                    onClick = { navController.navigate("recipes") { launchSingleTop = true } },
                    icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null) },
                    label = { Text("Рецепти") }
                )
            }
        }
    ) { innerPadding ->
        AppNavigation(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
