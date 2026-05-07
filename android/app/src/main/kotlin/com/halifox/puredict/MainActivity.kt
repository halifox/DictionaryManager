package com.halifox.puredict

import android.content.ComponentName
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
                                val count = insertBatch(words)
                                result.success(count)
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

            while (it.moveToNext()) {
                words.add(
                    mapOf(
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

    private fun insertBatch(words: List<Map<String, Any>>): Int {
        val values = Array(words.size) { index ->
            val entry = words[index]
            ContentValues(5).apply {
                put(UserDictionary.Words.WORD, entry["word"] as String)
                put(UserDictionary.Words.FREQUENCY, entry["frequency"] as Int?)
                put(UserDictionary.Words.LOCALE, entry["locale"] as String?)
                put(UserDictionary.Words.APP_ID, entry["appid"] as Int?)
                put(UserDictionary.Words.SHORTCUT, entry["shortcut"] as String?)
            }
        }
        return applicationContext.contentResolver.bulkInsert(UserDictionary.Words.CONTENT_URI, values)
    }

    private fun clearUserDictionary(): Int {
        return applicationContext.contentResolver.delete(
            UserDictionary.Words.CONTENT_URI,
            null,
            null
        )
    }
}
