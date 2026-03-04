package com.example.coba.ui.navigation

import android.net.Uri
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.coba.ui.screens.download.DownloadRoute
import com.example.coba.ui.screens.home.HomeRoute
import com.example.coba.ui.screens.player.VideoPlayerRoute
import com.example.coba.ui.screens.splash.SplashRoute
import com.example.coba.ui.screens.welcome.WelcomeRoute

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Destination.SPLASH,
        enterTransition = { fadeIn(tween(250)) },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(250)) },
        popExitTransition = { fadeOut(tween(200)) }
    ) {
        composable(Destination.SPLASH) {
            SplashRoute(
                onFinished = {
                    navController.navigate(Destination.WELCOME) {
                        popUpTo(Destination.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Destination.WELCOME) {
            WelcomeRoute(
                onMasukClick = {
                    navController.navigate(Destination.HOME) {
                        popUpTo(Destination.WELCOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Destination.HOME) {
            HomeRoute(
                onOpenDownload = { fileItem ->
                    val payload = NavPayload.encode(NavPayload.fromItem(fileItem))
                    navController.navigate(Destination.downloadRoute(payload))
                }
            )
        }

        composable(
            route = Destination.DOWNLOAD,
            arguments = listOf(
                navArgument(Destination.DOWNLOAD_ARG) { type = NavType.StringType }
            )
        ) {
            DownloadRoute(
                onBack = { navController.popBackStack() },
                onPlay = { url, filename ->
                    navController.navigate(Destination.videoRoute(url, filename))
                }
            )
        }

        composable(
            route = Destination.VIDEO_PLAYER,
            arguments = listOf(
                navArgument(Destination.VIDEO_URL_ARG) { type = NavType.StringType },
                navArgument(Destination.VIDEO_NAME_ARG) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString(Destination.VIDEO_URL_ARG).orEmpty()
            val encodedName = backStackEntry.arguments?.getString(Destination.VIDEO_NAME_ARG).orEmpty()
            VideoPlayerRoute(
                url = Uri.decode(encodedUrl),
                filename = Uri.decode(encodedName),
                onBack = { navController.popBackStack() }
            )
        }
    }
}
