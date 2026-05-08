package com.halifox.puredict

import android.content.ComponentName
import android.content.ContentProviderOperation
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.provider.UserDictionary
import android.view.inputmethod.InputMethodManager
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine

class MainActivity : FlutterActivity(), DictionaryApi {

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        DictionaryApi.setUp(flutterEngine.dartExecutor.binaryMessenger, this)
    }

    override fun checkImeStatus(): Boolean {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val localImeService = ComponentName(this, IMEService::class.java)
        val enabledInputMethodList = inputMethodManager.enabledInputMethodList
        return enabledInputMethodList.any { it.component == localImeService }
    }

    override fun openImeSettings() {
        startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
    }

    override fun queryWords(): List<TableEntryData> {
        val words = mutableListOf<TableEntryData>()
        val projection = arrayOf(
            UserDictionary.Words._ID,
            UserDictionary.Words.WORD,
            UserDictionary.Words.FREQUENCY,
            UserDictionary.Words.LOCALE,
            UserDictionary.Words.SHORTCUT,
            UserDictionary.Words.APP_ID
        )

        val cursor = applicationContext.contentResolver.query(
            UserDictionary.Words.CONTENT_URI,
            projection,
            null,
            null,
            "${UserDictionary.Words.FREQUENCY} DESC"
        )

        cursor?.use {
            val wordIndex = it.getColumnIndex(UserDictionary.Words.WORD)
            val frequencyIndex = it.getColumnIndex(UserDictionary.Words.FREQUENCY)
            val localeIndex = it.getColumnIndex(UserDictionary.Words.LOCALE)
            val shortcutIndex = it.getColumnIndex(UserDictionary.Words.SHORTCUT)
            val idIndex = it.getColumnIndex(UserDictionary.Words._ID)
            val appIdIndex = it.getColumnIndex(UserDictionary.Words.APP_ID)

            while (it.moveToNext()) {
                words.add(
                    TableEntryData(
                        id = if (idIndex >= 0) it.getLong(idIndex) else null,
                        word = it.getString(wordIndex),
                        frequency = it.getInt(frequencyIndex).toLong(),
                        locale = if (localeIndex >= 0) it.getString(localeIndex) else null,
                        shortcut = if (shortcutIndex >= 0) it.getString(shortcutIndex) else null,
                        appId = if (appIdIndex >= 0) it.getInt(appIdIndex).toLong() else null
                    )
                )
            }
        }

        return words
    }

    override fun addWords(words: List<TableEntryData>): List<Long> {
        val allIds = mutableListOf<Long>()
        val chunkSize = 100

        words.chunked(chunkSize).forEach { chunk ->
            val operations = ArrayList<ContentProviderOperation>()
            chunk.forEach { entry ->
                operations.add(
                    ContentProviderOperation.newInsert(UserDictionary.Words.CONTENT_URI)
                        .withValue(UserDictionary.Words.WORD, entry.word)
                        .withValue(UserDictionary.Words.FREQUENCY, entry.frequency?.toInt())
                        .withValue(UserDictionary.Words.LOCALE, entry.locale)
                        .withValue(UserDictionary.Words.APP_ID, entry.appId?.toInt())
                        .withValue(UserDictionary.Words.SHORTCUT, entry.shortcut)
                        .build()
                )
            }

            val results = applicationContext.contentResolver.applyBatch(UserDictionary.AUTHORITY, operations)
            results.mapNotNullTo(allIds) { result ->
                result.uri?.let { ContentUris.parseId(it) }
            }
        }

        return allIds
    }

    override fun clearDictionary() {
        applicationContext.contentResolver.delete(
            UserDictionary.Words.CONTENT_URI,
            null,
            null
        )
    }

    override fun removeWords(ids: List<Long>): Long {
        var deletedCount = 0L
        ids.forEach { id ->
            val uri = ContentUris.withAppendedId(UserDictionary.Words.CONTENT_URI, id)
            deletedCount += applicationContext.contentResolver.delete(uri, null, null)
        }
        return deletedCount
    }

    override fun findWords(ids: List<Long>): List<TableEntryData> {
        val words = mutableListOf<TableEntryData>()
        val projection = arrayOf(
            UserDictionary.Words._ID,
            UserDictionary.Words.WORD,
            UserDictionary.Words.FREQUENCY,
            UserDictionary.Words.LOCALE,
            UserDictionary.Words.SHORTCUT,
            UserDictionary.Words.APP_ID
        )

        ids.forEach { id ->
            val cursor = applicationContext.contentResolver.query(
                UserDictionary.Words.CONTENT_URI,
                projection,
                "${UserDictionary.Words._ID} = ?",
                arrayOf(id.toString()),
                null
            )

            cursor?.use {
                val idIndex = it.getColumnIndex(UserDictionary.Words._ID)
                val wordIndex = it.getColumnIndex(UserDictionary.Words.WORD)
                val frequencyIndex = it.getColumnIndex(UserDictionary.Words.FREQUENCY)
                val localeIndex = it.getColumnIndex(UserDictionary.Words.LOCALE)
                val shortcutIndex = it.getColumnIndex(UserDictionary.Words.SHORTCUT)
                val appIdIndex = it.getColumnIndex(UserDictionary.Words.APP_ID)

                if (it.moveToFirst()) {
                    words.add(
                        TableEntryData(
                            id = if (idIndex >= 0) it.getLong(idIndex) else null,
                            word = it.getString(wordIndex),
                            frequency = it.getInt(frequencyIndex).toLong(),
                            locale = if (localeIndex >= 0) it.getString(localeIndex) else null,
                            shortcut = if (shortcutIndex >= 0) it.getString(shortcutIndex) else null,
                            appId = if (appIdIndex >= 0) it.getInt(appIdIndex).toLong() else null
                        )
                    )
                }
            }
        }

        return words
    }

    override fun addWord(word: TableEntryData): Long {
        val values = ContentValues().apply {
            put(UserDictionary.Words.WORD, word.word)
            put(UserDictionary.Words.FREQUENCY, word.frequency?.toInt())
            put(UserDictionary.Words.LOCALE, word.locale)
            put(UserDictionary.Words.SHORTCUT, word.shortcut)
            put(UserDictionary.Words.APP_ID, word.appId?.toInt())
        }

        val uri = applicationContext.contentResolver.insert(UserDictionary.Words.CONTENT_URI, values)
        return uri?.let { ContentUris.parseId(it) } ?: -1
    }

    override fun modifyWord(id: Long, word: TableEntryData): Long {
        val values = ContentValues().apply {
            put(UserDictionary.Words.WORD, word.word)
            put(UserDictionary.Words.FREQUENCY, word.frequency?.toInt())
            put(UserDictionary.Words.LOCALE, word.locale)
            put(UserDictionary.Words.SHORTCUT, word.shortcut)
            put(UserDictionary.Words.APP_ID, word.appId?.toInt())
        }

        val uri = ContentUris.withAppendedId(UserDictionary.Words.CONTENT_URI, id)
        return applicationContext.contentResolver.update(uri, values, null, null).toLong()
    }
}
