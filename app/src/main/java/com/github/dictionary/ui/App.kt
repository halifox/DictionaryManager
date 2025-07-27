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
            composable<PermissionCheck> { PermissionCheckScreen(navController) }
            composable<Home> { HomeScreen(navController) }
            composable<Dictionary> { DictionaryScreen(navController, it.toRoute()) }
            composable<DictionaryDetail> { DictionaryDetailScreen(it.toRoute()) }
            composable<LocalDictionary> { LocalDictionaryScreen(navController) }
        }
    }
}


@Serializable
object Splash

@Serializable
object Home

@Serializable
object PermissionCheck

@Serializable
object LocalDictionary

@Serializable
data class Dictionary(val source: String)

@Serializable
data class DictionaryDetail(val id: Int)

