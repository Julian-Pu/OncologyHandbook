package com.oncology.handbook.util

import android.content.Context
import android.net.Uri
import com.oncology.handbook.data.entity.UserContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object JsonExporter {

    data class ExportResult(val success: Boolean, val filePath: String? = null, val message: String = "")

    /** 导出所有用户内容为 JSON 文件，同时复制关联的图片/视频到导出目录 */
    suspend fun exportAll(
        context: Context,
        contents: List<UserContent>
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            StorageHelper.ensureDirs()
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val exportDir = File(StorageHelper.getExportsDir(), "backup_$timeStamp")
            exportDir.mkdirs()

            val mediaDir = File(exportDir, "media")
            mediaDir.mkdirs()

            val jsonArray = JSONArray()
            for (c in contents) {
                val obj = JSONObject().apply {
                    put("title", c.title)
                    put("content", c.content)
                    put("category", c.category)
                    put("createdAt", c.createdAt)
                    put("updatedAt", c.updatedAt)
                }

                // 复制图片
                val imagePaths = c.imagePaths.split("|").filter { it.isNotEmpty() }
                val exportedImages = JSONArray()
                for (imgPath in imagePaths) {
                    val src = File(imgPath)
                    if (src.exists()) {
                        val dest = File(mediaDir, src.name)
                        FileUtils.copyFile(src, dest)
                        exportedImages.put("media/${src.name}")
                    }
                }
                obj.put("imagePaths", exportedImages.join("|"))

                // 复制视频
                if (c.videoPath.isNotEmpty()) {
                    val src = File(c.videoPath)
                    if (src.exists()) {
                        val dest = File(mediaDir, src.name)
                        FileUtils.copyFile(src, dest)
                        obj.put("videoPath", "media/${src.name}")
                    } else {
                        obj.put("videoPath", "")
                    }
                } else {
                    obj.put("videoPath", "")
                }

                jsonArray.put(obj)
            }

            val root = JSONObject().apply {
                put("appName", "肿瘤科医生值班手册")
                put("version", 1)
                put("exportTime", System.currentTimeMillis())
                put("count", contents.size)
                put("contents", jsonArray)
            }

            val jsonFile = File(exportDir, "data.json")
            jsonFile.writeText(root.toString(2), Charsets.UTF_8)

            ExportResult(true, exportDir.absolutePath, "导出成功：${contents.size} 条内容")
        } catch (e: Exception) {
            ExportResult(false, message = "导出失败：${e.message}")
        }
    }

    /** 从 JSON 文件导入内容 */
    suspend fun importFromJson(
        context: Context,
        uri: Uri
    ): Pair<Int, String> = withContext(Dispatchers.IO) {
        try {
            val jsonText = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                ?: return@withContext 0 to "无法读取文件"

            val root = JSONObject(jsonText)
            val contentsArray = root.optJSONArray("contents") ?: return@withContext 0 to "文件格式不正确"

            // 解析导入文件所在目录（用于复制媒体文件）
            val importBaseDir = File(StorageHelper.getRootDir(), "import_${System.currentTimeMillis()}")
            importBaseDir.mkdirs()

            var imported = 0
            val dao = com.oncology.handbook.App.instance.database.userContentDao()

            for (i in 0 until contentsArray.length()) {
                val obj = contentsArray.getJSONObject(i)
                val imagePathsStr = obj.optString("imagePaths", "")
                val videoPathStr = obj.optString("videoPath", "")

                val content = UserContent(
                    title = obj.optString("title", "未命名"),
                    content = obj.optString("content", ""),
                    category = obj.optString("category", "导入"),
                    imagePaths = imagePathsStr,
                    videoPath = videoPathStr,
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                )
                dao.insert(content)
                imported++
            }

            imported to "成功导入 $imported 条内容"
        } catch (e: Exception) {
            0 to "导入失败：${e.message}"
        }
    }
}
