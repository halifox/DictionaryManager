package com.github.dictionary.repository

import android.util.Log
import androidx.paging.PagingSource
import androidx.sqlite.db.SimpleSQLiteQuery
import com.github.dictionary.db.DictDao
import com.github.dictionary.model.Dict
import org.intellij.lang.annotations.Language
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DictRepository @Inject constructor(private val dao: DictDao) : DictDao by dao {

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
        Log.d("TAG", "getSubTreeQuery:${listOf(pid, source, tiers)} ")
        val query = SimpleSQLiteQuery(sql, arrayOf(pid, source, tiers))
        return dao.getSubTree(query)
    }

}
