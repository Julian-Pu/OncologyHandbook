package com.oncology.handbook.util

import android.content.Context
import android.net.Uri
import com.oncology.handbook.App
import com.oncology.handbook.data.entity.ContentBlock
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

            val db = App.instance.database
            val jsonArray = JSONArray()

            for (c in contents) {
                val obj = JSONObject().apply {
                    put("title", c.title)
                    put("category", c.category)
                    put("createdAt", c.createdAt)
                    put("updatedAt", c.updatedAt)
                }

                // 导出 blocks（新版数据）
                val blocks = db.contentBlockDao().getByNoteId(c.id)
                val blocksArray = JSONArray()
                val allMediaPaths = mutableSetOf<String>()

                for (block in blocks) {
                    val blockObj = JSONObject().apply {
                        put("type", block.type)
                        put("orderIndex", block.orderIndex)
                        put("text", block.text)
                    }
                    if (block.filePath.isNotEmpty()) {
                        val src = File(block.filePath)
                        if (src.exists()) {
                            val dest = File(mediaDir, src.name)
                            FileUtils.copyFile(src, dest)
                            blockObj.put("filePath", "media/${src.name}")
                            allMediaPaths.add(block.filePath)
                        } else {
                            blockObj.put("filePath", "")
                        }
                    }
                    blocksArray.put(blockObj)
                }
                obj.put("blocks", blocksArray)

                // 兼容旧字段（旧版数据无 blocks 时用这些）
                val imagePaths = c.imagePaths.split("|").filter { it.isNotEmpty() }
                val exportedImages = JSONArray()
                for (imgPath in imagePaths) {
                    if (allMediaPaths.contains(imgPath)) continue // 已在 blocks 中
                    val src = File(imgPath)
                    if (src.exists()) {
                        val dest = File(mediaDir, src.name)
                        FileUtils.copyFile(src, dest)
                        exportedImages.put("media/${src.name}")
                    }
                }
                obj.put("imagePaths", exportedImages.join("|"))

                if (c.videoPath.isNotEmpty() && !allMediaPaths.contains(c.videoPath)) {
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

                // 保留旧 content 字段用于兼容
                obj.put("content", c.content)

                jsonArray.put(obj)
            }

            val root = JSONObject().apply {
                put("appName", "肿瘤科医生值班手册")
                put("version", 2)
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

    /** 从 JSON 文件导入内容（支持 v1 和 v2 格式） */
    suspend fun importFromJson(
        context: Context,
        uri: Uri
    ): Pair<Int, String> = withContext(Dispatchers.IO) {
        try {
            val jsonText = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                ?: return@withContext 0 to "无法读取文件"

            val root = JSONObject(jsonText)
            val contentsArray = root.optJSONArray("contents") ?: return@withContext 0 to "文件格式不正确"

            // 导入文件所在目录（用于复制媒体文件）
            val importBaseDir = File(StorageHelper.getRootDir(), "import_${System.currentTimeMillis()}")
            importBaseDir.mkdirs()
            val mediaImportDir = File(importBaseDir, "media")
            mediaImportDir.mkdirs()

            var imported = 0
            val db = App.instance.database

            for (i in 0 until contentsArray.length()) {
                val obj = contentsArray.getJSONObject(i)

                val content = UserContent(
                    title = obj.optString("title", "未命名"),
                    content = obj.optString("content", ""),
                    category = obj.optString("category", "导入"),
                    imagePaths = obj.optString("imagePaths", ""),
                    videoPath = obj.optString("videoPath", ""),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                )
                val noteId = db.userContentDao().insert(content)

                // 导入 blocks（v2 格式）
                val blocksArray = obj.optJSONArray("blocks")
                if (blocksArray != null && blocksArray.length() > 0) {
                    val blocks = mutableListOf<ContentBlock>()
                    for (j in 0 until blocksArray.length()) {
                        val blockObj = blocksArray.getJSONObject(j)
                        val relativePath = blockObj.optString("filePath", "")
                        val actualPath = if (relativePath.isNotEmpty()) {
                            // 从导入目录复制媒体文件到应用目录
                            val srcFile = File(importBaseDir, relativePath)
                            if (srcFile.exists()) {
                                val destDir = if (blockObj.optInt("type") == ContentBlock.TYPE_VIDEO)
                                    StorageHelper.getVideosDir() else StorageHelper.getImagesDir()
                                val destFile = File(destDir, srcFile.name)
                                FileUtils.copyFile(srcFile, destFile)
                                destFile.absolutePath
                            } else ""
                        } else ""

                        blocks.add(
                            ContentBlock(
                                noteId = noteId,
                                type = blockObj.optInt("type", ContentBlock.TYPE_TEXT),
                                orderIndex = blockObj.optInt("orderIndex", j),
                                text = blockObj.optString("text", ""),
                                filePath = actualPath
                            )
                        )
                    }
                    if (blocks.isNotEmpty()) {
                        db.contentBlockDao().insertAll(blocks)
                    }
                }

                imported++
            }

            imported to "成功导入 $imported 条内容"
        } catch (e: Exception) {
            0 to "导入失败：${e.message}"
        }
    }
}
