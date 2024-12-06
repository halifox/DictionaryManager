package com.github.dictionary.importer

import android.accounts.AccountManager
import android.app.DownloadManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.EOFException
import java.io.File
import java.io.RandomAccessFile

/**
 * 处理搜狗词库文件（.scel文件）的类，提供对词条的提取和处理。
 */
class Sogou : DictionaryImporter.Parser, DictionaryImporter.Downloader, KoinComponent {
    companion object {
        private const val TAG = "Sogou"
        const val EXTENSION = "scel"
    }

    private val downloadManager by inject<DownloadManager>()

    override fun download(id: Int, name: String) {
        val request = DownloadManager.Request(Uri.parse("https://pinyin.sogou.com/dict/download_cell.php?id=$id&name=$name"))
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "${name}.${EXTENSION}")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            request.setNotificationVisibility(AccountManager.VISIBILITY_VISIBLE)
        }
        downloadManager.enqueue(request)
    }


    override fun verify(file: File): Boolean {
        return file.extension == EXTENSION
    }

    /**
     * 从搜狗词库文件中提取词条。
     *
     * @param dictionaryFile 搜狗词库文件。
     * @return 包含所有词条的序列。
     */
    override fun parse(file: File): List<DictionaryImporter.Entry> {
        RandomAccessFile(file, "r").use { file ->
            try {
                file.seek(4) // 跳过文件头的前4字节
                val headerByte = file.readUnsignedByte()
                val wordDataOffset = getWordDataOffset(headerByte) // 获取词条数据起始位置
                val pinyinMapping = extractPinyinMapping(file) // 提取拼音映射表
                return parseWordsFromFile(file, wordDataOffset, pinyinMapping) // 解析词条数据
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing SCEL file: ${e.message}")
                return emptyList()
            }
        }
    }


    /**
     * 根据文件头字节获取汉字区块的偏移量。
     *
     * @param headerByte 文件头字节。
     * @return 汉字区块的偏移量。
     * @throws IllegalArgumentException 如果文件格式不正确，抛出异常。
     */
    private fun getWordDataOffset(headerByte: Int): Long {
        return when (headerByte) {
            0x44 -> 0x2628L // 对应的偏移量
            0x45 -> 0x26c4L // 对应的偏移量
            else -> throw IllegalArgumentException("Invalid SCEL file format")
        }
    }

    /**
     * 提取拼音映射表，用于将拼音编码映射到拼音字符串。
     *
     * @param file 搜狗词库文件。
     * @return 拼音编码到拼音字符串的映射。
     */
    private fun extractPinyinMapping(file: RandomAccessFile): Map<Int, String> {
        val pinyinMapping = mutableMapOf<Int, String>()
        file.seek(0x1540 + 4) // 跳过头部信息
        while (true) {
            val pinyinCode = file.readUnsignedLittleEndianShort() // 读取拼音编码
            val pinyinLength = file.readUnsignedLittleEndianShort() // 读取拼音长度
            val pinyin = file.readUtf16String(pinyinLength) // 读取拼音
            pinyinMapping[pinyinCode] = pinyin
            if (pinyin == "zuo") break // 遇到"zuo"结束读取
        }
        return pinyinMapping
    }

    /**
     * 从文件中解析词条数据，并通过 [SequenceScope] 将其作为词条序列逐一返回。
     *
     * @param file 搜狗词库文件。
     * @param wordDataOffset 词条数据起始位置偏移量。
     * @param pinyinMapping 拼音映射表。
     */
    private fun parseWordsFromFile(
        file: RandomAccessFile,
        wordDataOffset: Long,
        pinyinMapping: Map<Int, String>,
    ): List<DictionaryImporter.Entry> {
        val wordEntries = mutableListOf<DictionaryImporter.Entry>()
        try {
            file.seek(wordDataOffset) // 定位到汉字数据起始位置
            while (true) {
                val wordCount = file.readUnsignedLittleEndianShort() // 读取词条数量
                val pinyinCount = file.readUnsignedLittleEndianShort() / 2 // 读取拼音数量
                val pinyinList = List(pinyinCount) { // 获取拼音列表
                    pinyinMapping.getOrDefault(file.readUnsignedLittleEndianShort(), "")
                }
                repeat(wordCount) {
                    val wordLength = file.readUnsignedLittleEndianShort() // 读取词语长度
                    val word = file.readUtf16String(wordLength) // 读取词语
                    file.skipBytes(12) // 跳过无关的字节
                    wordEntries.add(DictionaryImporter.Entry(word, pinyinList, pinyinList.joinToString(""))) // 返回词条
                }
            }
        } catch (e: Exception) {
//            e.printStackTrace()
        }
        return wordEntries
    }

    /**
     * 读取指定长度的UTF-16编码字符串。
     *
     * @param file 词库文件。
     * @param offset 字节偏移量，默认为-1表示不改变文件指针位置。
     * @param length 字符串长度。
     * @return 读取的UTF-16字符串。
     */
    private fun RandomAccessFile.readUtf16String(length: Int): String {
        val byteArray = ByteArray(length)
        readFully(byteArray)
        return byteArray
            .toString(Charsets.UTF_16LE)
            .trimEnd('\u0000')
    }

    /**
     * 从文件中读取一个无符号16位整数。
     *
     * @param file 词库文件。
     * @param buffer 用于读取数据的缓存区，避免每次都重新分配内存。
     * @return 读取的无符号16位整数。
     */
    private fun RandomAccessFile.readUnsignedLittleEndianShort(): Int {
        val ch1 = read()
        val ch2 = read()
        if ((ch1 or ch2) < 0) throw EOFException()
        return (ch1 shl 0) + (ch2 shl 8)
    }
}
