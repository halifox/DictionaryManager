package com.github.dictionary.repository

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.provider.UserDictionary
import androidx.paging.PagingSource
import androidx.sqlite.db.SimpleSQLiteQuery
import com.github.dictionary.db.DictDao
import com.github.dictionary.model.Dict
import com.github.dictionary.model.LocalRecord
import com.github.dictionary.parser.BaiduParser
import com.github.dictionary.parser.IParser
import com.github.dictionary.parser.ParsedResult
import com.github.dictionary.parser.QQParser
import com.github.dictionary.parser.SougoParser
import org.intellij.lang.annotations.Language
import java.io.File
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DictRepository @Inject constructor(private val context: Context, private val dao: DictDao) : DictDao by dao {


    fun getSubTreeQuery(pid: String, source: String, tiers: Int): PagingSource<Int, Dict> {
        /**
         * -- 该 SQL 查询用于从 dict 表中递归查找某个父节点（pid = ?）的所有子孙节点，
         * -- 并筛选出位于特定层级（tiers = ?）的唯一节点列表，按 id 升序返回。
         *
         * -- 查询过程包括三部分：
         * -- 1. 使用 WITH RECURSIVE 构建子树 sub_tree，从起始 pid 出发，递归遍历其所有后代节点。
         * -- 2. 使用 ROW_NUMBER() 对每个 id 进行去重，保留每个节点的第一条出现记录（防止多路径重复）。
         * -- 3. 最终筛选出指定 tiers 层的节点，并按 id 排序。
         *
         * -- 适用于组织结构、树形分类、权限分层等树状数据的多层遍历和筛选需求。
         *
         */
        @Language("RoomSql")
        val sql = """
                WITH RECURSIVE sub_tree AS (
                    SELECT * FROM dict WHERE pid = ? AND source = ?
                    UNION ALL
                    SELECT child.* FROM dict AS child
                                            JOIN sub_tree AS parent
                                                 ON child.pid = parent.id
                                                     AND child.tiers > parent.tiers
                ),
                               deduped AS (
                                   SELECT * FROM (
                                                     SELECT *, ROW_NUMBER() OVER (PARTITION BY id ORDER BY id) AS rn
                                                     FROM sub_tree
                                                 ) WHERE rn = 1
                               )
                SELECT * FROM deduped WHERE tiers = ? ORDER BY id ASC
    """.trimIndent()
        val query = SimpleSQLiteQuery(sql, arrayOf(pid, source, tiers))
        return dao.getSubTreeDict(query)
    }


    fun getUserDictionaryDownloadUrl(dict: Dict): String {
        return when (dict.source) {
            "sougo" -> "https://pinyin.sogou.com/d/dict/download_cell.php?id=${dict.id}&name=${dict.name}"
            "baidu" -> "https://shurufa.baidu.com/dict_innerid_download?innerid=${dict.innerId}"
            "qq" -> "https://cdict.qq.pinyin.cn/v1/download?dict_id=${dict.id}"
            else -> throw IllegalArgumentException()
        }
    }

    fun getUserDictionaryFileName(dict: Dict): String {
        return when (dict.source) {
            "sougo" -> "${dict.id}.scel"
            "baidu" -> "${dict.id}.bdict"
            "qq" -> "${dict.id}.qpyd"
            else -> throw IllegalArgumentException()
        }
    }

    fun getUserDictionaryMaxTiers(source: String): Int {
        return when (source) {
            "sougo" -> 4
            "baidu" -> 3
            "qq" -> 4
            else -> 0
        }
    }

    fun getUserDictionaryParser(extension: String): IParser {
        return when (extension) {
            "scel"/*sougo*/ -> SougoParser()
            "bdict"/*baidu*/ -> BaiduParser()
            "qpyd"/*qq*/ -> QQParser()
            else -> throw IllegalArgumentException()
        }
    }

    fun parseUserDictionaryFile(file: File): List<ParsedResult> {
        val parser = getUserDictionaryParser(file.extension)
        val results = parser.parse(file.absolutePath)
        return results
    }

    suspend fun installUserDictionary(dict: Dict, parsedResults: List<ParsedResult>): Int {
        val ids = mutableListOf<Long>()
        parsedResults.forEach {
            val values = ContentValues().apply {
                put(UserDictionary.Words.WORD, it.word)
                put(UserDictionary.Words.SHORTCUT, it.pinyin)
                put(UserDictionary.Words.FREQUENCY, it.wordFrequency.toInt())
                put(UserDictionary.Words.LOCALE, Locale.SIMPLIFIED_CHINESE.toString())
                put(UserDictionary.Words.APP_ID, dict._id)
            }
            val result = context.contentResolver.insert(UserDictionary.Words.CONTENT_URI, values)
            if (result != null) {
                val id = ContentUris.parseId(result)
                ids += id
            }
        }
        dao.insertRecord(LocalRecord(dict._id, ids))
        return ids.size
    }

    suspend fun uninstallUserDictionary(record: LocalRecord) {
        context.contentResolver.delete(
            UserDictionary.Words.CONTENT_URI,
            "${UserDictionary.Words._ID} IN (${record.ids.joinToString(",") { "?" }})",
            record.ids.map { it.toString() }.toTypedArray(),
        )
        dao.deleteRecordById(record._id)
    }

    suspend fun queryUserDictionaryByIds(record: LocalRecord): List<ParsedResult> {
        val results = mutableListOf<ParsedResult>()
        context.contentResolver.query(
            UserDictionary.Words.CONTENT_URI,
            null,
            "${UserDictionary.Words._ID} IN (${record.ids.joinToString(",") { "?" }})",
            record.ids.map { it.toString() }.toTypedArray(),
            null
        )?.use { cursor ->
            val wordIndex = cursor.getColumnIndex(UserDictionary.Words.WORD)
            val shortcutIndex = cursor.getColumnIndex(UserDictionary.Words.SHORTCUT)
            val freqIndex = cursor.getColumnIndex(UserDictionary.Words.FREQUENCY)
            while (cursor.moveToNext()) {
                val word = cursor.getString(wordIndex)
                val shortcut = cursor.getString(shortcutIndex)
                val frequency = cursor.getInt(freqIndex)
                results.add(ParsedResult(word, shortcut, frequency.toFloat()))
            }
        }
        return results
    }
}
