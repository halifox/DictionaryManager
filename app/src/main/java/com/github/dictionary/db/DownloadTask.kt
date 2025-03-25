package com.github.dictionary.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "download_queue")
data class DownloadTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val fileName: String,
    val filePath: String,
    val status: Int = 0, // 0: 待下载, 1: 下载中, 2: 已完成, 3: 失败
    val progress: Int = 0,
    val priority: Int = 1,
    val errorMessage: String? = null,
    val etag: String? = null,
    val checksum: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)


@Dao
interface DownloadDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: DownloadTask): Long

    @Update
    suspend fun update(task: DownloadTask)

    @Query("SELECT * FROM download_queue WHERE status IN (0,1) ORDER BY priority DESC, createdAt ASC")
    fun getUnfinishedTasks(): Flow<List<DownloadTask>>

    @Query("SELECT * FROM download_queue WHERE status = 0 ORDER BY priority DESC, createdAt ASC")
    fun getPendingTasksFlow(): Flow<List<DownloadTask>>

    @Query("UPDATE download_queue SET status = :status, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateStatus(id: Long, status: Int, timestamp: Long = System.currentTimeMillis())
}
