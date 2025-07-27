package com.github.dictionary.parser

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * 百度词库规则：
 * - 文件头8字节魔数 "biptbdsw" 验证格式。
 * - 偏移0x60处4字节整型词库数据结束位置。
 * - 词条数据从偏移0x350开始，逐条存储。
 * - 每条词条结构：
 *   - 4字节整型拼音对数量N。
 *   - N组拼音索引对，每组2字节：
 *     - 1字节声母索引
 *     - 1字节韵母索引
 *   - 紧接N×2字节UTF-16LE中文词文本。
 * - 拼音由声母和韵母索引拼接形成完整拼音，索引越界视为空串。
 * - 采用固定头部+拼音索引对+中文词结构，便于拼音检索。
 */

class BaiduParser : IParser {
    // 拼音声母列表，对应拼音开头部分
    private val initials = listOf(
        "c", "d", "b", "f", "g", "h", "ch", "j", "k", "l", "m", "n", "",
        "p", "q", "r", "s", "t", "sh", "zh", "w", "x", "y", "z"
    )

    // 拼音韵母列表，对应拼音结尾部分
    private val finals = listOf(
        "uang", "iang", "iong", "ang", "eng", "ian", "iao", "ing", "ong",
        "uai", "uan", "ai", "an", "ao", "ei", "en", "er", "ua", "ie", "in",
        "iu", "ou", "ia", "ue", "ui", "un", "uo", "a", "e", "i", "o", "u", "v"
    )

    override fun parse(path: String): List<ParsedResult> {
        val data = mutableListOf<ParsedResult>()
        // 读取文件为字节数组，使用 ByteBuffer 方便二进制读取
        val file = File(path)
        val buffer = ByteBuffer.wrap(file.readBytes()).order(ByteOrder.LITTLE_ENDIAN)

        buffer.position(0)
        // 读取文件头部的8字节魔数，用于校验文件格式是否正确
        val magic = ByteArray(8)
        buffer.get(magic)
        if (!magic.contentEquals("biptbdsw".toByteArray())) {
            throw IllegalArgumentException("Not a Baidu file")
        }

        // 定位到偏移0x60读取字典数据结束位置（整型4字节）
        buffer.position(0x60)
        val dataEndPosition = buffer.getInt()

        // 定位到偏移0x350开始读取拼音词条
        buffer.position(0x350)

        // 读取词条直到当前位置超过数据结束位置
        while (buffer.position() < dataEndPosition) {
            // 读取词条长度（int，表示拼音对数量）
            val entryLength = buffer.getInt()

            // 用于拼接当前词条的完整拼音字符串
            val pinyinBuilder = StringBuilder()

            // 遍历词条每个拼音对（声母索引 + 韵母索引）
            repeat(entryLength) {
                val initialIndex = buffer.get().toInt()
                val finalIndex = buffer.get().toInt()

                // 安全获取对应声母和韵母，越界时用空字符串替代
                val initial = initials.getOrElse(initialIndex) { "" }
                val finalPart = finals.getOrElse(finalIndex) { "" }

                pinyinBuilder.append(initial).append(finalPart)
            }
            val pinyin = pinyinBuilder.toString()

            // 读取词条对应的中文词，长度为拼音对数量 * 2字节（UTF-16LE编码）
            val dataBytes = ByteArray(entryLength * 2)
            buffer.get(dataBytes)
            val word = dataBytes.toString(Charsets.UTF_16LE)

            data += ParsedResult(pinyin, word, 1f)
        }
        return data
    }
}

