package com.github.dictionary

import android.app.Application
import android.app.DownloadManager
import android.app.NotificationManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import androidx.room.Room
import com.github.dictionary.db.AppDatabase
import com.github.dictionary.importer.DictionaryImporter
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module


class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(
                module {
                    single {
                        Room.databaseBuilder(get(), AppDatabase::class.java, "dictionary.db")
                            .createFromAsset("dictionary.db")
                            .fallbackToDestructiveMigration()
                            .build()
                    }
                    single { get<AppDatabase>().sogouDao() }
                    single { get<AppDatabase>().xunfeiDao() }
                    single { get<AppDatabase>().baiduDao() }
                    single { getSystemService<DownloadManager>() }
                    single { getSystemService<NotificationManager>() }
                    single { NotificationManagerCompat.from(get()) }
                    single { DictionaryImporter(get()) }
                    single { UserDictionaryManager(get()) }
                }
            )
        }
    }
}
