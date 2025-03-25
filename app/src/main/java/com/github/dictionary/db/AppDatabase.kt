package com.github.dictionary.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        SogouIndex::class,
        XunfeiIndex::class,
        BaiduIndex::class,
        DownloadTask::class,
    ],
    version = 2,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sogouDao(): SogouDao
    abstract fun xunfeiDao(): XunfeiDao
    abstract fun baiduDao(): BaiduDao
    abstract fun downloadDao(): DownloadDao
}