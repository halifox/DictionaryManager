package com.github.dictionary.ui

import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.github.dictionary.parser.ParsedResult

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DictionaryDetailScreen(navController: NavHostController, data: DictionaryDetail) {
    val (id) = data
    val viewModel = hiltViewModel<DictionaryDetailViewModel>()
    LaunchedEffect(Unit) {
        viewModel.loadDictionary(id)
    }
    val dictState by viewModel.dictState.collectAsState()

    UiStateScaffold(dictState) {
        val (dict) = dictState as UiState.Success
        LaunchedEffect(dict) {
            viewModel.loadDictionaryDetail(dict)
            viewModel.loadDictionaryIsInstalled(dict)
        }
        val parsedResultsState by viewModel.parsedResultsState.collectAsState()
        UiStateScaffold(parsedResultsState) {
            val installState by viewModel.installState.collectAsState()
            val listState = rememberLazyListState()
            Scaffold(topBar = { TopAppBar({ Text(dict.name.orEmpty()) }) }) {
                val (parsedResults) = parsedResultsState as UiState.Success<List<ParsedResult>>
                if (parsedResults.isEmpty()) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(it),
                    ) {
                        Text("无法解析出结果,请提供解析规则.", Modifier.align(Alignment.Center))
                    }
                    return@Scaffold
                }
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(it)
                ) {
                    LazyColumn(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        listState
                    ) {
                        items(parsedResults) {
                            ListItem(
                                { Text(it.word) },
                                supportingContent = { Text(it.pinyin) }
                            )
                            HorizontalDivider()
                        }
                    }
                    when (installState) {
                        InstallState.Uninstalling -> DictionaryDetailButton("卸载中")
                        InstallState.Installing -> DictionaryDetailButton("安装中")
                        InstallState.Uninstalled -> DictionaryDetailButton("安装", { viewModel.installUserDictionary(dict, parsedResults) })
                        InstallState.Installed -> DictionaryDetailButton("卸载", { viewModel.uninstallUserDictionary(dict) })
                        else -> null
                    }
                }
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