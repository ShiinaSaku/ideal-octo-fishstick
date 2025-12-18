package dev.sanmer.template.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.sanmer.template.ui.screens.*

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("explore") { ExploreScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
    }
}
