package com.github.dictionary

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.provider.UserDictionary
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
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
import com.github.dictionary.databinding.FragmentDictionaryBinding
import com.github.dictionary.databinding.ItemWordBinding
import com.github.dictionary.importer.DictionaryImporter
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.lang.ref.WeakReference
import java.util.Locale


class DictionaryFragment : Fragment() {
    companion object {
        private const val TAG = "DictionaryFragment"
        const val LOCALE = "LOCALE"
    }

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

    private val locale by lazy { requireArguments().getSerializable(LOCALE) as Locale }

    private val importer by inject<DictionaryImporter>()
    private val userDictionaryManager by inject<UserDictionaryManager>()
    private val inputMethodManager by inject<InputMethodManager>()
    private val adapter = DictionaryAdapter(::updateDictionary)
    private val adapterWeakReference = WeakReference(adapter)

    private var keyword = ""

    private val importDictionaryFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        it.data?.data?.let { uri ->
            val task = DictionaryImporter.Task(uri, locale) {
                adapterWeakReference.get()?.refresh()
            }
            importer.addImportTask(task)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(locale)
        setupRecyclerView(requireContext(), locale)
        binding.keyword.addTextChangedListener {
            keyword = it?.toString().orEmpty()
            adapter.refresh()
        }
        binding.keyword.setOnEditorActionListener { v, actionId, event ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
                else -> false
            }
        }
    }


    private fun setupToolbar(locale: Locale) {
        val collapsingToolbarLayout = requireActivity().findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar_layout)
        collapsingToolbarLayout.title = locale.displayName.ifEmpty { requireContext().getString(R.string.td_all_languages) }

        val toolbar = requireActivity().findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.inflateMenu(R.menu.menu_dictionary)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.item_add -> addDictionary()
                R.id.item_clean -> cleanDictionary()
                R.id.item_import -> importDictionaryFile()
                R.id.item_export -> exportDictionaryFile()
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

    private fun addDictionary() {
        val bundle = Bundle().apply {
            putInt(EditWordFragment.TYPE, EditWordFragment.TYPE_ADD)
            putString(EditWordFragment.LOCALE, locale.toString().ifEmpty { null })
        }
        findNavController().navigate(R.id.edieWordFragment, bundle)
    }

    private fun updateDictionary(word: UserDictionaryManager.Word) {
        val bundle = Bundle().apply {
            putInt(EditWordFragment.TYPE, EditWordFragment.TYPE_UPDATE)
            putString(EditWordFragment.WORD, word.word)
            putInt(EditWordFragment.FREQUENCY, word.frequency ?: 0)
            putString(EditWordFragment.LOCALE, word.locale?.ifEmpty { null })
            putInt(EditWordFragment.APPID, word.appid ?: 0)
            putString(EditWordFragment.SHORTCUT, word.shortcut)
        }
        findNavController().navigate(R.id.edieWordFragment, bundle)
    }

    private fun cleanDictionary() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_clean_all_title)
            .setMessage(R.string.dialog_clean_all_message)
            .setPositiveButton(R.string.dialog_clean_all_yes) { _, _ ->
                userDictionaryManager.delete(locale = locale.toString())
                adapter.refresh()
            }
            .setNegativeButton(R.string.dialog_clean_all_no) { _, _ -> }
            .show()
    }

    private fun importDictionaryFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "*/*")
        }
        importDictionaryFileLauncher.launch(intent)
    }

    private fun exportDictionaryFile() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_dev_titile)
            .setMessage(R.string.dialog_dev_message)
            .setPositiveButton(R.string.dialog_yes) { _, _ -> }
            .show()
    }

    private fun setupRecyclerView(context: Context, locale: Locale) {
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
        val pager = Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { DictionaryPagingSource(userDictionaryManager, locale, keyword) }
        )
            .flow
            .cachedIn(lifecycleScope)
        lifecycleScope.launch {
            pager.collectLatest(adapter::submitData)
        }
    }

    class DictionaryAdapter(private val updateDictionary: (UserDictionaryManager.Word) -> Unit) : PagingDataAdapter<UserDictionaryManager.Word, DictionaryAdapter.WordViewHolder>(WordDiffCallback()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
            val binding = ItemWordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return WordViewHolder(binding)
        }

        override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
            holder.bind(getItem(position), updateDictionary)
        }

        class WordViewHolder(private val binding: ItemWordBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(word: UserDictionaryManager.Word?, updateDictionary: (UserDictionaryManager.Word) -> Unit) {
                if (word == null) return
                binding.word.text = "${word.word}"
                binding.shortcut.text = "${word.shortcut}"
                binding.root.setOnClickListener {
                    updateDictionary(word)
                }
            }
        }

        class WordDiffCallback : DiffUtil.ItemCallback<UserDictionaryManager.Word>() {
            override fun areItemsTheSame(oldItem: UserDictionaryManager.Word, newItem: UserDictionaryManager.Word): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: UserDictionaryManager.Word, newItem: UserDictionaryManager.Word): Boolean {
                return oldItem == newItem
            }
        }
    }


    class DictionaryPagingSource(
        private val userDictionaryManager: UserDictionaryManager,
        private val locale: Locale,
        private val keyword: String,
    ) : PagingSource<Int, UserDictionaryManager.Word>() {
        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, UserDictionaryManager.Word> {
            return try {
                val page = params.key ?: 0
                val pageSize = params.loadSize
                val offset = page * pageSize
                val words = if (locale == Locale.ROOT) {
                    userDictionaryManager.query(
                        selection = "${UserDictionary.Words.WORD} LIKE ? AND ${UserDictionary.Words.LOCALE} is NULL",
                        selectionArgs = arrayOf("%${keyword}%"),
                        sortOrder = "${UserDictionary.Words.WORD} LIMIT $pageSize OFFSET $offset"
                    )
                } else {
                    userDictionaryManager.query(
                        selection = "${UserDictionary.Words.WORD} LIKE ? AND ${UserDictionary.Words.LOCALE} = ?",
                        selectionArgs = arrayOf("%${keyword}%", locale.toString()),
                        sortOrder = "${UserDictionary.Words.WORD} LIMIT $pageSize OFFSET $offset"
                    )
                }
                val prevKey = if (page == 0) null else page - 1
                val nextKey = if (words.size < pageSize) null else page + 1
                LoadResult.Page(data = words, prevKey = prevKey, nextKey = nextKey)
            } catch (e: Exception) {
                LoadResult.Error(e)
            }
        }

        override fun getRefreshKey(state: PagingState<Int, UserDictionaryManager.Word>): Int? {
            return null
        }
    }

}
