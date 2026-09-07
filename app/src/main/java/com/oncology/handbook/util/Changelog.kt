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
            versionName = "1.6.0",
            versionCode = 9,
            date = "2026-09-08",
            items = listOf(
                "【新增】手册支持新增大类：手册页右下角+按钮→新增大类，输入名称即可创建自定义分类",
                "【新增】手册支持删除大类：长按大类标题→确认删除，用户大类连带章节一并删除，内置大类从列表隐藏",
                "【新增】新增章节时可选择用户创建的大类"
            )
        ),
        VersionLog(
            versionName = "1.5.0",
            versionCode = 8,
            date = "2026-09-08",
            items = listOf(
                "【新增】手册支持新增章节：手册页右下角+按钮，选择分类并输入标题后创建，可在编辑器中编写内容",
                "【新增】手册支持删除章节：章节详情页右上角菜单→删除章节，用户新增章节直接删除，内置章节从列表移除",
                "【修复】笔记视频恢复为点击缩略图跳转全屏播放（内联自动播放存在适配问题）",
                "【修复】编辑笔记时图片/视频块的删除按钮被卡片遮挡，现已移至卡片内部正常显示"
            )
        ),
        VersionLog(
            versionName = "1.4.0",
            versionCode = 7,
            date = "2026-09-08",
            items = listOf(
                "【修复】笔记视频播放画面异常（竖屏视频一半黑屏），改为裁剪填充整个播放区域",
                "【新增】手册编辑器增加格式化工具栏：加粗、斜体、下划线、文字颜色（红/蓝/绿/黑）、标题、清除格式"
            )
        ),
        VersionLog(
            versionName = "1.3.0",
            versionCode = 6,
            date = "2026-09-08",
            items = listOf(
                "【新增】手册内容支持用户自行修改，所见即所得编辑器",
                "【新增】手册内容支持添加图片（不支持视频）",
                "【修复】笔记列表显示\"无文字内容\"的问题，保存时同步生成预览",
                "【修复】图片查看器图片缩到左上角的问题，初始化为适配屏幕居中",
                "【优化】笔记视频改为页面内联自动播放，带全屏播放按钮"
            )
        ),
        VersionLog(
            versionName = "1.2.0",
            versionCode = 4,
            date = "2026-09-08",
            items = listOf(
                "【修复】安装包版本号显示为 null 的问题",
                "【修复】首页点击手册跳转后，底部导航状态异常的问题",
                "【重构】笔记系统升级为块级富文本编辑器，支持文字、图片、视频混排",
                "【新增】每个内容块可单独删除，添加图片/视频后带预览",
                "【修复】笔记中图片点击无法放大的问题，新增全屏图片查看器（支持双指缩放、双击放大）",
                "【修复】笔记中视频黑屏无法播放的问题，新增全屏视频播放器",
                "【优化】设置页移除存储路径配置功能，简化界面"
            )
        ),
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
