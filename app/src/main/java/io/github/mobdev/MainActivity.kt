package io.github.mobdev

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.mobdev.ui.ChatViewModel
import io.github.mobdev.ui.screens.FullscreenImageScreen
import io.github.mobdev.ui.screens.LoginScreen
import io.github.mobdev.ui.screens.MainScreen
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkTheme = isSystemInDarkTheme()
            val colors = if (isDarkTheme) darkColorScheme() else lightColorScheme()

            MaterialTheme(colorScheme = colors) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: ChatViewModel) {
    val navController = rememberNavController()
    val requireAuth by viewModel.requireAuth.collectAsState()

    if (requireAuth == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }
        return
    }

    LaunchedEffect(requireAuth) {
        if (requireAuth == true) {
            navController.navigate("login") { popUpTo(0) }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (requireAuth == true) "login" else "main",
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }
    ) {
        composable("login") {
            LoginScreen(viewModel = viewModel) {
                navController.navigate("main") { popUpTo("login") { inclusive = true } }
            }
        }

        composable("main") {
            MainScreen(
                viewModel = viewModel,
                onImageClick = { link -> navController.navigate("image/${link}") }
            )
        }

        composable("image/{link}") { backStackEntry ->
            val encodedLink = backStackEntry.arguments?.getString("link") ?: ""
            val decodedLink = URLDecoder.decode(encodedLink, StandardCharsets.UTF_8.toString())
            FullscreenImageScreen(
                imageLink = decodedLink,
                onBack = { navController.popBackStack() }
            )
        }
    }
}