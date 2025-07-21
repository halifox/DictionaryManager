package com.github.dictionary.parser

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.InflaterInputStream

/**
 * QQ词库规则：
 * - 文件头偏移0x38处为4字节整型元数据结束位置(metadataEndPos)。
 * - 紧跟4字节整型词条数量(entryCount)。
 * - 元数据区0x60至metadataEndPos，UTF-16LE编码。
 * - 0x60之后为Zlib压缩数据，解压后格式：
 *   - 每个词条10字节：
 *     - 1字节拼音长度(pinyinLength)
 *     - 1字节词长度(wordLength)
 *     - 4字节词频(wordFrequency)
 *     - 4字节拼音偏移地址(pinyinOffset)
 *   - 拼音和词内容均在解压区，根据偏移读取：
 *     - 拼音UTF-8编码，词UTF-16LE编码。
 * - 词条数量由entryCount控制，依次解析所有词条。
 */
class QQParser : IParser {
    override fun parse(path: String): List<ParsedResult> {
        val data = mutableListOf<ParsedResult>()

        // 读取文件字节
        val inputFile = File(path)
        val rawBuffer = ByteBuffer.wrap(inputFile.readBytes()).order(ByteOrder.LITTLE_ENDIAN)

        // 定位到0x38，读取元数据结束位置和条目数量
        rawBuffer.position(0x38)
        val metadataEndPos = rawBuffer.getInt()
        rawBuffer.position(0x44)
        val entryCount = rawBuffer.getInt()

        // 定位到0x60，读取元数据内容
        rawBuffer.position(0x60)
        val metadataBytes = ByteArray(metadataEndPos - 0x60)
        rawBuffer.get(metadataBytes)
//        println(metadataEndPos)
//        println(entryCount)
        /**
         * Name: 成语
         * Type: 人文
         * FirstType: 人文社科
         * Intro: 成语
         * Example: 僾见忾闻 阿保之劳 阿顺取容 阿旨顺情 阿谀求容 阿谀谄佞 挨饿受冻 哀乐相生 哀矜惩创 矮矮实实
         */
//        println(metadataBytes.toString(Charsets.UTF_16LE))


        // 读取剩余部分为压缩数据
        val compressedData = ByteArray(rawBuffer.remaining())
        rawBuffer.get(compressedData)

        // 解压缩数据
        val decompressedOutput = ByteArrayOutputStream()
        val compressedInput = ByteArrayInputStream(compressedData)
        val inflaterStream = InflaterInputStream(compressedInput)
        inflaterStream.copyTo(decompressedOutput)
        val decompressedBytes = decompressedOutput.toByteArray()

        // 用ByteBuffer包装解压后的数据
        val dataBuffer = ByteBuffer.wrap(decompressedBytes).order(ByteOrder.LITTLE_ENDIAN)
        val readBuffer = ByteBuffer.wrap(decompressedBytes).order(ByteOrder.LITTLE_ENDIAN)

        // 逐条读取词条
        repeat(entryCount) {
            // 读取固定长度条目头（10字节）
            val entryHeader = ByteArray(10)
            dataBuffer.get(entryHeader)
            val headerBuffer = ByteBuffer.wrap(entryHeader).order(ByteOrder.LITTLE_ENDIAN)

            // 读取拼音长度、词长度、跳过4字节无用数据，读取拼音偏移地址
            val pinyinLength = headerBuffer.get().toInt()
            val wordLength = headerBuffer.get().toInt()
            val wordFrequency = headerBuffer.getFloat() // 忽略
            val pinyinOffset = headerBuffer.getInt()
            // 根据偏移读取拼音
            readBuffer.position(pinyinOffset)
            val pinyinBytes = ByteArray(pinyinLength)
            readBuffer.get(pinyinBytes)
            val pinyin = pinyinBytes.toString(Charsets.UTF_8)

            // 读取词
            val wordBytes = ByteArray(wordLength)
            readBuffer.get(wordBytes)
            val word = wordBytes.toString(Charsets.UTF_16LE)

            data += ParsedResult(pinyin, word, wordFrequency)
        }
        return data
    }

}
