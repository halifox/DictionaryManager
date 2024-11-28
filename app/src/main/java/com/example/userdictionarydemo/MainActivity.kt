package com.example.userdictionarydemo

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.provider.UserDictionary
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        kotlin.run {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            val enabledInputMethodList = imm.enabledInputMethodList
            enabledInputMethodList.forEach {
                Log.d("TAG", "enabledInputMethodList:${it.id} ")
            }
            val isKeyboardActive = enabledInputMethodList.any {
                it.id == "com.example.userdictionarydemo/.MyInputMethodService"
            }
            Log.d("TAG", "isKeyboardActive:${isKeyboardActive} ")
            if (!isKeyboardActive) {
                val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                startActivity(intent)
            }
        }

        run {
            val values = ContentValues()
            values.put(UserDictionary.Words.WORD, "hello") // 单词
            values.put(UserDictionary.Words.FREQUENCY, 100) // 频率
            values.put(UserDictionary.Words.LOCALE, "en_US") // 语言代码
            contentResolver.insert(UserDictionary.Words.CONTENT_URI, values)
        }



        run {
            val cursor = contentResolver.query(
                UserDictionary.Words.CONTENT_URI,  // 查询 URI
                arrayOf(UserDictionary.Words.WORD, UserDictionary.Words.FREQUENCY),  // 查询字段
                null,  // 查询条件
                null,  // 查询参数
                null // 排序
            )
            if (cursor != null) {
                Log.d("TAG", "cursor:${cursor.count} ")

                while (cursor.moveToNext()) {
                    val word =
                        cursor.getString(cursor.getColumnIndexOrThrow(UserDictionary.Words.WORD))
                    val frequency =
                        cursor.getInt(cursor.getColumnIndexOrThrow(UserDictionary.Words.FREQUENCY))
                    Log.d("TAG", "Word: $word, Frequency: $frequency")
                }
                cursor.close()
            }
        }

        run {
            val values = ContentValues()
            values.put(UserDictionary.Words.FREQUENCY, 200) // 修改频率
            contentResolver.update(
                UserDictionary.Words.CONTENT_URI,  // 更新 URI
                values,
                UserDictionary.Words.WORD + "=?",  // 查询条件
                arrayOf("hello") // 条件参数
            )
        }

        run {
            contentResolver.delete(
                UserDictionary.Words.CONTENT_URI,   // 删除 URI
                UserDictionary.Words.WORD + "=?",   // 查询条件
                arrayOf("hello")               // 条件参数
            );
        }

    }
}