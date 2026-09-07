package com.oncology.handbook.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileUtils {

    private const val BUFFER_SIZE = 8192

    /** 从 Uri 复制文件到目标目录，返回新文件路径 */
    fun copyFromUri(context: Context, uri: Uri, targetDir: File, prefix: String = "file"): String? {
        return try {
            val displayName = getFileName(context, uri) ?: "${prefix}_${System.currentTimeMillis()}"
            val extension = displayName.substringAfterLast(".", "")
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = if (extension.isNotEmpty()) "${prefix}_${timeStamp}.$extension" else "${prefix}_${timeStamp}"
            val targetFile = File(targetDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output, BUFFER_SIZE)
                }
            }
            targetFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) result = cursor.getString(index)
                }
            }
        }
        if (result == null) {
            result = uri.path?.let { File(it).name }
        }
        return result
    }

    fun copyFile(source: File, dest: File): Boolean {
        return try {
            FileInputStream(source).use { input ->
                FileOutputStream(dest).use { output ->
                    input.copyTo(output, BUFFER_SIZE)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun deleteFile(path: String): Boolean {
        return try {
            File(path).delete()
        } catch (e: Exception) {
            false
        }
    }

    fun getFileSize(path: String): Long = File(path).length()

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
        }
    }
}
