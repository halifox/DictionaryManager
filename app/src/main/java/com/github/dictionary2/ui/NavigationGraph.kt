package com.github.dictionary2.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.dictionary2.ui.theme.DictionaryTheme
import org.koin.compose.koinInject

val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("NavController not provided")
}


@Composable
fun App() {
    DictionaryTheme {
        CompositionLocalProvider(LocalNavController provides rememberNavController()) {
            NavigationGraph()
        }
    }
}

@SuppressLint("RestrictedApi")
@Composable
private fun NavigationGraph() {
    val context: Context = koinInject()
    val inputMethodManager: InputMethodManager = koinInject()
    val startDestination = remember { if (checkInputMethodSettingsActive(context, inputMethodManager)) "/home" else "/ime_request_permission" }
    val navController = LocalNavController.current

    val navBackStackEntries by navController.currentBackStack.collectAsState()
    Log.d("TAG", "NavigationGraph:${navBackStackEntries.map { it.destination.route }.toList()} ")
    NavHost(navController, startDestination = startDestination) {
        composable("/home") {
            HomeScreen()
        }
        composable("/ime_request_permission") {
            ImeRequestPermissionScreen()
        }
        composable("/dictionary_index_screen") {
            DictionaryIndexScreen()
        }
    }
}


