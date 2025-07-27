package com.github.dictionary.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> UiStateScaffold(
    state: UiState<T>,
    content: @Composable () -> Unit,
) {
    when (state) {
        is UiState.Error -> {
            val (exception) = state
            Scaffold(topBar = { TopAppBar({ Text("异常") }) }) {
                Text(
                    exception.stackTraceToString(),
                    Modifier
                        .fillMaxSize()
                        .padding(it)
                        .verticalScroll(
                            rememberScrollState()
                        )
                )
            }
        }

        UiState.Idle -> {
            Scaffold(topBar = { TopAppBar({ Text("加载中") }) }) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(it),
                ) {
                    LoadingIndicator(Modifier.align(Alignment.Center))
                }
            }
        }

        is UiState.Loading -> {
            val (progress) = state
            Scaffold(topBar = { TopAppBar({ Text("加载中") }) }) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(it),
                ) {
                    if (progress != 0f) {
                        LinearWavyProgressIndicator({ progress }, Modifier.fillMaxWidth())
                    }
                    LoadingIndicator(Modifier.align(Alignment.Center))
                }
            }
        }

        is UiState.Success<*> -> content()
    }
}