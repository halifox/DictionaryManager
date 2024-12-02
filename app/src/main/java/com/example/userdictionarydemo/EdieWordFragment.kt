package com.example.userdictionarydemo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.userdictionarydemo.databinding.FragmentEditWordBinding

class EdieWordFragment : Fragment() {
    companion object {
        private const val TAG = "DictionaryFragment"
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
}
