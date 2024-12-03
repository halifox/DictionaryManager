package com.github.dictionary

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.UserDictionary
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingDataAdapter
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ReflectUtils
import com.blankj.utilcode.util.UriUtils
import com.github.dictionary.databinding.FragmentDictionaryBinding
import com.github.dictionary.databinding.ItemWordBinding
import com.google.android.material.appbar.CollapsingToolbarLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.io.File
import java.util.Locale


class DictionaryFragment : Fragment() {
    companion object {
        private const val TAG = "DictionaryFragment"
        const val LANGUAGE_TAG = "LANGUAGE_TAG"
    }

    private val locale by lazy { Locale.forLanguageTag(requireArguments().getString(LANGUAGE_TAG, Locale.ROOT.toLanguageTag())) }

    private val userDictionaryManager by inject<UserDictionaryManager>()
    private val adapter = DictionaryAdapter()

    private val importer by inject<FileImporter>()

    private var _binding: FragmentDictionaryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentDictionaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(locale)
        setupRecyclerView(requireContext(), locale)
    }

    private val importDictionaryFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val uri = it.data?.data
            if (uri != null && locale != null) {
                importer.addImportTask(FileImporter.Task(uri, locale))
            }
        }

    private fun importDictionaryFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "*/*")
        }
        importDictionaryFileLauncher.launch(intent)
    }

    private fun exportDictionaryFile() {

    }


    private fun setupToolbar(locale: Locale) {
        val collapsingToolbarLayout = requireActivity().findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar_layout)
        var displayName = locale.getDisplayName()
        if (locale == Locale.ROOT) {
            displayName = "所有语言"
        }
        collapsingToolbarLayout.title = displayName

        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        toolbar.inflateMenu(R.menu.menu_dictionary)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.item_add -> addDictionary()
                R.id.item_clean -> cleanDictionary()
                R.id.item_import -> importDictionaryFile()
                R.id.item_export -> exportDictionaryFile()
            }
            true
        }
    }

    private fun addDictionary() {
        findNavController().navigate(R.id.edieWordFragment)
    }

    private fun cleanDictionary() {
        userDictionaryManager.delete(locale = locale.toString())
        adapter.refresh()
    }

    private fun setupRecyclerView(context: Context, locale: Locale) {
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
        val pager = Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { DictionaryPagingSource(userDictionaryManager, locale) }
        )
            .flow
            .cachedIn(lifecycleScope)
        lifecycleScope.launch {
            pager.collectLatest(adapter::submitData)
        }
    }

    class DictionaryAdapter : PagingDataAdapter<UserDictionaryManager.Word, DictionaryAdapter.WordViewHolder>(WordDiffCallback()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
            val binding = ItemWordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return WordViewHolder(binding)
        }

        override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        class WordViewHolder(private val binding: ItemWordBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(word:UserDictionaryManager.Word?) {
                if (word == null) return
                binding.word.text = "${word.word}"
                binding.shortcut.text = "${word.shortcut}"
            }
        }

        class WordDiffCallback : DiffUtil.ItemCallback<UserDictionaryManager.Word>() {
            override fun areItemsTheSame(oldItem:UserDictionaryManager.Word, newItem:UserDictionaryManager.Word): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem:UserDictionaryManager.Word, newItem:UserDictionaryManager.Word): Boolean {
                return oldItem == newItem
            }
        }
    }


    class DictionaryPagingSource(
        private val userDictionaryManager: UserDictionaryManager,
        private val locale: Locale,
    ) : PagingSource<Int,UserDictionaryManager.Word>() {
        override suspend fun load(params: LoadParams<Int>): LoadResult<Int,UserDictionaryManager.Word> {
            return try {
                val page = params.key ?: 0
                val pageSize = params.loadSize
                val offset = page * pageSize
                Log.d(TAG, "load: ${locale}")
                Log.d(TAG, "load: ${locale == Locale.ROOT}")
                val words = if (locale == Locale.ROOT) {
                    userDictionaryManager.query(
                        selection = "${UserDictionary.Words.LOCALE} is NULL",
                        sortOrder = "${UserDictionary.Words.WORD} LIMIT $pageSize OFFSET $offset"
                    )
                } else {
                    userDictionaryManager.query(
                        selection = "${UserDictionary.Words.LOCALE} = ?",
                        selectionArgs = arrayOf(locale.toString()),
                        sortOrder = "${UserDictionary.Words.WORD} LIMIT $pageSize OFFSET $offset"
                    )
                }
                Log.d(TAG, "words: ${words}")

                val prevKey = if (page == 0) null else page - 1
                val nextKey = if (words.size < pageSize) null else page + 1
                LoadResult.Page(data = words, prevKey = prevKey, nextKey = nextKey)
            } catch (e: Exception) {
                LoadResult.Error(e)
            }
        }

        override fun getRefreshKey(state: PagingState<Int,UserDictionaryManager.Word>): Int? {
            return null
        }
    }

}
