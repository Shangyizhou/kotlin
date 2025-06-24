package com.example.kotlin.util

import java.io.File
import java.io.IOException
import kotlin.concurrent.Volatile

class FileUtils private constructor() {

    companion object {
        // 单例模式
        @Volatile
        private var instance: FileUtils? = null

        fun getInstance(): FileUtils {
            return instance ?: synchronized(this) {
                instance ?: FileUtils().also { instance = it }
            }
        }
    }

    // 检查文件是否存在
    fun isFileExists(filePath: String): Boolean {
        return File(filePath).exists()
    }

    // 读取文本文件内容
    fun readTextFile(filePath: String): String? {
        return try {
            File(filePath).readText()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    // 写入文本文件（覆盖模式）
    fun writeTextFile(filePath: String, content: String, append: Boolean = false): Boolean {
        return try {
            File(filePath).let { file ->
                if (!file.parentFile.exists()) {
                    file.parentFile.mkdirs() // 自动创建父目录
                }
                file.writeText(content, Charsets.UTF_8)
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    // 追加文本到文件
    fun appendTextFile(filePath: String, content: String): Boolean {
        return writeTextFile(filePath, content, append = true)
    }

    // 读取二进制文件（返回 ByteArray）
    fun readBinaryFile(filePath: String): ByteArray? {
        return try {
            File(filePath).readBytes()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    // 写入二进制文件
    fun writeBinaryFile(filePath: String, bytes: ByteArray): Boolean {
        return try {
            File(filePath).let { file ->
                if (!file.parentFile.exists()) {
                    file.parentFile.mkdirs()
                }
                file.writeBytes(bytes)
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    // 删除文件
    fun deleteFile(filePath: String): Boolean {
        return try {
            File(filePath).delete()
        } catch (e: SecurityException) {
            e.printStackTrace()
            false
        }
    }
}