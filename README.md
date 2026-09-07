# 肿瘤科医生值班手册 - 安卓 APP

## 项目简介

肿瘤科医生值班专用离线参考工具，支持安卓手机和平板设备。内置值班手册、心电图学习手册及 3 本 PDF 参考书籍，支持用户自定义笔记（文字/图片/视频），数据完全本地存储，无需联网。

## 技术栈

- **语言**: Kotlin
- **最低 SDK**: 24 (Android 7.0)
- **目标 SDK**: 34 (Android 14)
- **架构**: MVVM + Room 数据库
- **UI**: Material Design 3 + ViewBinding
- **导航**: Navigation Component + 底部导航
- **PDF 渲染**: android-pdf-viewer (mhiew 维护版)
- **图片加载**: Coil
- **异步**: Kotlin Coroutines + Flow

## 项目结构

```
OncologyHandbookApp/
├── app/
│   ├── build.gradle.kts              # 模块构建配置
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml       # 清单文件（权限、Activity、Provider）
│       ├── assets/books/             # 内置 PDF 书籍（3本，约123MB）
│       ├── java/com/oncology/handbook/
│       │   ├── App.kt                # Application 类
│       │   ├── MainActivity.kt       # 主界面（底部导航容器）
│       │   ├── data/                 # 数据层
│       │   │   ├── AppDatabase.kt    # Room 数据库
│       │   │   ├── entity/           # 实体类
│       │   │   ├── dao/              # 数据访问对象
│       │   │   └── repository/       # 数据仓库
│       │   ├── ui/
│       │   │   ├── home/             # 首页
│       │   │   ├── bookshelf/        # 书架（PDF列表+阅读器）
│       │   │   ├── manual/           # 内置手册浏览
│       │   │   ├── content/          # 用户内容管理
│       │   │   └── settings/         # 设置（存储、导入导出）
│       │   ├── util/                 # 工具类
│       │   │   ├── StorageHelper.kt  # 存储路径管理
│       │   │   ├── FileUtils.kt      # 文件操作
│       │   │   ├── JsonExporter.kt   # 数据导入导出
│       │   │   └── ManualContent.kt  # 内置手册内容数据
│       │   └── adapter/              # RecyclerView 适配器
│       └── res/                      # 资源文件（布局、颜色、菜单、图标）
├── build.gradle.kts                  # 项目级构建配置
├── settings.gradle.kts               # 项目设置
├── gradle.properties
├── gradle/libs.versions.toml         # 依赖版本管理
└── gradle/wrapper/gradle-wrapper.properties
```

## 构建步骤

### 前置要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- Android SDK 34
- JDK 17

> **注意**：克隆项目后，用 Android Studio 打开会自动生成 `local.properties`（含 SDK 路径）。若用命令行构建，需手动在项目根目录创建 `local.properties`，内容为 `sdk.dir=你的Android SDK路径`。

### 构建 APK

1. **打开项目**: 用 Android Studio 打开 `OncologyHandbookApp` 文件夹
2. **同步 Gradle**: 等待 Android Studio 自动同步（首次会下载 Gradle 8.5 和依赖）
3. **构建 APK**:
   - 菜单栏 → Build → Build Bundle(s) / APK(s) → Build APK(s)
   - 或命令行: `./gradlew assembleDebug`（Mac/Linux）/ `gradlew assembleDebug`（Windows）
4. **获取 APK**: 构建完成后，APK 位于 `app/build/outputs/apk/debug/app-debug.apk`

### 构建 Release 版（可选）

1. 生成签名密钥: Build → Generate Signed Bundle / APK
2. 选择 APK，配置签名密钥
3. 选择 release 构建类型
4. 生成的 APK 位于 `app/release/` 目录

## 功能说明

### 1. 首页
- 快捷入口：急诊心电图手册、心电图从入门到精通、新增笔记
- 手册概览信息

### 2. 书架
- 内置 3 本 PDF 书籍：
  - 心电图从入门到精通
  - 急诊医生心电图手册（第一册）
  - 急诊医生心电图手册（第二册）
- 内置 PDF 阅读器：支持翻页、缩放、页码跳转、进度条

### 3. 手册
- 值班手册（8章）：值班前准备、常见症状处理、高危急症、不良反应、癌痛管理、临终关怀、抢救药物、参考资源
- 心电图学习手册（6章）：学习意义、学习路径、肿瘤科ECG场景、学习资源、快速决策参考、学习建议
- 支持全文搜索

### 4. 我的内容
- 用户可添加自定义笔记：标题、分类、文字内容
- 支持添加多张图片（从相册选择）
- 支持添加视频（从相册选择）
- 支持搜索、编辑、删除
- 数据保存在 Room 数据库，媒体文件保存在存储目录

### 5. 设置
- 存储权限管理（Android 11+ 需授权"所有文件访问"）
- 默认存储路径：手机根目录/肿瘤内科医生值班手册a/
  - images/ - 用户添加的图片
  - videos/ - 用户添加的视频
  - exports/ - 导出的数据备份
- 数据导出：将所有内容（含图片视频）导出为 JSON + 媒体文件
- 数据导入：从 JSON 文件恢复内容
- 应用信息

## 存储权限说明

本应用需要存储权限以保存用户添加的图片和视频文件。

- **Android 10 及以下**: 申请 READ_EXTERNAL_STORAGE / WRITE_EXTERNAL_STORAGE
- **Android 11 及以上**: 申请 MANAGE_EXTERNAL_STORAGE（所有文件访问权限）
  - 首次启动会弹出授权引导，点击"去授权"跳转到系统设置页面
  - 也可在"设置"页面随时申请

## 数据导入导出

### 导出
- 导出位置：存储目录/exports/backup_年月日_时分秒/
- 包含：data.json（所有笔记数据）+ media/（关联的图片和视频）

### 导入
- 选择之前导出的 data.json 文件
- 导入的内容会追加到现有数据中，分类标记为"导入"

## 注意事项

1. **PDF 书籍已封装在 APK 中**，位于 `app/src/main/assets/books/`，无需额外下载
2. **完全离线使用**，不包含任何联网功能或第三方统计
3. **平板适配**: 界面采用响应式布局，在平板横屏模式下体验更佳
4. **数据安全**: 所有数据仅保存在设备本地，卸载应用前请先导出备份
5. 如需更换内置 PDF 书籍，替换 `app/src/main/assets/books/` 目录下的文件，并同步修改 `BookshelfFragment.kt` 中的书籍列表

## 常见问题

**Q: 构建时报错 "Could not find com.github.mhiew:android-pdf-viewer"**
A: 确保项目的 `settings.gradle.kts` 中已添加 JitPack 仓库（已配置），同步 Gradle 即可。

**Q: APK 体积很大？**
A: 因为内置了 3 本 PDF 书籍（约123MB），这是正常的。如需减小体积，可移除部分 PDF 书籍。

**Q: Android 11+ 无法保存图片/视频？**
A: 请在设置中授予"所有文件访问权限"（MANAGE_EXTERNAL_STORAGE）。

**Q: 如何修改默认存储文件夹名称？**
A: 修改 `StorageHelper.kt` 中的 `DEFAULT_FOLDER_NAME` 常量。
