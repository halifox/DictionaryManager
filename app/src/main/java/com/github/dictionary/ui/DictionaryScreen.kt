package com.github.dictionary.ui

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopSearchBar
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.github.dictionary.model.Dict
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DictionaryScreen(navController: NavHostController, data: Dictionary) {
    val (source) = data
    val viewModel = hiltViewModel<DictionaryViewModel>()
    val textFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarState()
    val scope = rememberCoroutineScope()
    val scrollBehavior = SearchBarDefaults.enterAlwaysSearchBarScrollBehavior()


    val inputField =
        @Composable {
            SearchBarDefaults.InputField(
                modifier = Modifier,
                searchBarState = searchBarState,
                textFieldState = textFieldState,
                onSearch = { scope.launch { searchBarState.animateToCollapsed() } },
                placeholder = { Text("Search...") },
                leadingIcon = {
                    if (searchBarState.currentValue == SearchBarValue.Expanded) {
                        IconButton(
                            onClick = { scope.launch { searchBarState.animateToCollapsed() } }
                        ) {
                            Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                        }
                    } else {
                        Icon(Icons.Default.Search, contentDescription = null)
                    }
                },
            )
        }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopSearchBar(
                scrollBehavior = scrollBehavior,
                state = searchBarState,
                inputField = inputField,
            )
            ExpandedFullScreenSearchBar(state = searchBarState, inputField = inputField) {
                val key = textFieldState.text.toString()
                val items = viewModel.getSearchPager(source, "%${key}%").collectAsLazyPagingItems()
                LazyColumn(
                    Modifier
                        .fillMaxSize()
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
                            DictItem(navController, items[index])
                        }
                    }
                }
            }
        },
    ) {
        var currentDict by rememberSaveable(stateSaver = Dict.Saver) { mutableStateOf<Dict?>(null) }
        val items = viewModel.getSubTreeQuery(currentDict?.id, source).collectAsLazyPagingItems()
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            item {
                Column {
                    DictFilterChipGroup(viewModel, source, 1, null) { currentDict = it }
                }
            }
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
                    DictItem(navController, items[index])
                }
            }
        }
    }
}

@Composable
fun DictItem(navController: NavHostController, dict: Dict?) {
    dict ?: return
    var isExpand by remember(dict) { mutableStateOf(false) }
    ListItem(
        {
            Text(dict.name.orEmpty())
        },
        Modifier.clickable {
            navController.navigate(Install(dict._id))
        },
        supportingContent = {
            Text(
                """
                        词库示例: ${dict.exps.orEmpty()}
                        更新时间: ${dict.time.orEmpty()}
                        下载次数: ${dict.downCount.orEmpty()}
                    """.trimIndent(),
                maxLines = if (isExpand) Int.MAX_VALUE else 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = {
            IconButton({
                isExpand = !isExpand
            }) {
                Icon(
                    if (isExpand) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    null
                )
            }
        }
    )
    HorizontalDivider()
}

@Composable
fun DictFilterChipGroup(
    viewModel: DictionaryViewModel,
    source: String,
    tiers: Int,
    parentDict: Dict?,
    onSelected: (Dict?) -> Unit,
) {
    var currentDict by rememberSaveable(parentDict, stateSaver = Dict.Saver) { mutableStateOf<Dict?>(null) }
    val dictList by viewModel.getCategories(source, parentDict?.id, tiers).collectAsState(emptyList())
    if (dictList.isEmpty()) return
    Row(
        Modifier
            .horizontalScroll(rememberScrollState())
            .padding(start = 16.dp),
        Arrangement.spacedBy(8.dp)
    ) {
        dictList.forEach { dict ->
            FilterChip(
                currentDict == dict,
                {
                    if (currentDict == dict) {
                        currentDict = null
                        onSelected(parentDict)
                    } else {
                        currentDict = dict
                        onSelected(dict)
                    }
                },
                {
                    Text(dict.name.orEmpty())
                }
            )
        }
    }
    if (currentDict != null) {
        DictFilterChipGroup(viewModel, source, tiers + 1, currentDict, onSelected)
    }
}