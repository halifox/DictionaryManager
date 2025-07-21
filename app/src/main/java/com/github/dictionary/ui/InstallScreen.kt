package com.github.dictionary.ui

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.dictionary.model.Dict
import com.github.dictionary.model.LocalRecord
import com.github.dictionary.parser.ParsedResult
import com.github.dictionary.repository.DictRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.http.promisesBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun InstallScreen(data: Install) {
    val viewModel = hiltViewModel<InstallViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.init(data)
    }

    when (uiState) {
        is InstallScreenState.Error -> {
            val uiState = uiState as InstallScreenState.Error
            return ErrorScreen(uiState.e)
        }

        InstallScreenState.Loading -> {
            return LoadingIndicatorScreen()
        }

        is InstallScreenState.Installed -> {
            val (dict, localRecord) = uiState as InstallScreenState.Installed
            val results = viewModel.repo.getLocalWorlds(localRecord)
            return Scaffold(
                topBar = {
                    TopAppBar(
                        {
                            Text(dict.name.orEmpty())
                        })
                }
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(it)
                ) {
                    LazyColumn(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(results) {
                            ListItem(
                                { Text("${it.word}") },
                                supportingContent = { Text("${it.pinyin}") }
                            )
                            HorizontalDivider()
                        }
                    }
                    Button(
                        { viewModel.uninstall(localRecord) },
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp, 0.dp)
                    ) {
                        Text("删除")
                    }
                }
            }

        }

        is InstallScreenState.UnInstalled -> {
            val (dict) = uiState as InstallScreenState.UnInstalled
            val progress by viewModel.progress.collectAsState()
            val results by viewModel.results.collectAsState()
            LaunchedEffect(dict) {
                viewModel.download(dict)
            }

            return Scaffold(
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
                            .weight(1f)
                    ) {
                        items(results) {
                            ListItem(
                                { Text("${it.word}") },
                                supportingContent = { Text("${it.pinyin}") }
                            )
                            HorizontalDivider()
                        }
                    }
                    Button(
                        { viewModel.install(dict, results) },
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp, 0.dp),
                        enabled = results.isNotEmpty()
                    ) {
                        Text("安装")
                    }
                }
            }
        }
    }
}

sealed class InstallScreenState {
    object Loading : InstallScreenState()
    data class Error(val e: Exception) : InstallScreenState()
    data class Installed(val dict: Dict, val localRecord: LocalRecord) : InstallScreenState()
    data class UnInstalled(val dict: Dict) : InstallScreenState()
}


@HiltViewModel
class InstallViewModel @Inject constructor(val repo: DictRepository, application: Application) : AndroidViewModel(application) {
    val context = application
    private val _uiState = MutableStateFlow<InstallScreenState>(InstallScreenState.Loading)
    val uiState = _uiState.asStateFlow()

    val client = OkHttpClient.Builder().build()
    val progress = MutableStateFlow(0f)
    val results = MutableStateFlow(emptyList<ParsedResult>())

    fun init(data: Install) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dict = repo.getDict(data.id)
                val localRecord = repo.getById(dict._id)
                if (localRecord != null) {
                    _uiState.value = InstallScreenState.Installed(dict, localRecord)
                } else {
                    _uiState.value = InstallScreenState.UnInstalled(dict)
                }
            } catch (e: Exception) {
                _uiState.value = InstallScreenState.Error(e)
                e.printStackTrace()
            }
        }
    }


    fun download(dict: Dict) {
        viewModelScope.launch(Dispatchers.IO) {
            val url = repo.getDownloadUrl(dict)
            val fn = repo.getFileName(dict)
            val file = File(context.cacheDir, fn)
            if (file.exists()) {
                results.value = repo.parse(file)
                return@launch
            }
            val request = Request.Builder()
                .get()
                .url(url)
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IllegalArgumentException("服务器响应异常")
            }
            if (!response.promisesBody()) {
                throw IllegalArgumentException("服务器响应异常")
            }
            val body = response.body ?: return@launch
            val contentLength = body.contentLength()

            val inputStream = body.byteStream()
            val tmpFile = File(context.cacheDir, "${fn}.tmp")
            val outputStream = FileOutputStream(tmpFile)

            try {
                var bytesCopied: Long = 0
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                while (true) {
                    val bytes = inputStream.read(buffer)
                    if (bytes == -1) break
                    outputStream.write(buffer, 0, bytes)
                    bytesCopied += bytes
                    progress.value = bytesCopied * 1f / contentLength
                }
            } catch (e: Exception) {
                throw e
            } finally {
                inputStream.close()
                outputStream.close()
            }
            tmpFile.renameTo(file)
            results.value = repo.parse(file)
        }
    }


    fun install(dict: Dict, data: List<ParsedResult>) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.install(dict, data)
        }
    }

    fun uninstall(record: LocalRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.uninstall(record)
        }
    }


    companion object {
        private const val TAG = "InstallScreen"
    }
}
