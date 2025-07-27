package com.github.dictionary.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DictionaryDetailScreen(data: DictionaryDetail) {
    val (id) = data
    val viewModel = hiltViewModel<DictionaryDetailViewModel>()
    LaunchedEffect(Unit) {
        viewModel.init(id)
    }
    val listState = rememberLazyListState()

    val uiState by viewModel.uiState.collectAsState()
    if (uiState == DictionaryDetailState.Idle) {
        return LoadingIndicatorScreen()
    }
    if (uiState is DictionaryDetailState.Error) {
        val uiState = uiState as DictionaryDetailState.Error
        return ErrorScreen(uiState.exception)
    }


    val results by viewModel.results.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar({
                Text(viewModel.dict.name.orEmpty())
            })
        }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(it)

        ) {
            if (uiState is DictionaryDetailState.Downloading) {
                val progress by viewModel.progress.collectAsState()
                LinearWavyProgressIndicator({ progress }, Modifier.fillMaxWidth())
            }
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
                listState
            ) {
                items(results) {
                    ListItem(
                        { Text(it.word) },
                        supportingContent = { Text(it.pinyin) }
                    )
                    HorizontalDivider()
                }
            }
            when (uiState) {
                DictionaryDetailState.UnInstalling -> DictionaryDetailButton("卸载中")
                DictionaryDetailState.Installing -> DictionaryDetailButton("安装中")
                DictionaryDetailState.Uninstalled -> DictionaryDetailButton("安装", viewModel::installUserDictionary)
                DictionaryDetailState.Installed -> DictionaryDetailButton("安装", viewModel::uninstallUserDictionary)
                DictionaryDetailState.Downloading -> DictionaryDetailButton("下载中")
                else -> null
            }
        }
    }
}

@Composable
fun DictionaryDetailButton(
    text: String,
    onClick: (() -> Unit)? = null,
) {
    Button(
        { onClick?.invoke() },
        Modifier
            .fillMaxWidth()
            .padding(16.dp, 0.dp),
        enabled = onClick != null
    ) {
        Text(text)
    }

}