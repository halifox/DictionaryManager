package com.github.dictionary.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorScreen(e: Throwable) {
    Scaffold(topBar = {
        TopAppBar({
            Text("Exception")
        })
    }) {
        Text(
            e.stackTraceToString(),
            Modifier
                .padding(it)
                .verticalScroll(
                    rememberScrollState()
                )
        )
    }
}