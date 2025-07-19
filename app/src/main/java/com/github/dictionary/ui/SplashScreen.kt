package com.github.dictionary.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController

@Composable
fun SplashScreen(navController: NavHostController) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        if (checkInputMethodSettingsActive(context)) {
            navController.navigate("home") {
                popUpTo(0) {
                    inclusive = true
                }
            }
        } else {
            navController.navigate("ime_permission") {
                popUpTo(0) {
                    inclusive = true
                }
            }
        }
        onDispose { }
    }
    Scaffold {
        Box(Modifier.padding(it)) {

        }
    }
}