package com.example.userdictionarydemo

import android.content.ContentValues
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.provider.Settings
import android.provider.UserDictionary
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainActivity : AppCompatActivity() {

    private val pickFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.data?.let { uri ->
                processScelFile(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        setupWindowInsets()
        launchFilePicker()
        observeUserDictionary()
        validateKeyboard()
        performDictionaryOperations()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun launchFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "*/*")
        }
        pickFileLauncher.launch(intent)
    }

    private fun observeUserDictionary() {
        val handlerThread = HandlerThread("ContentObserverThread").apply { start() }
        val handler = Handler(handlerThread.looper)
        contentResolver.registerContentObserver(
            UserDictionary.Words.CONTENT_URI,
            true,
            object : ContentObserver(handler) {
                override fun onChange(selfChange: Boolean) {
                    Log.d("MainActivity", "UserDictionary changed: $selfChange")
                }
            }
        )
    }

    private fun validateKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val enabledInputMethodList = imm.enabledInputMethodList
        val isKeyboardActive = enabledInputMethodList.any {
            it.id == "com.example.userdictionarydemo/.MyInputMethodService"
        }
        if (!isKeyboardActive) {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }
    }

    private fun performDictionaryOperations() {
        addWordToDictionary("hello", 100, "en_US")
        queryDictionary()
        updateWordFrequency("hello", 200)
        deleteWordFromDictionary("hello")
    }

    private fun addWordToDictionary(word: String, frequency: Int, locale: String) {
        val values = ContentValues().apply {
            put(UserDictionary.Words.WORD, word)
            put(UserDictionary.Words.FREQUENCY, frequency)
            put(UserDictionary.Words.LOCALE, locale)
        }
        contentResolver.insert(UserDictionary.Words.CONTENT_URI, values)
    }

    private fun queryDictionary() {
        contentResolver.query(
            UserDictionary.Words.CONTENT_URI,
            arrayOf(UserDictionary.Words.WORD, UserDictionary.Words.FREQUENCY),
            null,
            null,
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val word = cursor.getString(cursor.getColumnIndexOrThrow(UserDictionary.Words.WORD))
                val frequency = cursor.getInt(cursor.getColumnIndexOrThrow(UserDictionary.Words.FREQUENCY))
                Log.d("MainActivity", "Word: $word, Frequency: $frequency")
            }
        }
    }

    private fun updateWordFrequency(word: String, newFrequency: Int) {
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

    private fun deleteWordFromDictionary(word: String) {
        contentResolver.delete(
            UserDictionary.Words.CONTENT_URI,
            "${UserDictionary.Words.WORD} = ?",
            arrayOf(word)
        )
    }

    private fun processScelFile(uri: Uri) {
        val entries = parseScelFile(uri)
        entries.forEach { entry ->
            Log.d("MainActivity", "Pinyin: ${entry.pinyinList.joinToString(" ")}, Word: ${entry.word}")
        }
    }

    private fun uriToFile(uri: Uri): File? {
        val tempFile = File(cacheDir, "temp_file_${System.currentTimeMillis()}")
        return runCatching {
            contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output -> input.copyTo(output) }
            }
            tempFile
        }.getOrNull()
    }

    private fun parseScelFile(uri: Uri): Sequence<WordEntry> = sequence {
        RandomAccessFile(uriToFile(uri), "r").use { file ->
            try {
                file.seek(4)
                val mask = file.readUnsignedByte()
                val hzOffset = when (mask) {
                    0x44 -> 0x2628L
                    0x45 -> 0x26c4L
                    else -> throw IllegalArgumentException("Invalid scel file format")
                }
                val pyMap = parsePinyinMap(file)
                parseWords(file, hzOffset, pyMap)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error parsing scel file: ${e.message}")
            }
        }
    }

    private fun parsePinyinMap(file: RandomAccessFile): Map<Int, String> {
        val pyMap = mutableMapOf<Int, String>()
        file.seek(0x1540 + 4)
        while (true) {
            val pyCode = readUInt16(file)
            val pyLen = readUInt16(file)
            val pyStr = readUtf16Str(file, length = pyLen)
            pyMap[pyCode] = pyStr
            if (pyStr == "zuo") break
        }
        return pyMap
    }

    private suspend fun SequenceScope<WordEntry>.parseWords(
        file: RandomAccessFile,
        hzOffset: Long,
        pyMap: Map<Int, String>
    ) {
        file.seek(hzOffset)
        while (true) {
            val wordCount = readUInt16(file)
            val pinyinCount = readUInt16(file) / 2
            val pinyinList = List(pinyinCount) { pyMap[readUInt16(file)] ?: "" }
            repeat(wordCount) {
                val wordLen = readUInt16(file)
                val word = readUtf16Str(file, length = wordLen)
                file.skipBytes(12)
                yield(WordEntry(pinyinList, word))
            }
        }
    }

    private fun readUtf16Str(file: RandomAccessFile, offset: Long = -1, length: Int): String {
        if (offset >= 0) file.seek(offset)
        return ByteArray(length).also { file.readFully(it) }
            .toString(Charsets.UTF_16LE)
            .trimEnd('\u0000')
    }

    private fun readUInt16(file: RandomAccessFile): Int {
        return ByteBuffer.wrap(ByteArray(2).also { file.readFully(it) })
            .order(ByteOrder.LITTLE_ENDIAN)
            .short
            .toInt() and 0xFFFF
    }
}

data class WordEntry(val pinyinList: List<String>, val word: String)
