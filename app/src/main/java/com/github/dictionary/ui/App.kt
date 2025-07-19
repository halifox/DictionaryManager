package com.github.dictionary.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.dictionary.ui.theme.DictionaryTheme
import okio.`-DeprecatedOkio`.source

@Composable
fun App() {
    DictionaryTheme {
        val navController = rememberNavController()
        NavHost(navController, "splash") {
            composable("splash") { SplashScreen(navController) }
            composable("ime_permission") { PermissionCheckScreen(navController) }
            composable("home") { HomeScreen(navController) }
            composable("dictionary/{source}") { DictionaryScreen(navController, (it.arguments?.getString("source")).orEmpty()) }
        }
    }
}
