package com.github.dictionary.ui

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.dictionary.model.Dict
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

    if (uiState is UiState.Error) {
        return ErrorScreen((uiState as UiState.Error).e)
    }
    if (uiState == UiState.Loading) {
        return LoadingIndicatorScreen()
    }



    val dict = (uiState as UiState.Success).data
    val progress by viewModel.progress.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                {
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
    data class Error(val e: Exception) : UiState<Nothing>()
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
                _uiState.value = UiState.Error(e)
                e.printStackTrace()
            }
        }
    }


    suspend fun download(dict: Dict) {
        val url = repo.getDownloadUrl(dict)
        val fn = repo.getFileName(dict)
        val file = File(context.cacheDir, fn)
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
        repo.install(file, dict)
    }


    companion object {
        private const val TAG = "InstallScreen"
    }
}
