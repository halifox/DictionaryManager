package com.github.dictionary.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.github.dictionary.model.Dict

@Dao
interface DictDao {
    @Query("SELECT * FROM dict WHERE source = :source AND name LIKE :key AND tiers = :tiers ORDER BY id ASC")
    fun search(source: String, key: String, tiers: Int): PagingSource<Int, Dict>

    @Query("SELECT * FROM dict WHERE source = :source AND pid = :pid AND tiers = :tiers ORDER BY id ASC")
    suspend fun getCategories(source: String, pid: String, tiers: Int): List<Dict>

    @RawQuery(observedEntities = [Dict::class])
    fun getSubTree(query: SupportSQLiteQuery): PagingSource<Int, Dict>
}
