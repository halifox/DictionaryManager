package com.github.dictionary.db

import androidx.paging.PagingSource
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "baidu")
data class BaiduIndex(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "name", index = true)
    val name: String,
)

@Dao
interface BaiduDao {
    @Insert
    suspend fun insert(BaiduIndex: BaiduIndex)

    @Query("SELECT * FROM baidu")
    suspend fun query(): List<BaiduIndex>

    @Query("SELECT * FROM baidu WHERE name LIKE :name")
    fun pagingSource(name: String): PagingSource<Int, BaiduIndex>
}