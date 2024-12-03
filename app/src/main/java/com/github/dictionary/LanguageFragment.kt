package com.github.dictionary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class LanguageFragment : Fragment() {
    companion object {
        private const val TAG = "UserDictionaryLangFragment"
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        binding.toolbar.setupWithNavController(findNavController())
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        initLocales()
    }

    private fun initLocales() {
        lifecycleScope.launch(Dispatchers.IO) {
            val locales = buildList<Locale> {
                add(Locale.ROOT)
                val localLocales = resources.configuration.getLocales()
                repeat(localLocales.size()) {
                    val locale = localLocales.get(it)
                    add(locale)
                }
            }
            withContext(Dispatchers.Main) {
                adapter.submitList(locales)
            }
        }
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
                    displayName = "所有语言"
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