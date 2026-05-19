package com.social.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.social.app.ui.AppViewModel
import com.social.app.ui.screen.LoginScreen
import com.social.app.ui.theme.SocialTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SocialTheme { SocialApp() }
        }
    }
}

@Composable
fun SocialApp() {
    val appViewModel: AppViewModel = viewModel()
    val user by appViewModel.user.collectAsState()
    val unreadCount by appViewModel.unreadCount.collectAsState()

    if (user == null) {
        LoginScreen(appViewModel = appViewModel)
        return
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == "feed",
                    onClick = {
                        navController.navigate("feed") {
                            launchSingleTop = true
                            popUpTo("feed") { inclusive = false }
                        }
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Feed") }
                )
                NavigationBarItem(
                    selected = currentRoute?.startsWith("messages") == true,
                    onClick = { navController.navigate("messages") { launchSingleTop = true } },
                    icon = {
                        BadgedBox(badge = {
                            if (unreadCount > 0) Badge { Text(unreadCount.toString()) }
                        }) {
                            Icon(Icons.Outlined.Message, contentDescription = null)
                        }
                    },
                    label = { Text("Messages") }
                )
                NavigationBarItem(
                    selected = currentRoute == "search",
                    onClick = { navController.navigate("search") { launchSingleTop = true } },
                    icon = { Icon(Icons.Default.Search, contentDescription = null) },
                    label = { Text("Search") }
                )
                NavigationBarItem(
                    selected = currentRoute == "profile/${user?.id}",
                    onClick = { navController.navigate("profile/${user?.id}") { launchSingleTop = true } },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text(user?.username ?: "") }
                )
            }
        }
    ) { innerPadding ->
        AppNavigation(
            navController = navController,
            appViewModel = appViewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
