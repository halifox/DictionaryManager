package com.github.dictionary.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.dictionary.model.Dict
import com.github.dictionary.parser.ParsedResult
import com.github.dictionary.repository.DictRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.http.promisesBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    data class Loading(val progress: Float) : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val exception: Throwable) : UiState<Nothing>()
}

sealed class InstallState {
    object Idle : InstallState()
    object Installing : InstallState()
    object Installed : InstallState()
    object Uninstalling : InstallState()
    object Uninstalled : InstallState()
}

@HiltViewModel
class DictionaryDetailViewModel @Inject constructor(val repo: DictRepository, application: Application) : AndroidViewModel(application) {
    val context = application

    val client = OkHttpClient.Builder().build()

    val installState = MutableStateFlow<InstallState>(InstallState.Idle)
    val dictState = MutableStateFlow<UiState<Dict>>(UiState.Idle)
    val parsedResultsState = MutableStateFlow<UiState<List<ParsedResult>>>(UiState.Idle)

    fun loadDictionary(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dictState.value = UiState.Loading(0.5f)
                val dict = repo.getDictionaryById(id)
                dictState.value = UiState.Success(dict)
            } catch (e: Exception) {
                dictState.value = UiState.Error(e)
            }
        }
    }

    fun loadDictionaryDetail(dict: Dict) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val localRecord = repo.getRecordById(dict._id)
                if (localRecord == null) {
                    val file = downloadUserDictionary(dict)
                    val results = repo.parseUserDictionaryFile(file)
                    parsedResultsState.value = UiState.Success(results)
                } else {
                    val results = repo.queryUserDictionaryByIds(localRecord)
                    parsedResultsState.value = UiState.Success(results)
                }
            } catch (e: Exception) {
                parsedResultsState.value = UiState.Error(e)
            }
        }
    }

    fun loadDictionaryIsInstalled(dict: Dict) {
        viewModelScope.launch(Dispatchers.IO) {
            val localRecord = repo.getRecordById(dict._id)
            if (localRecord != null) {
                installState.value = InstallState.Installed
            } else {
                installState.value = InstallState.Uninstalled
            }
        }
    }


    suspend fun downloadUserDictionary(dict: Dict): File {
        val url = repo.getUserDictionaryDownloadUrl(dict)
        val fn = repo.getUserDictionaryFileName(dict)
        val file = File(context.cacheDir, fn)
        if (file.exists()) {
            return file
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
        val body = response.body!!
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
                val progress = bytesCopied * 1f / contentLength
                parsedResultsState.value = UiState.Loading(progress)
                delay(1000)
            }
        } catch (e: Exception) {
            throw e
        } finally {
            inputStream.close()
            outputStream.close()
        }
        tmpFile.renameTo(file)
        return file
    }


    fun installUserDictionary(dict: Dict, parsedResults: List<ParsedResult>) {
        viewModelScope.launch(Dispatchers.IO) {
            installState.value = InstallState.Installing
            repo.installUserDictionary(dict, parsedResults)
            installState.value = InstallState.Installed
        }
    }

    fun uninstallUserDictionary(dict: Dict) {
        viewModelScope.launch(Dispatchers.IO) {
            installState.value = InstallState.Uninstalling
            val localRecord = repo.getRecordById(dict._id)
            if (localRecord != null) {
                repo.uninstallUserDictionary(localRecord)
            }
            installState.value = InstallState.Uninstalled
        }
    }
}
