package com.github.dictionary.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.github.dictionary.ui.theme.DictionaryTheme
import kotlinx.serialization.Serializable

@Composable
fun App() {
    DictionaryTheme {
        val navController = rememberNavController()
        NavHost(navController, Splash) {
            composable<Splash> { SplashScreen(navController) }
            composable<ImePermission> { PermissionCheckScreen(navController) }
            composable<Home> { HomeScreen(navController) }
            composable<Dictionary> { DictionaryScreen(navController, it.toRoute()) }
            composable<Install> { InstallScreen(it.toRoute()) }
        }
    }
}


@Serializable
object Splash

@Serializable
object Home

@Serializable
object ImePermission

@Serializable
data class Dictionary(val source: String)

@Serializable
data class Install(val id: Int)

