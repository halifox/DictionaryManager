package com.github.dictionary.importer

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.github.dictionary.UserDictionaryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.FileNotFoundException
import java.net.URLDecoder
import java.util.Locale

class DictionaryImporter(
    private val context: Context,
    private val parser: List<Parser> = listOf(Sogou())
) : KoinComponent {

    @Volatile
    private var isProcessing = false
    private val scope = MainScope()
    private val userDictionaryManager by inject<UserDictionaryManager>()
    private val taskQueue = mutableListOf<Task>()

    @Synchronized
    fun addImportTask(task: Task) {
        taskQueue.add(task)
        if (!isProcessing) {
            processNextTask()
        }
    }

    private fun processNextTask() {
        if (taskQueue.isNotEmpty()) {
            isProcessing = true
            scope.launch(Dispatchers.IO) {
                val task = taskQueue.removeAt(0)
                runCatching {
                    processTask(task)
                }
                processNextTask()
            }
        } else {
            isProcessing = false
        }
    }

    private fun queryUriFileName(uri: Uri): String {
        return when (uri.scheme) {
            "content" -> queryUriFileNameByContent(uri)
            "file" -> queryUriFileNameByFile(uri)
            else -> throw IllegalArgumentException("Unsupported URI scheme: ${uri.scheme}")
        }
    }

    private fun queryUriFileNameByFile(uri: Uri): String {
        val path = uri.path ?: throw IllegalArgumentException("URI path is null or empty")
        val file = File(path)
        return URLDecoder.decode(file.name, Charsets.UTF_8.name())
    }

    private fun queryUriFileNameByContent(uri: Uri): String {
        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
        )?.use {
            if (it.moveToFirst()) {
                return it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            } else {
                throw FileNotFoundException("File name not found for URI: $uri")
            }
        }
        throw IllegalArgumentException("Failed to query URI: $uri")
    }

    private fun processTask(task: Task) {
        val callback = NotificationTaskCallback(context, task.uri.hashCode())
        processTaskCore(task, callback)
    }

    private fun processTaskCore(
        task: Task,
        callback: TaskCallback,
    ) {
        var cacheFile: File? = null
        try {
            val fileName = queryUriFileName(task.uri)
            callback.onParser(fileName.substringBeforeLast("."))
            cacheFile = copyUriToCache(task.uri, fileName)
            for (parser in parser) {
                if (parser.verify(cacheFile)) {
                    val items = parser.parse(cacheFile)
                    callback.onStart()
                    items.forEachIndexed { index, wordEntry ->
                        userDictionaryManager.insert(wordEntry.word, wordEntry.pinyin, 250, task.locale.toString(), 0)
                        callback.onProgress(index, items.size)
                    }
                    callback.onComplete()
                    task.onComplete.invoke()
                    return
                }
            }
            throw IllegalArgumentException("Didn't find a suitable thesaurus parser")
        } catch (e: Exception) {
            callback.onException(e)
        } finally {
            cacheFile?.delete()
        }
    }

    private fun copyUriToCache(uri: Uri, name: String): File {
        val cacheFile = File(context.cacheDir, name)
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            cacheFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return cacheFile
    }

    data class Entry(
        val word: String,
        val pinyinList: List<String>,
        val pinyin: String,
    )

    data class Task(
        val uri: Uri,
        val locale: Locale,
        val onComplete: () -> Unit = {},
    )

    interface Downloader {
        fun download(id: Int, name: String)
    }

    interface Parser {
        fun verify(file: File): Boolean
        fun parse(file: File): List<Entry>
    }

    interface TaskCallback {
        fun onParser(fileName: String)
        fun onStart()
        fun onProgress(index: Int, size: Int)
        fun onComplete()
        fun onException(e: Exception)
    }

}