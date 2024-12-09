package com.github.dictionary

import androidx.paging.PagingSource
import com.github.dictionary.db.XunfeiDao
import com.github.dictionary.db.XunfeiIndex
import org.koin.android.ext.android.inject

class XunfeiFragment : DictionaryIndexFragment<XunfeiIndex>() {
    companion object {
        private const val TAG = "XunfeiFragment"
    }

    private val dao by inject<XunfeiDao>()

    override fun pagingSource(name: String): PagingSource<Int, XunfeiIndex> {
        return dao.pagingSource(keyword)
    }

    override fun download(id: Int, name: String) {
    }

    override fun toDictionaryWord(index: XunfeiIndex?): DictionaryAdapter.DictionaryWord? {
        index ?: return null
        return DictionaryAdapter.DictionaryWord(index.id, index.name)
    }
}