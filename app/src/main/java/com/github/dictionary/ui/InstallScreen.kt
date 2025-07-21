package com.github.dictionary.ui

import android.app.Application
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.dictionary.model.Dict
import com.github.dictionary.parser.BaiduParser
import com.github.dictionary.parser.QQParser
import com.github.dictionary.parser.SougoParser
import com.github.dictionary.repository.DictRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

    if (uiState is UiState.Error) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text("${(uiState as UiState.Error).message}")
        }
        return
    } else if (uiState == UiState.Loading) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            LoadingIndicator()
        }
        return
    }
    val dict = (uiState as UiState.Success).data
    val progress by viewModel.progress.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar({
                Text(dict.name.orEmpty())
            })
        }
    ) {
        Column(Modifier.padding(it)) {
            LinearWavyProgressIndicator({ progress }, Modifier.fillMaxWidth())
            Text("${progress}")
        }
    }
}

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}


@HiltViewModel
class InstallViewModel @Inject constructor(private val repo: DictRepository, application: Application) : AndroidViewModel(application) {
    val context = application
    private val _uiState = MutableStateFlow<UiState<Dict>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    val client = OkHttpClient.Builder().build()
    val progress = MutableStateFlow(0f)

    fun init(data: Install) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dict = repo.getDict(data.id)
                _uiState.value = UiState.Success(dict)
                download(dict)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }


    fun getDownloadUrl(dict: Dict): String {
        return when (dict.source) {
            "sougo" -> "https://pinyin.sogou.com/d/dict/download_cell.php?id=${dict.id}&name=${dict.name}"
            "baidu" -> "https://shurufa.baidu.com/dict_innerid_download?innerid=${dict.innerId}"
            "qq" -> "https://cdict.qq.pinyin.cn/v1/download?dict_id=${dict.id}"
            else -> throw IllegalArgumentException()
        }
    }

    fun getFileName(dict: Dict): String {
        return when (dict.source) {
            "sougo" -> "${dict.id}.scel"
            "baidu" -> "${dict.id}.bdict"
            "qq" -> "${dict.id}.qpyd"
            else -> throw IllegalArgumentException()
        }
    }


    suspend fun download(dict: Dict) {
        val url = getDownloadUrl(dict)
        Log.d(TAG, "url:${url} ")
        val fn = getFileName(dict)
        val file = File(context.cacheDir, fn)
        Log.d(TAG, "file:${file} ")
        if (file.exists()) {
            return
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
        val body = response.body ?: return
        val contentLength = body.contentLength()

        val inputStream = body.byteStream()
        val tmpFile = File(context.cacheDir, "${fn}.tmp")
        val outputStream = FileOutputStream(tmpFile)

        try {
            var bytesCopied: Long = 0
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                delay(1000)
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
        install(file, dict)
    }


    fun install(file: File, dict: Dict) {
        val parser = when (file.extension) {
            "scel"/*sougo*/ -> SougoParser()
            "bdict"/*baidu*/ -> BaiduParser()
            "qpyd"/*qq*/ -> QQParser()
            else -> throw IllegalArgumentException()
        }
        val results = parser.parse(file.path)


    }

    companion object {
        private const val TAG = "InstallScreen"
    }
}
