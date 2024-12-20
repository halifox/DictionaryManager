package com.github.dictionary

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


        const val TYPE = "TYPE"
        const val WORD = "WORD"
        const val FREQUENCY = "FREQUENCY"
        const val LOCALE = "LOCALE"
        const val APPID = "APPID"
        const val SHORTCUT = "SHORTCUT"
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
        val _type = requireArguments().getInt(TYPE)
        val _word = requireArguments().getString(WORD)
        val _frequency = requireArguments().getInt(FREQUENCY, 250)
        val _locale = requireArguments().getString(LOCALE)
        val _appid = requireArguments().getInt(APPID, 0)
        val _shortcut = requireArguments().getString(SHORTCUT)

        binding.word.setText(_word)
        binding.frequency.setText(_frequency.toString())
        binding.locale.setText(_locale)
        binding.appid.setText(_appid.toString())
        binding.shortcut.setText(_shortcut)

        binding.delete.isVisible = _type == TYPE_UPDATE
        binding.delete.setOnClickListener {
            userDictionaryManager.delete(word = _word, shortcut = _shortcut, frequency = _frequency, locale = _locale, appid = _appid)
            findNavController().navigateUp()
        }

        binding.save.setOnClickListener {
            val word = binding.word.text?.toString()
            val shortcut = binding.shortcut.text?.toString()
            val frequency = binding.frequency.text?.toString()?.toIntOrNull()
            var locale = binding.locale.text?.toString()
            val appid = binding.appid.text?.toString()?.toIntOrNull()

            if (locale.isNullOrEmpty()) {
                locale = null // 处理local = ""的情况
            }

            if (word.isNullOrBlank()) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.dialog_add_error_titile)
                    .setMessage(R.string.dialog_add_word_error_message)
                    .setPositiveButton(R.string.dialog_yes) { _, _ -> }
                    .show()
                return@setOnClickListener
            }
            if (frequency == null || frequency < 1 || frequency > 255) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.dialog_add_error_titile)
                    .setMessage(R.string.dialog_add_frequency_error_message)
                    .setPositiveButton(R.string.dialog_yes) { _, _ -> }
                    .show()
                return@setOnClickListener
            }
            when (_type) {
                TYPE_ADD -> userDictionaryManager.insert(
                    word = word,
                    shortcut = shortcut,
                    frequency = frequency,
                    locale = locale,
                    appid = appid,
                )

                TYPE_UPDATE -> userDictionaryManager.update(
                    _word = _word,
                    _shortcut = _shortcut,
                    _frequency = _frequency,
                    _locale = _locale,
                    _appid = _appid,
                    word = word,
                    shortcut = shortcut,
                    frequency = frequency,
                    locale = locale,
                    appid = appid,
                )
            }
            findNavController().navigateUp()
        }
    }
}
