package com.github.dictionary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.dictionary.databinding.FragmentLanguageBinding
import com.github.dictionary.databinding.ItemLanguageBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.util.Locale

class LanguageFragment : Fragment() {
    companion object {
        private const val TAG = "LanguageFragment"
    }

    private var _binding: FragmentLanguageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentLanguageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val adapter = Adapter()
    private val userDictionaryManager by inject<UserDictionaryManager>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        setupToolbar()
        initLocales()
    }

    private fun initLocales() {
        lifecycleScope.launch(Dispatchers.IO) {
            val locales = buildList<Locale> {
                add(Locale.ROOT)
                val localLocales = buildSet<Locale> {
                    val localLocales = resources.configuration.getLocales()
                    repeat(localLocales.size()) {
                        val locale = localLocales.get(it)
                        add(Locale(locale.language, locale.country))
                    }
                }
                addAll(localLocales.toList())
            }
            withContext(Dispatchers.Main) {
                adapter.submitList(locales)
            }
        }
    }

    private fun setupToolbar() {
        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        toolbar.inflateMenu(R.menu.menu_language)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.item_clean -> cleanDictionary()
            }
            true
        }
    }

    private fun cleanDictionary() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_clean_all_title)
            .setMessage(R.string.dialog_clean_all_message)
            .setPositiveButton(R.string.dialog_clean_all_yes) { _, _ ->
                userDictionaryManager.delete()
            }
            .setNegativeButton(R.string.dialog_clean_all_no) { _, _ -> }
            .show()
    }


    class Callback : DiffUtil.ItemCallback<Locale>() {
        override fun areItemsTheSame(oldItem: Locale, newItem: Locale): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Locale, newItem: Locale): Boolean {
            return oldItem == newItem
        }
    }

    inner class Adapter : ListAdapter<Locale, Adapter.WordViewHolder>(Callback()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
            val binding = ItemLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return WordViewHolder(binding)
        }

        override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        inner class WordViewHolder(private val binding: ItemLanguageBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(locale: Locale) {
                var displayName = locale.getDisplayName()
                if (locale == Locale.ROOT) {
                    displayName = getString(R.string.td_all_languages)
                }
                val identifier = locale.toString()
                val languageTag = locale.toLanguageTag()
                binding.displayName.text = displayName
                binding.identifier.text = identifier
                binding.root.setOnClickListener {
                    findNavController().navigate(R.id.DictionaryFragment, bundleOf(DictionaryFragment.LANGUAGE_TAG to languageTag))
                }
            }
        }
    }
}