package com.github.dictionary.parser

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * 搜狗词库规则：
 * - 文件头固定偏移0x130，数据依次为标题区、分类区、描述区、示例词区。
 * - 字符编码UTF-16LE（小端Unicode）。
 * - 拼音表由多条拼音片段组成，包含索引和拼音字符串，用于构造词语拼音。
 * - 词语数据结构：
 *   - 同音词数量(short)，拼音索引字节长度(short)。
 *   - 拼音索引数组（对应拼音表索引）。
 *   - 词字节长度(short)，词内容(UTF-16LE)，扩展长度(short)，词频(short)，扩展数据。
 * - 词频为short，扩展数据长度为扩展长度减2，通常包含词性信息。
 * - 使用小端序读取数据，数据连续存储，按偏移依次解析。
 */
class SougoParser : IParser {
    override fun parse(path: String): List<ParsedResult> {
        val data = mutableListOf<ParsedResult>()

        // 读取 scel 文件
        val scelFile = File(path)
        // 读取文件字节，构建小端字节缓冲区
        val buffer = ByteBuffer.wrap(scelFile.readBytes()).order(ByteOrder.LITTLE_ENDIAN)

        // 设置指针到标题区起始位置
        buffer.position(0x130)
        // 读取标题区字节
        val titleBytes = ByteArray(0x338 - 0x130)
        buffer.get(titleBytes)
        // 读取分类区字节
        val categoryBytes = ByteArray(0x540 - 0x338)
        buffer.get(categoryBytes)
        // 读取描述区字节
        val descriptionBytes = ByteArray(0xd40 - 0x540)
        buffer.get(descriptionBytes)
        // 读取示例词区字节
        val sampleBytes = ByteArray(0x1540 - 0xd40)
        buffer.get(sampleBytes)

        // 打印各区内容（UTF-16LE编码）
        /**
         * 魔道祖师【官方推荐】
         * 其它
         * 粉丝们，蓝忘机和魏无羡在这里等你哦~
         * 魔道祖师 魏无羡 阴虎符 云梦江氏 江澄 无上邪尊 
         */
//        println(titleBytes.toString(Charsets.UTF_16LE))
//        println(categoryBytes.toString(Charsets.UTF_16LE))
//        println(descriptionBytes.toString(Charsets.UTF_16LE))
//        println(sampleBytes.toString(Charsets.UTF_16LE))

        // 读取拼音表总数
        val pinyinCount = buffer.getShort().toInt()
        // 跳过一个无用的 short（可能是保留字段）
        buffer.getShort()

        // 创建拼音表数组，索引对应拼音索引，内容是拼音字符串
        val pinyinArray = Array(pinyinCount) { "" }
        repeat(pinyinCount) {
            val index = buffer.getShort().toInt()        // 拼音索引
            val length = buffer.getShort().toInt()       // 拼音字节长度
            val pinyinBytes = ByteArray(length)           // 拼音字节数据
            buffer.get(pinyinBytes)
            pinyinArray[index] = pinyinBytes.toString(Charsets.UTF_16LE)
        }

        // 解析词语数据区，直到缓冲区无剩余字节
        while (buffer.hasRemaining()) {
            val sameWordCount = buffer.getShort().toInt()       // 词语同音词数量
            val pinyinIndexBytesLength = buffer.getShort().toInt() // 拼音索引字节长度
            val pinyinBuilder = StringBuilder()
            // 根据拼音索引构建拼音字符串
            repeat(pinyinIndexBytesLength / 2) {
                val pinyinIndex = buffer.getShort().toInt()
                pinyinBuilder.append(pinyinArray[pinyinIndex])
            }
            val pinyin = pinyinBuilder.toString()

            // 读取每个同音词的具体内容
            repeat(sameWordCount) {
                val wordByteLength = buffer.getShort().toInt()    // 词语字节长度
                val wordBytes = ByteArray(wordByteLength)
                buffer.get(wordBytes)
                val word = wordBytes.toString(Charsets.UTF_16LE) // 词语字符串

                val extLength = buffer.getShort().toInt()          // 词语扩展信息长度
                val wordFrequency = buffer.getShort().toFloat()      // 词频
                val extBytes = ByteArray(extLength - 2)            // 其余扩展数据
                buffer.get(extBytes)

                data += ParsedResult(pinyin, word, wordFrequency)
            }
        }
        return data
    }
}
