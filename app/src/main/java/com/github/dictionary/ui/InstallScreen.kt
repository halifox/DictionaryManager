package com.github.dictionary.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstallScreen(_id: String) {
    Scaffold(
        topBar = {
            TopAppBar({

            })
        }
    ) {
        Column(Modifier.padding(it)) {
            Text("${_id}")
        }
    }
}