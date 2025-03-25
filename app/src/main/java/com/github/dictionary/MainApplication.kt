package com.github.dictionary

import android.app.Application
import android.app.DownloadManager
import android.app.NotificationManager
import android.content.Intent
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import androidx.room.Room
import androidx.room.RoomDatabase.Callback
import androidx.sqlite.db.SupportSQLiteDatabase
import com.github.dictionary.db.AppDatabase
import com.github.dictionary.db.SogouDao
import com.github.dictionary.db.SogouIndex
import com.github.dictionary.importer.DictionaryImporter
import com.github.dictionary2.DownloadService
import com.google.android.material.color.DynamicColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module


class MainApplication : Application() {

    private val sogouDao by inject<SogouDao>()


    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        startService(Intent(this, DownloadService::class.java))
        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(
                module {
                    single {
                        Room.databaseBuilder(get(), AppDatabase::class.java, "dictionary_5.db")
                            .addCallback(object : Callback() {
                                override fun onCreate(db: SupportSQLiteDatabase) {
                                    MainScope().launch(Dispatchers.IO) {
                                        assets.open("sogou_pinyin_dict.list").bufferedReader().use {
                                            while (true) {
                                                val line = it.readLine()
                                                if (line.isNullOrBlank()) break
                                                val split = line.split("#").map { it.trim() }
                                                Log.d("TAG", "split: ${split}")
                                                sogouDao.insert(SogouIndex(split[0].toInt(), split[1]))
                                            }
                                        }
                                    }
                                }
                            })
                            .build()
                    }
                    single { get<AppDatabase>().sogouDao() }
                    single { get<AppDatabase>().xunfeiDao() }
                    single { get<AppDatabase>().baiduDao() }
                    single { get<AppDatabase>().downloadDao() }
                    single { getSystemService<InputMethodManager>() }
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
