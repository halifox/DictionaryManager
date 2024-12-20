package com.github.dictionary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.dictionary.databinding.FragmentEditWordBinding
import com.google.android.material.appbar.MaterialToolbar
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

    private val _type by lazy { requireArguments().getInt(TYPE) }
    private val _word by lazy { requireArguments().getString(WORD) }
    private val _frequency by lazy { requireArguments().getInt(FREQUENCY, 250) }
    private val _locale by lazy { requireArguments().getString(LOCALE) }
    private val _appid by lazy { requireArguments().getInt(APPID, 0) }
    private val _shortcut by lazy { requireArguments().getString(SHORTCUT) }


    private val userDictionaryManager by inject<UserDictionaryManager>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fillText()
        setupToolbar()
    }

    private fun fillText() {
        binding.word.setText(_word)
        binding.frequency.setText(_frequency.toString())
        binding.locale.setText(_locale.toString())
        binding.appid.setText(_appid.toString())
        binding.shortcut.setText(_shortcut)
    }


    private fun setupToolbar() {
        val toolbar = requireActivity().findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.inflateMenu(R.menu.menu_edit_word)
        val add = toolbar.menu.findItem(R.id.add)
        val update = toolbar.menu.findItem(R.id.update)
        val delete = toolbar.menu.findItem(R.id.delete)
        add.isVisible = _type == TYPE_ADD
        update.isVisible = _type == TYPE_UPDATE
        delete.isVisible = _type == TYPE_UPDATE
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.add -> add()
                R.id.update -> update()
                R.id.delete -> delete()
            }
            true
        }
    }


    private fun check(callback: (word: String, shortcut: String?, frequency: Int?, locale: String?, appid: Int?) -> Unit) {
        val word = binding.word.text?.toString()
        val shortcut = binding.shortcut.text?.toString()
        val frequency = binding.frequency.text?.toString()?.toIntOrNull()
        var locale = binding.locale.text?.toString()
        val appid = binding.appid.text?.toString()?.toIntOrNull()

        if (word.isNullOrBlank()) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_add_error_titile)
                .setMessage(R.string.dialog_add_word_error_message)
                .setPositiveButton(R.string.dialog_yes) { _, _ -> }
                .show()
            return
        }
        if (frequency == null || frequency < 1 || frequency > 255) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_add_error_titile)
                .setMessage(R.string.dialog_add_frequency_error_message)
                .setPositiveButton(R.string.dialog_yes) { _, _ -> }
                .show()
            return
        }

        callback(word, shortcut, frequency, locale, appid)
    }

    private fun add() {
        check { word, shortcut, frequency, locale, appid ->
            userDictionaryManager.insert(
                word = word,
                shortcut = shortcut,
                frequency = frequency,
                locale = locale,
                appid = appid,
            )
            findNavController().navigateUp()
        }
    }

    private fun update() {
        check { word, shortcut, frequency, locale, appid ->
            userDictionaryManager.update(
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
            findNavController().navigateUp()
        }
    }

    private fun delete() {
        userDictionaryManager.delete(word = _word, shortcut = _shortcut, frequency = _frequency, locale = _locale, appid = _appid)
        findNavController().navigateUp()
    }
}
