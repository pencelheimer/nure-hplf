package com.social.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.social.app.ui.AppViewModel
import com.social.app.ui.screen.*

@Composable
fun AppNavigation(
    navController: NavHostController,
    appViewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(navController = navController, startDestination = "feed", modifier = modifier) {
        composable("feed") {
            FeedScreen(appViewModel = appViewModel, navController = navController)
        }
        composable(
            "profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { back ->
            ProfileScreen(
                userId = back.arguments!!.getInt("userId"),
                appViewModel = appViewModel,
                navController = navController
            )
        }
        composable(
            "post/{postId}",
            arguments = listOf(navArgument("postId") { type = NavType.IntType })
        ) { back ->
            PostScreen(
                postId = back.arguments!!.getInt("postId"),
                appViewModel = appViewModel,
                navController = navController
            )
        }
        composable("messages") {
            MessagesScreen(activeUserId = null, appViewModel = appViewModel, navController = navController)
        }
        composable(
            "messages/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { back ->
            MessagesScreen(
                activeUserId = back.arguments!!.getInt("userId"),
                appViewModel = appViewModel,
                navController = navController
            )
        }
        composable("search") {
            SearchScreen(appViewModel = appViewModel, navController = navController)
        }
    }
}
