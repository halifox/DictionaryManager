package com.github.dictionary.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.compose.collectAsLazyPagingItems
import com.github.dictionary.repository.DictRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryScreen(navController: NavHostController) {
    val viewModel = hiltViewModel<DictionaryViewModel>()
    val items = viewModel.pager.collectAsLazyPagingItems()
    Scaffold(topBar = {
        TopAppBar({ Text("搜狗词库") })
    }) {
        LazyColumn(
            Modifier
                .padding(it)
                .padding(16.dp, 0.dp)
        ) {
            items(items.itemCount) { index ->
                val dict = items[index]
                if (dict != null) {
                    ListItem(
                        { Text(dict.name.orEmpty()) },
                        supportingContent = { Text("""
                            time: ${dict.time}
                            downCount: ${dict.downCount}
                            exps: ${dict.exps}
                        """.trimIndent()) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@HiltViewModel
class DictionaryViewModel @Inject constructor(private val repo: DictRepository) : ViewModel() {
    val pager = Pager(
        PagingConfig(pageSize = 20)
    ) {
        repo.pagingSource()
    }.flow.cachedIn(viewModelScope)
}