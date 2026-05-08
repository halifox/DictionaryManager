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
import androidx.lifecycle.lifecycleScope
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : FlutterActivity() {
    private val CHANNEL = "com.halifox.puredict/dictionary"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            lifecycleScope.launch(Dispatchers.IO) {
                when (call.method) {
                    "insertBatch" -> {
                        try {
                            val words = call.argument<List<Map<String, Any>>>("words")
                            if (words == null) {
                                result.error("INVALID_ARGUMENT", "words is null", null)
                            } else {
                                val ids = insertBatch(words)
                                result.success(ids)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            result.error("INSERT_ERROR", e.message, null)
                        }
                    }

                    "queryUserDictionary" -> {
                        try {
                            val words = queryUserDictionary()
                            result.success(words)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            result.error("QUERY_ERROR", e.message, null)
                        }
                    }

                    "clearUserDictionary" -> {
                        try {
                            val count = clearUserDictionary()
                            result.success(count)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            result.error("CLEAR_ERROR", e.message, null)
                        }
                    }

                    "deleteWordsByIds" -> {
                        try {
                            val ids = call.argument<List<Long>>("ids")
                            if (ids == null) {
                                result.error("INVALID_ARGUMENT", "ids is null", null)
                            } else {
                                val count = deleteWordsByIds(ids)
                                result.success(count)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            result.error("DELETE_ERROR", e.message, null)
                        }
                    }

                    "queryWordsByIds" -> {
                        try {
                            val ids = call.argument<List<Long>>("ids")
                            if (ids == null) {
                                result.error("INVALID_ARGUMENT", "ids is null", null)
                            } else {
                                val words = queryWordsByIds(ids)
                                result.success(words)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            result.error("QUERY_ERROR", e.message, null)
                        }
                    }

                    "isImeEnabled" -> {
                        try {
                            val enabled = checkInputMethodSettingsActive()
                            result.success(enabled)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            result.error("CHECK_ERROR", e.message, null)
                        }
                    }

                    "openImeSettings" -> {
                        try {
                            requestInputMethodSettingsActive()
                            result.success(true)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            result.error("SETTINGS_ERROR", e.message, null)
                        }
                    }

                    else -> {
                        result.notImplemented()
                    }
                }
            }
        }
    }

    private fun checkInputMethodSettingsActive(): Boolean {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val localImeService = ComponentName(this, IMEService::class.java)
        val enabledInputMethodList = inputMethodManager.enabledInputMethodList
        return enabledInputMethodList.any { it.component == localImeService }
    }

    private fun requestInputMethodSettingsActive() {
        startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
    }

    private fun queryUserDictionary(): List<Map<String, Any?>> {
        val words = mutableListOf<Map<String, Any?>>()
        val projection = arrayOf(
            UserDictionary.Words._ID,
            UserDictionary.Words.WORD,
            UserDictionary.Words.FREQUENCY,
            UserDictionary.Words.LOCALE,
            UserDictionary.Words.SHORTCUT
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

            while (it.moveToNext()) {
                words.add(
                    mapOf(
                        "id" to if (idIndex >= 0) it.getLong(idIndex) else null,
                        "word" to it.getString(wordIndex),
                        "frequency" to it.getInt(frequencyIndex),
                        "locale" to if (localeIndex >= 0) it.getString(localeIndex) else null,
                        "shortcut" to if (shortcutIndex >= 0) it.getString(shortcutIndex) else null
                    )
                )
            }
        }

        return words
    }

    private fun insertBatch(words: List<Map<String, Any>>): List<Long> {
        val allIds = mutableListOf<Long>()
        val chunkSize = 100

        words.chunked(chunkSize).forEach { chunk ->
            val operations = ArrayList<ContentProviderOperation>()
            chunk.forEach { entry ->
                operations.add(
                    ContentProviderOperation.newInsert(UserDictionary.Words.CONTENT_URI)
                        .withValue(UserDictionary.Words.WORD, entry["word"] as String)
                        .withValue(UserDictionary.Words.FREQUENCY, entry["frequency"] as Int?)
                        .withValue(UserDictionary.Words.LOCALE, entry["locale"] as String?)
                        .withValue(UserDictionary.Words.APP_ID, entry["appid"] as Int?)
                        .withValue(UserDictionary.Words.SHORTCUT, entry["shortcut"] as String?)
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

    private fun clearUserDictionary(): Int {
        return applicationContext.contentResolver.delete(
            UserDictionary.Words.CONTENT_URI,
            null,
            null
        )
    }

    private fun deleteWordsByIds(ids: List<Long>): Int {
        var deletedCount = 0
        ids.forEach { id ->
            val uri = ContentUris.withAppendedId(UserDictionary.Words.CONTENT_URI, id)
            deletedCount += applicationContext.contentResolver.delete(uri, null, null)
        }
        return deletedCount
    }

    private fun queryWordsByIds(ids: List<Long>): List<Map<String, Any?>> {
        val words = mutableListOf<Map<String, Any?>>()
        val projection = arrayOf(
            UserDictionary.Words._ID,
            UserDictionary.Words.WORD,
            UserDictionary.Words.FREQUENCY,
            UserDictionary.Words.LOCALE,
            UserDictionary.Words.SHORTCUT
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

                if (it.moveToFirst()) {
                    words.add(
                        mapOf(
                            "id" to if (idIndex >= 0) it.getLong(idIndex) else null,
                            "word" to it.getString(wordIndex),
                            "frequency" to it.getInt(frequencyIndex),
                            "locale" to if (localeIndex >= 0) it.getString(localeIndex) else null,
                            "shortcut" to if (shortcutIndex >= 0) it.getString(shortcutIndex) else null
                        )
                    )
                }
            }
        }

        return words
    }
}
