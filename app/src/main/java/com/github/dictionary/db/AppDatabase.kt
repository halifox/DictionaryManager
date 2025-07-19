package com.github.dictionary.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.github.dictionary.model.Dict

@Database(entities = [Dict::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dictDao(): DictDao
}
