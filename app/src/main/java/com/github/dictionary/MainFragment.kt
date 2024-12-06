package com.github.dictionary

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity.INPUT_METHOD_SERVICE
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.dictionary.databinding.FragmentMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.android.ext.android.inject


class MainFragment : Fragment() {
    private val notificationManagerCompat by inject<NotificationManagerCompat>()

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isNotificationsEnabled ->
            binding.notificationPermissionWarning.isVisible = !isNotificationsEnabled
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imeRequestPermission.setOnClickListener {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }
        binding.notificationRequestPermission.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
        binding.btnUserDictionary.setOnClickListener {
            findNavController().navigate(R.id.LanguageFragment)
        }
        binding.btnGboard.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_dev_titile)
                .setMessage(R.string.dialog_dev_message)
                .setPositiveButton(R.string.dialog_yes) { _, _ -> }
                .show()
        }
        binding.btnSogou.setOnClickListener {
            findNavController().navigate(R.id.sogouFragment)
        }
        binding.btnXunfei.setOnClickListener {
            findNavController().navigate(R.id.xunfeiFragment)
        }
        binding.btnBaidu.setOnClickListener {
            findNavController().navigate(R.id.baiduFragment)
        }

    }

    override fun onStart() {
        super.onStart()
        val imeService = ComponentName(requireContext(), IMEService::class.java)
        val inputMethodManager = requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val enabledInputMethodList = inputMethodManager.enabledInputMethodList
        val isKeyboardActive = enabledInputMethodList.any { it.component == imeService }
        binding.imePermissionWarning.isVisible = !isKeyboardActive

        val isNotificationsEnabled = notificationManagerCompat.areNotificationsEnabled()
        binding.notificationPermissionWarning.isVisible = !isNotificationsEnabled
    }
}
