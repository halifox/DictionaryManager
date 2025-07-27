package com.github.dictionary.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LocalDictionaryScreen(navController: NavHostController) {
    val viewModel = hiltViewModel<DictionaryViewModel>()
    Scaffold(
        topBar = { TopAppBar({ Text("已安装的词库") }) },
    ) {
        val items = viewModel.getInstalledDictionaries().collectAsLazyPagingItems()
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            val state = items.loadState.refresh
            when (state) {
                is LoadState.Error -> item {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text("${state.error}")
                    }
                }

                LoadState.Loading -> item {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        LoadingIndicator()
                    }
                }

                is LoadState.NotLoading -> items(items.itemCount) { index ->
                    DictionaryItem(navController, items[index])
                }
            }
        }
    }
}

