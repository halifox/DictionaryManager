package com.github.dictionary2

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.dictionary.IMEService
import com.github.dictionary2.ui.theme.DictionaryTheme
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            App()
        }
    }
}


private fun checkInputMethodSettingsActive(
    context: Context,
    inputMethodManager: InputMethodManager,
): Boolean {
    val localImeService = ComponentName(context, IMEService::class.java)
    val enabledInputMethodList = inputMethodManager.enabledInputMethodList
    val isInputMethodSettingsActive = enabledInputMethodList.any { it.component == localImeService }
    return isInputMethodSettingsActive
}

val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("NavController not provided")
}


@Composable
private fun App() {
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
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun HomeScreen() {
    val context: Context = koinInject()
    val inputMethodManager: InputMethodManager = koinInject()
    Scaffold {
    }
}

@Composable
private fun ImeRequestPermissionScreen() {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val inputMethodManager: InputMethodManager = koinInject()
    Scaffold { paddingValues ->
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> {
                        if (checkInputMethodSettingsActive(context, inputMethodManager)) {
                            navController.navigate("/home") {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }

                    else -> {}
                }
            }

            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
        Button(
            {
                context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            },
            modifier = Modifier.padding(paddingValues),
        ) {
            Text("打开输入法设置")
        }
    }
}
