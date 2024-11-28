package com.example.userdictionarydemo

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button


// 4. 实现输入法服务
// 继承 InputMethodService 并重写相关方法。
class MyInputMethodService : InputMethodService() {
    override fun onCreateInputView(): View {
        // 返回自定义输入法视图
        return layoutInflater.inflate(R.layout.input_view, null)
    }
}