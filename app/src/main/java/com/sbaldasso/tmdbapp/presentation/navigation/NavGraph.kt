package com.sbaldasso.tmdbapp.presentation.navigation

import com.sbaldasso.tmdbapp.presentation.screen.favorites.FavoritesScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sbaldasso.tmdbapp.presentation.screen.details.DetailsScreen
import com.sbaldasso.tmdbapp.presentation.screen.home.HomeScreen
import com.sbaldasso.tmdbapp.presentation.screen.search.SearchScreen
import com.sbaldasso.tmdbapp.presentation.screen.splash.SplashScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onMovieClick = { movieId ->
                    navController.navigate(Screen.Details.createRoute(movieId))
                },
                onFavoritesClick = { navController.navigate(Screen.Favorites.route) },
                onSearchClick = {
                    navController.navigate(Screen.Search.route)
                }
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onBackClick = { navController.navigateUp() },
                onMovieClick = { navController.navigate(Screen.Details.createRoute(it)) }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onBackClick = { navController.navigateUp() },
                onMovieClick = { movieId ->
                    navController.navigate(Screen.Details.createRoute(movieId))
                }
            )
        }

        composable(
            route = Screen.Details.route,
            arguments = listOf(
                navArgument("movieId") { type = NavType.IntType }
            )
        ) {
            DetailsScreen(
                onBackClick = { navController.navigateUp() }
            )
        }
    }
}