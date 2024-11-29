package com.example.userdictionarydemo

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity.INPUT_METHOD_SERVICE
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.userdictionarydemo.databinding.FragmentMainBinding


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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 权限检查 授权按钮 跳转授权
        // 本地词库管理 从文件添加标准格式 从文件添加搜狗格式 清空词库 移除某个搜狗词库 查看词库内容（多语言） 设置词库优先级 单个词条的CURD
        // 从搜狗下载词库
        //


        binding.btnRequestPermission.setOnClickListener {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }
        binding.btnUd.setOnClickListener {
            findNavController().navigate(R.id.secondFragment)
        }
    }

    override fun onStart() {
        super.onStart()
        val imeService = ComponentName(requireContext(), IMEService::class.java)
        val inputMethodManager = requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val enabledInputMethodList = inputMethodManager.enabledInputMethodList
        val isKeyboardActive = enabledInputMethodList.any { it.component == imeService }
        binding.permissionWarning.isVisible = !isKeyboardActive
    }
}
