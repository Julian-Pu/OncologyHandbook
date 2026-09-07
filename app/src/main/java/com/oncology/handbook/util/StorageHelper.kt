package com.oncology.handbook.util

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import java.io.File

object StorageHelper {

    private const val PREFS_NAME = "storage_prefs"
    private const val KEY_CUSTOM_PATH = "custom_storage_path"
    const val DEFAULT_FOLDER_NAME = "肿瘤内科医生值班手册a"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /** 获取根存储目录：用户自定义路径优先，否则默认手机根目录下的文件夹 */
    fun getRootDir(): File {
        val custom = prefs.getString(KEY_CUSTOM_PATH, null)
        return if (!custom.isNullOrEmpty()) {
            File(custom)
        } else {
            File(Environment.getExternalStorageDirectory(), DEFAULT_FOLDER_NAME)
        }
    }

    fun setCustomPath(path: String) {
        prefs.edit().putString(KEY_CUSTOM_PATH, path).apply()
    }

    fun resetToDefault() {
        prefs.edit().remove(KEY_CUSTOM_PATH).apply()
    }

    fun getImagesDir(): File = File(getRootDir(), "images").apply { mkdirs() }
    fun getVideosDir(): File = File(getRootDir(), "videos").apply { mkdirs() }
    fun getExportsDir(): File = File(getRootDir(), "exports").apply { mkdirs() }

    fun ensureDirs() {
        getRootDir().mkdirs()
        getImagesDir()
        getVideosDir()
        getExportsDir()
    }

    fun isExternalStorageWritable(): Boolean =
        Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
}
