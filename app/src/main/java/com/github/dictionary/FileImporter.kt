package com.github.dictionary

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.net.URLDecoder
import java.util.Locale


class FileImporter(
    private val context: Context,
) : KoinComponent {


    private val taskQueue = mutableListOf<Task>()
    private var isProcessing = false
    private val scope = MainScope()
    private val notificationManager by inject<NotificationManagerCompat>()
    private val userDictionaryManager by inject<UserDictionaryManager>()

    @Synchronized
    fun addImportTask(uri: Task) {
        taskQueue.add(uri)
        if (!isProcessing) {
            processNextTask()
        }
    }

    private fun processNextTask() {
        if (taskQueue.isNotEmpty()) {
            isProcessing = true
            scope.launch(Dispatchers.IO) {
                val uri = taskQueue.removeAt(0)
                processDictionaryUri(uri)
                processNextTask()
            }
        } else {
            isProcessing = false
        }
    }


    private suspend fun processDictionaryUri(task: Task) {
        val fileName = queryUriFileName(task.uri)
        when {
            fileName.endsWith(Sogou.suffix) -> handleFile(task, Sogou(), {
//                adapter.refresh()
            })

            else -> showInvalidFileToast()
        }
    }

    private fun queryUriFileName(uri: Uri): String {
        when (uri.scheme) {
            "content" -> {
                context.contentResolver.query(
                    uri,
                    arrayOf(OpenableColumns.DISPLAY_NAME),
                    null,
                    null,
                    null,
                )?.use {
                    if (it.moveToFirst()) {
                        return it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                    }
                }
            }

            "file" -> {
                val file = File(uri.path)
                return URLDecoder.decode(file.name, "UTF-8")
            }
        }
        return ""
    }

    private suspend fun showInvalidFileToast() {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "请选择 .scel 文件", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun handleFile(
        task: Task,
        parser: Parser,
        onComplete: suspend () -> Unit
    ) {
        val uri = task.uri
        val locale = task.locale
        val id = uri.hashCode()
        val fileName = queryUriFileName(uri)
        //a
        val notification = NotificationCompat.Builder(context, "default_channel")
            .setSmallIcon(R.drawable.ic_search)
            .setContentTitle(fileName)
            .setContentText("正在解析文件...")
            .setProgress(0, 0, true)
            .setOngoing(true)
        notificationManager.notify(id, notification.build())

        val cacheFile = copyUri2Cache(uri, fileName)
        val entryList = parser.parse(cacheFile)

        //b
        notification
            .setContentTitle(fileName)
            .setContentText("正在添加到词库...")
            .setProgress(entryList.size, 0, false)
        notificationManager.notify(id, notification.build())

        var t = System.currentTimeMillis()
        entryList.forEachIndexed { index, wordEntry ->
            userDictionaryManager.insert(wordEntry.word, wordEntry.pinyin, locale = locale.toString())
            if (System.currentTimeMillis() - t > 1000) {
                notification.setProgress(entryList.size, index, false)
                notificationManager.notify(id, notification.build())
                t = System.currentTimeMillis()
            }
        }

        //c
        notification
            .setContentTitle(fileName)
            .setContentText("添加完成")
            .setProgress(0, 0, false)
            .setOngoing(false)
        notificationManager.notify(id, notification.build())
        onComplete.invoke()
        cacheFile.delete()
    }

    private fun copyUri2Cache(uri: Uri, name: String): File {
        val cache = File(context.cacheDir, name)
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            cache.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return cache
    }

    data class Task(
        val uri: Uri,
        val locale: Locale,
    )

    data class WordEntry(
        val word: String,
        val pinyinList: List<String>,
        val pinyin: String,
    )

    interface Parser {
        fun parse(file: File): List<WordEntry>
    }
}



