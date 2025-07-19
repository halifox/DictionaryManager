package com.github.dictionary.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.dictionary.model.Dict

@Dao
interface DictDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<Dict>)

    @Query("SELECT * FROM dict ORDER BY id ASC")
    fun pagingSource(): PagingSource<Int, Dict>
}
