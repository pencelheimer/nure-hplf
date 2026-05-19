package com.pz3.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pz3.app.ui.screen.HelloScreen
import com.pz3.app.ui.screen.RecipesScreen
import com.pz3.app.ui.screen.TodoScreen
import com.pz3.app.ui.screen.WeatherScreen

@Composable
fun AppNavigation(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = "hello", modifier = modifier) {
        composable("hello") { HelloScreen() }
        composable("todo") { TodoScreen() }
        composable("weather") { WeatherScreen() }
        composable("recipes") { RecipesScreen() }
    }
}
