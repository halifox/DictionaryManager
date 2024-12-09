package com.github.dictionary

import androidx.paging.PagingSource
import com.github.dictionary.db.BaiduDao
import com.github.dictionary.db.BaiduIndex
import org.koin.android.ext.android.inject

class BaiduFragment : DictionaryIndexFragment<BaiduIndex>() {
    companion object {
        private const val TAG = "BaiduFragment"
    }

    private val dao by inject<BaiduDao>()

    override fun pagingSource(name: String): PagingSource<Int, BaiduIndex> {
        return dao.pagingSource(keyword)
    }

    override fun download(id: Int, name: String) {
    }

    override fun toDictionaryWord(index: BaiduIndex?): DictionaryAdapter.DictionaryWord? {
        index ?: return null
        return DictionaryAdapter.DictionaryWord(index.id, index.name)
    }
}