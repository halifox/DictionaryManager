package com.github.dictionary.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.github.dictionary.IMEService

@Composable
fun PermissionCheckScreen(navController: NavHostController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    if (checkInputMethodSettingsActive(context)) {
                        navController.navigate("home") {
                            popUpTo(0) {
                                inclusive = true
                            }
                        }
                    }
                }
                else                     -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold {
        Column(
            Modifier
                .padding(it)
                .padding(16.dp)
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                ListItem(
                    { Text("请求输入法权限") },
                    Modifier,
                    null,
                    { Text("用于启用词库功能，提升输入体验。不会上传或收集任何输入内容。") },
                    { Icon(Icons.Default.Security, contentDescription = null) }
                )
            }
            Button({
                requestInputMethodSettingsActive(context)
            }, Modifier.fillMaxWidth()) {
                Text("申请权限")
            }
        }
    }
}


fun checkInputMethodSettingsActive(context: Context): Boolean {
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val localImeService = ComponentName(context, IMEService::class.java)
    val enabledInputMethodList = inputMethodManager.enabledInputMethodList
    val isInputMethodSettingsActive = enabledInputMethodList.any { it.component == localImeService }
    return isInputMethodSettingsActive
}

fun requestInputMethodSettingsActive(context: Context) {
    context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
}