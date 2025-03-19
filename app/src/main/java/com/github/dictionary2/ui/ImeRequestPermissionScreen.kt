package com.github.dictionary2.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.github.dictionary.IMEService
import org.koin.compose.koinInject

@Composable
fun ImeRequestPermissionScreen() {
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

fun checkInputMethodSettingsActive(
    context: Context,
    inputMethodManager: InputMethodManager,
): Boolean {
    val localImeService = ComponentName(context, IMEService::class.java)
    val enabledInputMethodList = inputMethodManager.enabledInputMethodList
    val isInputMethodSettingsActive = enabledInputMethodList.any { it.component == localImeService }
    return isInputMethodSettingsActive
}
