package com.github.dictionary.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.dictionary.model.Converters
import com.github.dictionary.model.Dict
import com.github.dictionary.model.LocalRecord

@Database(entities = [Dict::class, LocalRecord::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dictDao(): DictDao
}
