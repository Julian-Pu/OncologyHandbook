import java.util.Properties
import java.io.FileInputStream
import java.io.FileOutputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

// ===== 版本管理：每次构建自动递增 versionCode =====
val versionPropsFile = file("version.properties")
val versionProps = Properties()
if (versionPropsFile.exists()) {
    versionProps.load(FileInputStream(versionPropsFile))
}
var appVersionCode = (versionProps["VERSION_CODE"] as String?)?.toIntOrNull() ?: 1
val appVersionName = versionProps["VERSION_NAME"] as String? ?: "1.0.0"

// 仅在执行构建任务时递增版本号
val isBuildTask = gradle.startParameter.taskNames.any {
    it.contains("assemble") || it.contains("bundle") || it.contains("build")
}
if (isBuildTask) {
    appVersionCode++
    versionProps["VERSION_CODE"] = appVersionCode.toString()
    versionProps.store(FileOutputStream(versionPropsFile), "Auto-incremented on build")
}

android {
    namespace = "com.oncology.handbook"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.oncology.handbook"
        minSdk = 24
        targetSdk = 34
        versionCode = appVersionCode
        versionName = appVersionName
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // aapt 忽略 assets 下大文件的压缩警告
    androidResources {
        noCompress += "pdf"
    }
}

// APK 输出文件命名：应用名_版本号（构建完成后重命名）
tasks.register("renameApk") {
    doLast {
        val isRelease = gradle.startParameter.taskNames.any { it.contains("Release", ignoreCase = true) }
        val buildType = if (isRelease) "release" else "debug"
        val outputDir = layout.buildDirectory.dir("outputs/apk/$buildType").get().asFile
        val apkFile = outputDir.listFiles { _, name -> name.endsWith(".apk") }?.firstOrNull()
        if (apkFile != null) {
            val newFile = file("${outputDir.absolutePath}/肿瘤科医生值班手册_v${appVersionName}.apk")
            if (newFile.exists()) newFile.delete()
            apkFile.renameTo(newFile)
            println("APK 已重命名: ${newFile.name}")
        }
    }
}
afterEvaluate {
    tasks.findByName("assembleDebug")?.finalizedBy("renameApk")
    tasks.findByName("assembleRelease")?.finalizedBy("renameApk")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.preference.ktx)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Coil 图片加载
    implementation(libs.coil)

    // PDF 阅读器
    implementation(libs.android.pdf.viewer)

    // 协程
    implementation(libs.kotlinx.coroutines.android)
}
