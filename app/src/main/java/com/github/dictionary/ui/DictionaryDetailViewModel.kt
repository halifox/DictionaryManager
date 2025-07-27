package com.github.dictionary.ui

import android.app.Application
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

@HiltViewModel
class DictionaryDetailViewModel @Inject constructor(val repo: DictRepository, application: Application) : AndroidViewModel(application) {
    val context = application
    private val _uiState = MutableStateFlow<DictionaryDetailState>(DictionaryDetailState.Loading)
    val uiState = _uiState.asStateFlow()

    val client = OkHttpClient.Builder().build()
    val progress = MutableStateFlow(0f)
    val results = MutableStateFlow(emptyList<ParsedResult>())
    val isBusy = MutableStateFlow(false)

    fun init(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                isBusy.value = true
                val dict = repo.getDictionaryById(id)
                val localRecord = repo.getRecordById(dict._id)
                _uiState.value = DictionaryDetailState.Installed(dict, localRecord)
                if (localRecord == null) {
                    downloadUserDictionary(dict)
                } else {
                    results.value = repo.queryUserDictionaryByIds(localRecord)
                }
            } catch (e: Exception) {
                _uiState.value = DictionaryDetailState.Error(e)
                e.printStackTrace()
            } finally {
                isBusy.value = false
            }
        }
    }


    suspend fun downloadUserDictionary(dict: Dict) {
        val url = repo.getUserDictionaryDownloadUrl(dict)
        val fn = repo.getUserDictionaryFileName(dict)
        val file = File(context.cacheDir, fn)
        if (file.exists()) {
            results.value = repo.parseUserDictionaryFile(file)
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
        results.value = repo.parseUserDictionaryFile(file)
    }


    fun installUserDictionary(dict: Dict, data: List<ParsedResult>) {
        viewModelScope.launch(Dispatchers.IO) {
            isBusy.value = true
            repo.installUserDictionary(dict, data)
            val localRecord = repo.getRecordById(dict._id)
            _uiState.value = DictionaryDetailState.Installed(dict, localRecord)
            isBusy.value = false
        }
    }

    fun uninstallUserDictionary(dict: Dict, record: LocalRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            isBusy.value = true
            repo.uninstallUserDictionary(record)
            val localRecord = repo.getRecordById(dict._id)
            _uiState.value = DictionaryDetailState.Installed(dict, localRecord)
            isBusy.value = false
        }
    }


    companion object {
        private const val TAG = "InstallScreen"
    }
}
