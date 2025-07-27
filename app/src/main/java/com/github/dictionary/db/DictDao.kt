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
    suspend fun getDictionaryById(id: Int): Dict

    @Query("SELECT * FROM dict WHERE source = :source AND name LIKE :key AND tiers = :tiers ORDER BY id ASC")
    fun searchDictionaries(source: String, key: String, tiers: Int): PagingSource<Int, Dict>

    @Query("SELECT * FROM dict WHERE source = :source AND pid = :pid AND tiers = :tiers ORDER BY id ASC")
    suspend fun getCategoriesByParentId(source: String, pid: String, tiers: Int): List<Dict>

    @RawQuery(observedEntities = [Dict::class])
    fun getSubTreeDict(query: SupportSQLiteQuery): PagingSource<Int, Dict>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: LocalRecord)

    @Query("SELECT * FROM record WHERE _id = :id")
    suspend fun getRecordById(id: Int): LocalRecord?

    @Query("DELETE FROM record WHERE _id = :id")
    suspend fun deleteRecordById(id: Int)

    @Query("SELECT dict.* FROM dict  JOIN record ON dict._id = record._id")
    fun getInstalledDictionaries(): PagingSource<Int, Dict>
}
