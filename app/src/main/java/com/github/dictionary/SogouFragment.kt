package com.github.dictionary

import androidx.paging.PagingSource
import com.github.dictionary.db.SogouDao
import com.github.dictionary.db.SogouIndex
import com.github.dictionary.importer.Sogou
import org.koin.android.ext.android.inject

class SogouFragment : DictionaryIndexFragment<SogouIndex>() {
    companion object {
        private const val TAG = "SogouFragment"
    }

    private val dao by inject<SogouDao>()
    private val sogou = Sogou()

    override fun pagingSource(name: String): PagingSource<Int, SogouIndex> {
        return dao.pagingSource(keyword)
    }

    override fun download(id: Int, name: String) {
        sogou.download(id, name)
    }

    override fun toDictionaryWord(index: SogouIndex?): DictionaryAdapter.DictionaryWord? {
        index ?: return null
        return DictionaryAdapter.DictionaryWord(index.id, index.name)
    }
}