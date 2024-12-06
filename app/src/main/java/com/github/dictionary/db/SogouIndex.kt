package com.github.dictionary.db

import androidx.paging.PagingSource
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "sogou")
data class SogouIndex(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "name", index = true)
    val name: String,
)

@Dao
interface SogouDao {
    @Insert
    suspend fun insert(sogouIndex: SogouIndex)

    @Query("SELECT * FROM sogou")
    suspend fun query(): List<SogouIndex>

    @Query("SELECT * FROM sogou WHERE name LIKE :name")
    fun pagingSource(name: String): PagingSource<Int, SogouIndex>
}