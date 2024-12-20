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

    private val notificationManager by inject<NotificationManagerCompat>()
    private val inputMethodManager by inject<InputMethodManager>()

    private val requestNotificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isNotificationsEnabled ->
        checkNotificationsEnabled()
    }

    private fun checkNotificationsEnabled() {
        val isNotificationsEnabled = notificationManager.areNotificationsEnabled()
        binding.notificationPermissionWarning.isVisible = !isNotificationsEnabled
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun checkInputMethodSettingsActive() {
        val localImeService = ComponentName(requireContext(), IMEService::class.java)
        val enabledInputMethodList = inputMethodManager.enabledInputMethodList
        val isInputMethodSettingsActive = enabledInputMethodList.any { it.component == localImeService }
        binding.imePermissionWarning.isVisible = !isInputMethodSettingsActive
    }

    private fun requestInputMethodSettingsActive() {
        startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imeRequestPermission.setOnClickListener {
            requestInputMethodSettingsActive()
        }
        binding.notificationRequestPermission.setOnClickListener {
            requestNotificationPermission()
        }
        binding.btnUserDictionary.setOnClickListener {
            findNavController().navigate(R.id.languageFragment)
        }
        binding.btnGboard.setOnClickListener {
            devDialog(requireContext())
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
        checkInputMethodSettingsActive()
        checkNotificationsEnabled()
    }
}
