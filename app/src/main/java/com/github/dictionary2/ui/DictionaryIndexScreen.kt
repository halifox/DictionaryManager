package com.github.dictionary2.ui

import androidx.compose.foundation.layout.Arrangement
import com.github.dictionary.R
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import com.github.dictionary.db.SogouDao
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun DictionaryIndexScreen() {
    val navHostController = LocalNavController.current
    val dao: SogouDao = koinInject()
    var key by remember { mutableStateOf("") }
    val pager = remember(key) {
        Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { dao.pagingSource("%${key}%") }
        )
    }
    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("词典")
                },
                navigationIcon = {
                    IconButton({
                        navHostController.navigateUp()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton({

                    }) {
                        Icon(Icons.Default.Search, null)
                    }
                },
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier.padding(it)
        ) {
            items(count = lazyPagingItems.itemCount) { index ->
                val item = lazyPagingItems[index]!!
                ItemView("${index + 1} ${item.name}", onClick = {})
            }
        }
    }
}


@Preview(device = "spec:parent=pixel_5")
@Composable
private fun ItemView(
    title: String = "title",
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 0.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        OutlinedButton(onClick = onClick) {
            Text(text = stringResource(R.string.btn_item_dictionary_index_install))
        }
    }
}
