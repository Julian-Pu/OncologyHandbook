package com.oncology.handbook.util

/**
 * 应用更新记录
 * 每次发布新版本时在此添加新条目
 */
object Changelog {

    data class VersionLog(
        val versionName: String,
        val versionCode: Int,
        val date: String,
        val items: List<String>
    )

    val logs = listOf(
        VersionLog(
            versionName = "1.1.0",
            versionCode = 2,
            date = "2026-09-07",
            items = listOf(
                "【修复】首页快捷入口\"手册\"卡片点击无跳转的问题",
                "【修复】设置页\"打开存储文件夹\"无法正常打开的问题，改用系统文件管理器",
                "【优化】APK 安装包命名为应用名称，便于识别",
                "【优化】每次打包版本号自动递增",
                "【新增】应用更新记录弹窗，新版本首次打开时显示更新内容"
            )
        ),
        VersionLog(
            versionName = "1.0.0",
            versionCode = 1,
            date = "2026-09-07",
            items = listOf(
                "【新增】内置肿瘤科值班手册（8大章节）",
                "【新增】内置肿瘤科心电图学习手册（6大章节）",
                "【新增】内置3本PDF参考书籍，支持离线阅读",
                "【新增】PDF阅读器：翻页、缩放、页码跳转",
                "【新增】用户自定义笔记：支持文字、多张图片、视频",
                "【新增】Room数据库本地存储，数据永不丢失",
                "【新增】数据导入导出功能，支持JSON格式备份",
                "【新增】存储权限管理，默认保存到手机根目录",
                "【新增】全文搜索：手册内容和用户笔记均可搜索",
                "【优化】Material Design 3界面，适配手机和平板",
                "【优化】完全离线运行，无需联网"
            )
        )
    )

    /** 获取当前版本的更新记录（最新一条） */
    fun getCurrentVersionLog(): VersionLog = logs.first()

    /** 格式化更新记录为可显示的文本 */
    fun formatAllLogs(): String {
        return logs.joinToString("\n\n") { log ->
            val sb = StringBuilder()
            sb.append("v${log.versionName}（${log.date}）\n")
            log.items.forEachIndexed { index, item ->
                sb.append("${index + 1}. $item\n")
            }
            sb.toString().trimEnd()
        }
    }

    /** 仅格式化最新版本的更新记录 */
    fun formatLatestLog(): String {
        val log = logs.first()
        val sb = StringBuilder()
        sb.append("v${log.versionName}（${log.date}）\n\n")
        log.items.forEachIndexed { index, item ->
            sb.append("${index + 1}. $item\n")
        }
        return sb.toString().trimEnd()
    }
}
