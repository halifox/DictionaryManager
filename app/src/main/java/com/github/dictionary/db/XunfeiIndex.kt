package com.github.dictionary.db

import androidx.paging.PagingSource
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "xunfei")
data class XunfeiIndex(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "name", index = true)
    val name: String,
)

@Dao
interface XunfeiDao {
    @Insert
    suspend fun insert(XunfeiIndex: XunfeiIndex)

    @Query("SELECT * FROM xunfei")
    suspend fun query(): List<XunfeiIndex>

    @Query("SELECT * FROM xunfei WHERE name LIKE :name")
    fun pagingSource(name: String): PagingSource<Int, XunfeiIndex>
}