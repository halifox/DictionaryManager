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
    if (uiState is DictionaryDetailState.Error) {
        val uiState = uiState as DictionaryDetailState.Error
        return ErrorScreen(uiState.exception)
    }
    if (uiState == DictionaryDetailState.Loading) {
        return LoadingIndicatorScreen()
    }

    val (dict, localRecord) = uiState as DictionaryDetailState.Installed
    val isBusy by viewModel.isBusy.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val results by viewModel.results.collectAsState()
    Scaffold(
        topBar = { TopAppBar({ Text(dict.name.orEmpty()) }) }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(it)

        ) {
            if (results.isEmpty()) {
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
                        { Text(it.pinyin) },
                        supportingContent = { Text(it.word) }
                    )
                    HorizontalDivider()
                }
            }
            if (isBusy) {
                Button(
                    { },
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp, 0.dp),
                    enabled = false
                ) {
                    Text("处理中")
                }
            } else if (localRecord == null) {
                Button(
                    { viewModel.installUserDictionary(dict, results) },
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp, 0.dp),
                ) {
                    Text("安装")
                }
            } else {
                Button(
                    { viewModel.uninstallUserDictionary(dict, localRecord) },
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp, 0.dp),
                ) {
                    Text("删除")
                }
            }

        }
    }
}