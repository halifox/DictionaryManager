package com.github.dictionary.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.github.dictionary.model.Dict
import com.github.dictionary.model.LocalRecord

@Dao
interface DictDao {
    @Query("SELECT * FROM dict WHERE _id = :id")
    suspend fun getDict(id: Int): Dict

    @Query("SELECT * FROM dict WHERE source = :source AND name LIKE :key AND tiers = :tiers ORDER BY id ASC")
    fun search(source: String, key: String, tiers: Int): PagingSource<Int, Dict>

    @Query("SELECT * FROM dict WHERE source = :source AND pid = :pid AND tiers = :tiers ORDER BY id ASC")
    suspend fun getCategories(source: String, pid: String, tiers: Int): List<Dict>

    @RawQuery(observedEntities = [Dict::class])
    fun getSubTree(query: SupportSQLiteQuery): PagingSource<Int, Dict>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: LocalRecord)

    @Query("SELECT * FROM record WHERE _id = :id")
    suspend fun getById(id: Int): LocalRecord?

    @Query("DELETE FROM record WHERE _id = :id")
    suspend fun deleteById(id: Int)

}
