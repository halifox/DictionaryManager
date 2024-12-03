package com.github.dictionary

import android.accounts.AccountManager
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
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
import androidx.paging.cachedIn
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.dictionary.databinding.FragmentSogouBinding
import com.github.dictionary.databinding.ItemSogouBinding
import com.google.android.material.appbar.AppBarLayout
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SogouFragment : Fragment() {
    companion object {
        private const val TAG = "SogouFragment"
    }

    private val adapter = DictionaryAdapter()

    private var _binding: FragmentSogouBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentSogouBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView(requireContext())
        setupToolbar()
        binding.keyword.addTextChangedListener {
            keyword = "%${it ?: ""}%"
            adapter.refresh()
        }
    }

    private fun setupToolbar() {
        val appbar = requireActivity().findViewById<AppBarLayout>(R.id.appbar)

        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        toolbar.inflateMenu(R.menu.menu_sogou)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.search_bar -> {
                    if (!binding.keywordLayout.isVisible) {
                        appbar.setExpanded(false)
                    }
                    binding.keywordLayout.isVisible = !binding.keywordLayout.isVisible
                }
            }
            true
        }
    }


    private val sogouDao by inject<SogouDao>()

    private var keyword = "%"

    private fun setupRecyclerView(context: Context) {
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
        val pager = Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { sogouDao.pagingSource(keyword) }
        ).flow
            .cachedIn(lifecycleScope)
        lifecycleScope.launch {
            pager.collectLatest(adapter::submitData)
        }
    }

    class DictionaryAdapter : PagingDataAdapter<SogouEntity, DictionaryAdapter.WordViewHolder>(WordDiffCallback()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
            val binding = ItemSogouBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return WordViewHolder(binding)
        }

        override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        class WordViewHolder(private val binding: ItemSogouBinding) : RecyclerView.ViewHolder(binding.root), KoinComponent {
            private val downloadManager by inject<DownloadManager>()

            fun bind(word: SogouEntity?) {
                if (word == null) return
                binding.displayName.text = "${word.name}#${word.id}"
                binding.install.setOnClickListener {
                    download(word.id, word.name)
                }
            }

            private fun download(id: Int, name: String) {
                val request = DownloadManager.Request(Uri.parse("https://pinyin.sogou.com/dict/download_cell.php?id=$id&name=$name"))
                    .setTitle("$name${Sogou.suffix}")
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "$name${Sogou.suffix}")
                    .setNotificationVisibility(AccountManager.VISIBILITY_VISIBLE)
                downloadManager.enqueue(request)
            }
        }

        class WordDiffCallback : DiffUtil.ItemCallback<SogouEntity>() {
            override fun areItemsTheSame(oldItem: SogouEntity, newItem: SogouEntity): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: SogouEntity, newItem: SogouEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}