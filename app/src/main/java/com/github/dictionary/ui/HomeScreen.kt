package com.github.dictionary.ui

import android.content.Intent
import android.provider.Settings
import android.provider.UserDictionary
import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.github.dictionary.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar({ Text("词库管理器") })
        }
    ) {
        Column(Modifier.padding(it)) {
            ListItem(
                { Text("搜狗词库") },
                Modifier.clickable { navController.navigate(Dictionary("sougo")) },
                supportingContent = { Text("声明:仅供学习交流") },
                leadingContent = { AsyncImage(R.raw.sogou, null, Modifier.size(28.dp)) },
            )
            ListItem(
                { Text("百度词库") },
                Modifier.clickable { navController.navigate(Dictionary("baidu")) },
                supportingContent = { Text("声明:仅供学习交流") },
                leadingContent = { AsyncImage(R.raw.baidu, null, Modifier.size(28.dp)) },
            )
            ListItem(
                { Text("QQ词库") },
                Modifier.clickable { navController.navigate(Dictionary("qq")) },
                supportingContent = { Text("声明:仅供学习交流") },
                leadingContent = { AsyncImage(R.raw.qq, null, Modifier.size(28.dp)) },
            )
            ListItem(
                { Text("已安装的词库") },
                Modifier.clickable { navController.navigate(LocalDictionary) },
                supportingContent = { Text("已安装的词库的管理") },
                leadingContent = { AsyncImage(R.raw.local, null, Modifier.size(28.dp)) },
            )
            ListItem(
                { Text("本地词库(系统)") },
                Modifier.clickable {
                    val intent = Intent(Settings.ACTION_USER_DICTIONARY_SETTINGS)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                },
                supportingContent = { Text("本地词库的管理") },
                leadingContent = { AsyncImage(R.raw.local, null, Modifier.size(28.dp)) },
            )
            if (com.github.dictionary.BuildConfig.DEBUG) {
                ListItem(
                    { Text("清空本地词库(DEBUG)") },
                    Modifier.clickable {
                        context.contentResolver.delete(UserDictionary.Words.CONTENT_URI, null, null)
                    },
                )
                ListItem(
                    { Text("打印本地词库信息(DEBUG)") },
                    Modifier.clickable {
                        context.contentResolver.query(UserDictionary.Words.CONTENT_URI, null, null, null, null)?.use { cursor ->
                            val wordIndex = cursor.getColumnIndex(UserDictionary.Words.WORD)
                            val shortcutIndex = cursor.getColumnIndex(UserDictionary.Words.SHORTCUT)
                            val freqIndex = cursor.getColumnIndex(UserDictionary.Words.FREQUENCY)
                            val appIdIndex = cursor.getColumnIndex(UserDictionary.Words.APP_ID)
                            val idIndex = cursor.getColumnIndex(UserDictionary.Words._ID)
                            while (cursor.moveToNext()) {
                                val word = cursor.getString(wordIndex)
                                val shortcut = cursor.getString(shortcutIndex)
                                val frequency = cursor.getInt(freqIndex)
                                val appid = cursor.getInt(appIdIndex)
                                val id = cursor.getInt(idIndex)
                                Log.d("TAG", "z:${id} ${appid} ${word} ${shortcut} ${frequency} ")
                            }
                        }
                    },
                )
            }
        }
    }
}