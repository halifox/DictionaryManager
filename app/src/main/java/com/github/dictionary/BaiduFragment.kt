package com.github.dictionary

import android.app.DownloadManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingDataAdapter
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.dictionary.databinding.FragmentSogouBinding
import com.github.dictionary.databinding.ItemDictionaryIndexBinding
import com.github.dictionary.db.BaiduDao
import com.github.dictionary.db.BaiduIndex
import com.github.dictionary.db.XunfeiDao
import com.github.dictionary.db.XunfeiIndex
import com.google.android.material.appbar.AppBarLayout
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class BaiduFragment : DictionaryIndexFragment<BaiduIndex>() {
    companion object {
        private const val TAG = "XunfeiFragment"
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