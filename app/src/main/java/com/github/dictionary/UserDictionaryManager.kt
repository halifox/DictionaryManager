package com.github.dictionary

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.HandlerThread
import android.provider.UserDictionary
import android.util.Log
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner


class UserDictionaryManager(context: Context) {
    companion object {
        private const val TAG = "UserDictionaryManager"
    }

    private val contentResolver = context.contentResolver
    private val handlerThread = HandlerThread(TAG).apply { start() }
    private val handler = Handler(handlerThread.looper)

    fun registerObserver(lifecycle: Lifecycle, onChange: () -> Unit) {
        lifecycle.addObserver(observer = object : DefaultLifecycleObserver {
            val observer = object : ContentObserver(handler) {
                override fun onChange(selfChange: Boolean) {
                    onChange.invoke()
                }
            }

            override fun onStart(owner: LifecycleOwner) {
                contentResolver.registerContentObserver(UserDictionary.Words.CONTENT_URI, true, observer)
            }

            override fun onStop(owner: LifecycleOwner) {
                contentResolver.unregisterContentObserver(observer)
            }
        })
    }


    /**
     * @param frequency 介于 1 和 255 之间的值。值越高，频率越高。
     * @param locale 此单词所属的区域设置。如果它与所有区域设置相关，则为 null。Locale 由 Locale. toString（） 返回的字符串定义。
     * @param appid 插入单词的应用程序的 uid。
     * @param shortcut 此单词的可选快捷方式。键入快捷方式时，支持的 IME 也应建议此行中的单词作为替代拼写。
     */
    fun insert(
        word: String,
        shortcut: String? = null,
        frequency: Int? = null,
        locale: String? = null,
        appid: Int? = null,
    ) {
        val values = ContentValues().apply {
            put(UserDictionary.Words.WORD, word)
            if (shortcut != null) {
                put(UserDictionary.Words.SHORTCUT, shortcut)//def:null
            }
            if (frequency != null) {
                put(UserDictionary.Words.FREQUENCY, frequency)//def:1
            }
            if (locale != null) {
                put(UserDictionary.Words.LOCALE, locale)//def:null
            }
            if (appid != null) {
                put(UserDictionary.Words.APP_ID, appid)//def:0
            }
        }
        contentResolver.insert(UserDictionary.Words.CONTENT_URI, values)
    }

    fun query(
        projection: Array<String>? = null,
        selection: String? = null,
        selectionArgs: Array<String?>? = null,
        sortOrder: String? = null,
    ): List<Word> {
        val words = mutableListOf<Word>()
        contentResolver.query(
            UserDictionary.Words.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val word = cursor.getStringOrNull(cursor.getColumnIndex(UserDictionary.Words.WORD))
                val frequency = cursor.getIntOrNull(cursor.getColumnIndex(UserDictionary.Words.FREQUENCY))
                val locale = cursor.getStringOrNull(cursor.getColumnIndex(UserDictionary.Words.LOCALE))
                val appid = cursor.getIntOrNull(cursor.getColumnIndex(UserDictionary.Words.APP_ID))
                val shortcut = cursor.getStringOrNull(cursor.getColumnIndex(UserDictionary.Words.SHORTCUT))
                Log.d("queryDictionary", "Word: $word, Frequency: $frequency locale:$locale appid:$appid shortcut:$shortcut")
                words.add(Word(word, frequency, locale, appid, shortcut))
            }
        }
        return words.toList()
    }

    fun update(word: String, newFrequency: Int) {
        val values = ContentValues().apply {
            put(UserDictionary.Words.FREQUENCY, newFrequency)
        }
        contentResolver.update(
            UserDictionary.Words.CONTENT_URI,
            values,
            "${UserDictionary.Words.WORD} = ?",
            arrayOf(word)
        )
    }

    fun delete(
        word: String? = null,
        shortcut: String? = null,
        frequency: Int? = null,
        locale: String? = null,
        appid: Int? = null,
    ) {
        val (selection, selectionArgs) = selection(word, shortcut, frequency, locale, appid)
        contentResolver.delete(UserDictionary.Words.CONTENT_URI, selection, selectionArgs)
    }


    fun clean() {
        contentResolver.delete(UserDictionary.Words.CONTENT_URI, null, null)
    }

    fun selection(
        word: String? = null,
        shortcut: String? = null,
        frequency: Int? = null,
        locale: String? = null,
        appid: Int? = null,
    ): Pair<String, Array<String>> {
        val sb = StringBuilder()
        val sa = mutableListOf<String>()

        if (word != null) {
            if (sb.length > 0) sb.append("AND ")
            sb.append("${UserDictionary.Words.WORD} = ? ")
            sa.add(word)
        }

        if (shortcut != null) {
            if (sb.length > 0) sb.append("AND ")
            sb.append("${UserDictionary.Words.SHORTCUT} = ? ")
            sa.add(shortcut)
        }

        if (frequency != null) {
            if (sb.length > 0) sb.append("AND ")
            sb.append("${UserDictionary.Words.FREQUENCY} = ? ")
            sa.add(java.lang.String.valueOf(frequency))
        }

        if (locale != null) {
            if (sb.length > 0) sb.append("AND ")
            sb.append("${UserDictionary.Words.LOCALE} = ? ")
            sa.add(locale)
        }

        if (appid != null) {
            if (sb.length > 0) sb.append("AND ")
            sb.append("${UserDictionary.Words.APP_ID} = ? ")
            sa.add(java.lang.String.valueOf(appid))
        }

        val selection = sb.toString()
        val selectionArgs = sa.toTypedArray<String>()
        return Pair(selection, selectionArgs)
    }

    fun queryLocales(): List<String?> {
        val locales = mutableSetOf<String?>()
        contentResolver.query(
            UserDictionary.Words.CONTENT_URI,
            arrayOf(UserDictionary.Words.LOCALE),
            null,
            null,
            null,
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val locale = cursor.getStringOrNull(cursor.getColumnIndex(UserDictionary.Words.LOCALE))
                locales.add(locale)
            }
        }
        return locales.toList()
    }

    data class Word(
        val word: String? = null,
        val frequency: Int? = null,
        val locale: String? = null,
        val appid: Int? = null,
        val shortcut: String? = null,
    )

}