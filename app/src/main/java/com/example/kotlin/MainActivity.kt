package com.example.kotlin

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.kotlin.navigation.AppNavigation
import com.example.kotlin.ui.theme.KotlinTheme
import com.example.kotlin.util.FileUtils

class MainActivity : ComponentActivity() {
    private val TAG: String = this::class.simpleName ?: "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KotlinTheme {
                AppNavigation()
            }
        }
    }

    private val fileUtils = FileUtils.getInstance()
    fun testFileUtils() {
        // 写入文件
        val success = fileUtils.writeTextFile("/sdcard/Download/test.txt", "Hello, Kotlin!")

        // 读取文件
        val content = fileUtils.readTextFile("/sdcard/Download/test.txt")
        println(content) // 输出: Hello, Kotlin!

        // 追加内容
        fileUtils.appendTextFile("/sdcard/Download/test.txt", "\nNew Line")

        val filePath: String = "/sdcard/Download/test.txt"
        val exists = fileUtils.isFileExists(filePath)
        if (exists) {
            fileUtils.deleteFile(filePath)
            Log.i(TAG, "testFileUtils: ")
        }
    }
}