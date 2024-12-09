package com.github.dictionary

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
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
import com.github.dictionary.DictionaryIndexFragment.DictionaryAdapter.DictionaryWord
import com.github.dictionary.databinding.FragmentDictionaryIndexBinding
import com.github.dictionary.databinding.ItemDictionaryIndexBinding
import com.google.android.material.appbar.AppBarLayout
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent

abstract class DictionaryIndexFragment<Index : Any> : Fragment() {
    private var _binding: FragmentDictionaryIndexBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentDictionaryIndexBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    abstract fun pagingSource(name: String): PagingSource<Int, Index>
    abstract fun toDictionaryWord(index: Index?): DictionaryWord?
    abstract fun download(id: Int, name: String)

    private val inputMethodManager by inject<InputMethodManager>()
    protected var keyword = "%"
    protected val adapter = DictionaryAdapter(::toDictionaryWord, ::download)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView(requireContext())
        setupToolbar()
        binding.keyword.addTextChangedListener {
            keyword = "%${it ?: ""}%"
            adapter.refresh()
        }
        binding.keyword.setOnEditorActionListener { v, actionId, event ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
                else -> false
            }
        }
    }

    private fun setupToolbar() {
        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        toolbar.inflateMenu(R.menu.menu_dictionary_index)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.search_bar -> showSearchBar()
            }
            true
        }
    }

    private fun showSearchBar() {
        if (!binding.keywordLayout.isVisible) {
            val appbar = requireActivity().findViewById<AppBarLayout>(R.id.appbar)
            appbar.setExpanded(false)
        }
        binding.keywordLayout.isVisible = !binding.keywordLayout.isVisible
    }


    private fun setupRecyclerView(context: Context) {
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
        val pager = Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { pagingSource(keyword) }
        )
            .flow
            .cachedIn(lifecycleScope)
        lifecycleScope.launch {
            pager.collectLatest(adapter::submitData)
        }
    }

    class DictionaryAdapter<T : Any>(
        private val toDictionaryWord: (T?) -> DictionaryWord?,
        private val download: (Int, String) -> Unit,
    ) : PagingDataAdapter<T, DictionaryAdapter.WordViewHolder>(DiffCallback<T>()) {
        data class DictionaryWord(val id: Int, val name: String)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
            val binding = ItemDictionaryIndexBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return WordViewHolder(binding)
        }

        override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
            val index = getItem(position)
            val word = toDictionaryWord(index)
            holder.bind(word, download)
        }

        class WordViewHolder(private val binding: ItemDictionaryIndexBinding) : RecyclerView.ViewHolder(binding.root), KoinComponent {

            fun bind(
                word: DictionaryWord?,
                download: (Int, String) -> Unit,
            ) {
                if (word == null) return
                binding.displayName.text = "${word.name}#${word.id}"
                binding.install.setOnClickListener {
                    download(word.id, word.name)
                }
            }

        }

        class DiffCallback<T : Any> : DiffUtil.ItemCallback<T>() {
            override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
                return oldItem == newItem
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
                return oldItem == newItem
            }
        }
    }
}