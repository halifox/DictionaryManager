package com.github.dictionary2

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.github.dictionary.db.DownloadDao
import com.github.dictionary.db.DownloadTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.koin.android.ext.android.inject
import java.io.File
import java.io.InputStream
import java.io.RandomAccessFile

class DownloadService : LifecycleService() {
    private val context = this

    private val dao by inject<DownloadDao>()
    private val notificationManager by inject<NotificationManager>()
    private val client = OkHttpClient()

    override fun onCreate() {
        super.onCreate()
        startForeground()

        // 监听数据库任务变化
        observeDownloadQueue()
    }

    private fun startForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("Download Service", "Download Service", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, "Download Service")
            .setContentTitle("下载服务运行中")
            .setSmallIcon(R.drawable.stat_sys_download)
            .build()

        startForeground(1, notification)
    }


    private var nowTaskId: Long? = -1L
    private var job: Job? = null
    private fun observeDownloadQueue() {
        lifecycleScope.launch(Dispatchers.IO) {
            dao.getUnfinishedTasks().collect { tasks ->
                val taskId = tasks.firstOrNull()?.id
                if (nowTaskId != taskId) {
                    nowTaskId = taskId
                    job?.cancel()
                    job = null
                    job = launch(Dispatchers.IO) {
                        if (tasks.isNotEmpty()) {
                            downloadFile(tasks.first())
                        }
                    }
                }
            }
        }
    }


    private suspend fun downloadFile(task: DownloadTask) {
        try {
            dao.updateStatus(task.id, 1) // 1 = 下载中

            val file = File(task.filePath)
            val downloadedSize = if (file.exists()) file.length() else 0L

            val request = Request.Builder()
                .url(task.url)
                .apply {
                    if (downloadedSize > 0) {
                        header("Range", "bytes=$downloadedSize-")
                    }
                }
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                dao.updateStatus(task.id, 3) // 3 = 失败
                return
            }

            response.body?.byteStream()?.use { input ->
                RandomAccessFile(file, "rw").use { raf ->
                    raf.seek(downloadedSize)
                    input.copyTo(raf)
                }
            }
            dao.updateStatus(task.id, 2) // 2 = 已完成
        } catch (e: Exception) {
            e.printStackTrace()
            dao.updateStatus(task.id, 3) // 3 = 失败
        }
    }

    suspend fun InputStream.copyTo(out: RandomAccessFile, bufferSize: Int = DEFAULT_BUFFER_SIZE): Long {
        return coroutineScope {
            var bytesCopied: Long = 0
            val buffer = ByteArray(bufferSize)
            var bytes = read(buffer)
            while (isActive && bytes >= 0) {
                out.write(buffer, 0, bytes)
                bytesCopied += bytes
                bytes = read(buffer)
            }
            bytesCopied
        }
    }


    override fun onDestroy() {
        super.onDestroy()
    }
}

