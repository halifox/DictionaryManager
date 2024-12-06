package com.github.dictionary

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.dictionary.databinding.FragmentEditWordBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.android.ext.android.inject


class EditWordFragment : Fragment() {
    companion object {
        private const val TAG = "EditWordFragment"
        const val TYPE_ADD = 0
        const val TYPE_UPDATE = 1
    }

    private var _binding: FragmentEditWordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentEditWordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private val userDictionaryManager by inject<UserDictionaryManager>()



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val type = requireArguments().getInt("type")
        val word = requireArguments().getString("word")
        val frequency = requireArguments().getInt("frequency", 1)
        val locale = requireArguments().getString("locale")
        val appid = requireArguments().getInt("appid", getUid(requireContext()))
        val shortcut = requireArguments().getString("shortcut")

        binding.word.setText(word)
        binding.frequency.setText(frequency.toString())
        binding.locale.setText(locale)
        binding.appid.setText(appid.toString())
        binding.shortcut.setText(shortcut)

        binding.delete.isVisible = type == TYPE_UPDATE
        binding.delete.setOnClickListener {
            userDictionaryManager.delete(word = word, shortcut = shortcut, frequency = frequency, locale = locale, appid = appid)
            findNavController().navigateUp()
        }

        binding.save.setOnClickListener {
            val newWord = binding.word.text?.toString()
            val newShortcut = binding.shortcut.text?.toString()
            val newFrequency = binding.frequency.text?.toString()?.toIntOrNull()
            val newLocale = binding.locale.text?.toString()
            val newAppid = binding.appid.text?.toString()?.toIntOrNull()

            if (newWord.isNullOrBlank()) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.dialog_add_error_titile)
                    .setMessage(R.string.dialog_add_word_error_message)
                    .setPositiveButton(R.string.dialog_yes) { _, _ -> }
                    .show()
                return@setOnClickListener
            }
            if (newFrequency == null || newFrequency < 1 || newFrequency > 255) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.dialog_add_error_titile)
                    .setMessage(R.string.dialog_add_frequency_error_message)
                    .setPositiveButton(R.string.dialog_yes) { _, _ -> }
                    .show()
                return@setOnClickListener
            }
            when (type) {
                TYPE_ADD -> userDictionaryManager.insert(word = newWord, shortcut = newShortcut, frequency = newFrequency, locale = newLocale, appid = newAppid)
                TYPE_UPDATE -> userDictionaryManager.update(
                    word = word, shortcut = shortcut, frequency = frequency, locale = locale, appid = appid,
                    newWord = newWord, newShortcut = newShortcut, newFrequency = newFrequency, newLocale = newLocale, newAppid = newAppid,
                )
            }
            findNavController().navigateUp()
        }
    }
}
