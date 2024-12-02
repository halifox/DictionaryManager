package com.example.userdictionarydemo

import android.app.ProgressDialog
import android.content.Intent
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
import com.example.userdictionarydemo.databinding.FragmentDictionaryBinding
import com.example.userdictionarydemo.databinding.ItemWordBinding
import com.google.android.material.appbar.CollapsingToolbarLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale


class DictionaryFragment : Fragment() {
    companion object {
        private const val TAG = "DictionaryFragment"
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


    private val locale by lazy { Locale.forLanguageTag(requireArguments().getString("languageTag", Locale.ROOT.toLanguageTag())) }

    private val sogou = Sogou()
    private val userDictionaryManager by lazy { UserDictionaryManager(requireContext().contentResolver) }

    private val importDictionaryFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.data?.let { uri ->
                lifecycleScope.launch(Dispatchers.IO) {
                    var fileName = ""
                    val cursor = requireContext().contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                    if (cursor != null && cursor.moveToFirst()) {
                        fileName = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                        cursor.close()
                    }
                    if (fileName.endsWith(".scel")) {
                        var parseProgressDialog = withContext(Dispatchers.Main) {
                            ProgressDialog(requireContext()).apply {
                                setCancelable(false)
                                setTitle("解析文件中...")
                                setProgressStyle(ProgressDialog.STYLE_SPINNER)
                                show()
                            }
                        }

                        val file = ReflectUtils.reflect(UriUtils::class.java).method("copyUri2Cache", uri).get<File>()


                        Log.d("TAG", ":${file.absolutePath} ")

                        val entryList = sogou.extractEntriesFromScelFile(file)
                        val progressDialog = withContext(Dispatchers.Main) {
                            parseProgressDialog.dismiss()
                            ProgressDialog(requireContext()).apply {
                                setTitle("添加到词库...")
                                setCancelable(false)
                                setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                                progress = 0
                                max = entryList.size

                                show()
                            }
                        }


                        entryList.forEachIndexed { index, wordEntry ->
                            userDictionaryManager.insert(wordEntry.word, wordEntry.pinyin, locale = locale.toString())
                            withContext(Dispatchers.Main) {
                                progressDialog.progress = index
                            }
                        }
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "添加完成", Toast.LENGTH_SHORT).show()
                            progressDialog.dismiss()
                            adapter.refresh()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "请选择 .scel 文件", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            }
        }

    private fun importDictionaryFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "*/*")
        }
        importDictionaryFileLauncher.launch(intent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(locale)
        setupRecyclerView(locale)
    }


    private fun setupToolbar(locale: Locale) {
        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        val collapsingToolbarLayout = requireActivity().findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar_layout)
        var displayName = locale.getDisplayName()
        if (locale == Locale.ROOT) {
            displayName = "所有语言"
        }
        collapsingToolbarLayout.title = displayName
        toolbar.inflateMenu(R.menu.menu_user_dictionary)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.item_new -> {
                    findNavController().navigate(R.id.edieWordFragment)
                }

                R.id.item_clean -> {
                    userDictionaryManager.delete(locale = locale.toString())
                    adapter.refresh()
                }

                R.id.item_import -> {
                    importDictionaryFile()
                }

                R.id.item_export -> Unit
            }
            true
        }
    }

    val adapter = DictionaryAdapter()
    fun setupRecyclerView(locale: Locale) {
        val context = requireContext()
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
        val pager = Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = {
                DictionaryPagingSource(
                    userDictionaryManager,
                    locale,
                )
            }
        ).flow.cachedIn(lifecycleScope)
        lifecycleScope.launch {
            pager.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }

    class DictionaryAdapter : PagingDataAdapter<Word, DictionaryAdapter.WordViewHolder>(WordDiffCallback()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
            val binding = ItemWordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return WordViewHolder(binding)
        }

        override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        inner class WordViewHolder(private val binding: ItemWordBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(word: Word?) {
                if (word == null) return
                binding.word.text = "${word.word}"
                binding.shortcut.text = "${word.shortcut}"
            }
        }

        class WordDiffCallback : DiffUtil.ItemCallback<Word>() {
            override fun areItemsTheSame(oldItem: Word, newItem: Word): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Word, newItem: Word): Boolean {
                return oldItem == newItem
            }
        }
    }


    class DictionaryPagingSource(
        private val userDictionaryManager: UserDictionaryManager,
        private val locale: Locale,
    ) : PagingSource<Int, Word>() {
        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Word> {
            return try {
                val page = params.key ?: 0
                val pageSize = params.loadSize
                val offset = page * pageSize
                val words = userDictionaryManager.query(
                    selection = "${UserDictionary.Words.LOCALE} = ?", selectionArgs = arrayOf(locale.toString()),
                    sortOrder = "${UserDictionary.Words.WORD} LIMIT $pageSize OFFSET $offset"
                )
                val prevKey = if (page == 0) null else page - 1
                val nextKey = if (words.size < pageSize) null else page + 1
                LoadResult.Page(data = words, prevKey = prevKey, nextKey = nextKey)
            } catch (e: Exception) {
                LoadResult.Error(e)
            }
        }

        override fun getRefreshKey(state: PagingState<Int, Word>): Int? {
            return null
        }
    }

}
