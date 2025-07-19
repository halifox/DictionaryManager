package com.github.dictionary

import android.inputmethodservice.InputMethodService
import android.view.View
import androidx.compose.material3.Text
import androidx.compose.ui.platform.ComposeView


// 4. 实现输入法服务
// 继承 InputMethodService 并重写相关方法。
class IMEService : InputMethodService() {
    override fun onCreateInputView(): View {
        // 返回自定义输入法视图
        return ComposeView(this).apply {
            setContent {
                Text("IMEService")
            }
        }
    }
}