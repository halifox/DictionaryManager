package com.github.dictionary

import androidx.paging.PagingSource
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

@Entity(tableName = "sogou")
data class SogouEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,
    @ColumnInfo(name = "name", index = true)
    val name: String,
)

@Dao
interface SogouDao {
    @Insert
    suspend fun insert(sogouEntity: SogouEntity)

    @Query("SELECT * FROM sogou")
    suspend fun query(): List<SogouEntity>

    @Query("SELECT * FROM sogou WHERE name LIKE :name")
    fun pagingSource(name: String): PagingSource<Int, SogouEntity>
}


@Database(entities = [SogouEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sogouDao(): SogouDao
}