package com.esheeq.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.esheeq.app.ui.home.HomeScreen
import com.esheeq.app.ui.player.PlayerScreen
import com.esheeq.app.ui.series.SeriesDetailScreen
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object NavRoutes {
    const val HOME = "home"
    const val SERIES_DETAIL = "series/{seriesId}"
    const val PLAYER = "player?url={url}&title={title}"

    fun seriesDetail(seriesId: String): String = "series/$seriesId"

    fun player(url: String, title: String): String {
        val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
        val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
        return "player?url=$encodedUrl&title=$encodedTitle"
    }
}

@Composable
fun EsheeqNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.HOME,
        modifier = modifier
    ) {
        composable(NavRoutes.HOME) {
            HomeScreen(
                onNavigateToSeries = { seriesId ->
                    navController.navigate(NavRoutes.seriesDetail(seriesId))
                },
                onNavigateToPlayer = { url, title ->
                    navController.navigate(NavRoutes.player(url, title))
                }
            )
        }

        composable(
            route = NavRoutes.SERIES_DETAIL,
            arguments = listOf(
                navArgument("seriesId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val seriesId = backStackEntry.arguments?.getString("seriesId") ?: ""
            SeriesDetailScreen(
                seriesId = seriesId,
                onBackClick = { navController.popBackStack() },
                onNavigateToPlayer = { url, title ->
                    navController.navigate(NavRoutes.player(url, title))
                }
            )
        }

        composable(
            route = NavRoutes.PLAYER,
            arguments = listOf(
                navArgument("url") { type = NavType.StringType; defaultValue = "" },
                navArgument("title") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val rawUrl = backStackEntry.arguments?.getString("url") ?: ""
            val rawTitle = backStackEntry.arguments?.getString("title") ?: ""
            val decodedUrl = URLDecoder.decode(rawUrl, StandardCharsets.UTF_8.toString())
            val decodedTitle = URLDecoder.decode(rawTitle, StandardCharsets.UTF_8.toString())

            PlayerScreen(
                videoUrl = decodedUrl,
                title = decodedTitle,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
