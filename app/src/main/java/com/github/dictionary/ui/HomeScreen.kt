package com.github.dictionary.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.github.dictionary.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar({ Text("词库管理器") })
        }
    ) {
        Column(
            Modifier
                .padding(it)
                .padding(16.dp)
        ) {
            ListItem(
                { Text("搜狗词库") },
                Modifier.clickable { navController.navigate("dictionary") },
                supportingContent = { Text("声明:仅供学习交流") },
                leadingContent = { AsyncImage(R.raw.sogou, null, Modifier.size(28.dp)) },
            )
            ListItem(
                { Text("百度词库") },
                Modifier.clickable { navController.navigate("dictionary") },
                supportingContent = { Text("声明:仅供学习交流") },
                leadingContent = { AsyncImage(R.raw.baidu, null, Modifier.size(28.dp)) },
            )
            ListItem(
                { Text("QQ词库") },
                Modifier.clickable { navController.navigate("dictionary") },
                supportingContent = { Text("声明:仅供学习交流") },
                leadingContent = { AsyncImage(R.raw.qq, null, Modifier.size(28.dp)) },
            )
            ListItem(
                { Text("本地词库") },
                Modifier.clickable { navController.navigate("dictionary") },
                supportingContent = { Text("本地词库的管理") },
                leadingContent = { AsyncImage(R.raw.local, null, Modifier.size(28.dp)) },
            )
        }
    }
}