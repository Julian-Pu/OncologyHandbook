package com.oncology.handbook

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.oncology.handbook.databinding.ActivityMainBinding
import com.oncology.handbook.util.Changelog
import com.oncology.handbook.util.StorageHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            StorageHelper.ensureDirs()
        } else {
            Toast.makeText(this, "存储权限未授予，部分功能可能受限", Toast.LENGTH_LONG).show()
        }
    }

    private val manageStorageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                StorageHelper.ensureDirs()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)

        checkStoragePermissions()
        checkChangelog()
    }

    private fun checkChangelog() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val lastShownVersion = prefs.getString("last_changelog_version", "") ?: ""
        val currentVersion = getAppVersionName()

        if (currentVersion != lastShownVersion) {
            showChangelogDialog(prefs)
        }
    }

    private fun getAppVersionName(): String {
        return try {
            packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    private fun showChangelogDialog(prefs: SharedPreferences) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_changelog, null)
        val tvChangelog = dialogView.findViewById<TextView>(R.id.tv_changelog)
        val cbDontShow = dialogView.findViewById<CheckBox>(R.id.cb_dont_show)

        tvChangelog.text = Changelog.formatLatestLog()

        val versionName = getAppVersionName()
        val dialog = AlertDialog.Builder(this)
            .setTitle("更新记录")
            .setView(dialogView)
            .setPositiveButton("知道了") { _, _ ->
                if (cbDontShow.isChecked) {
                    prefs.edit().putString("last_changelog_version", versionName).apply()
                }
            }
            .setOnCancelListener {
                if (cbDontShow.isChecked) {
                    prefs.edit().putString("last_changelog_version", versionName).apply()
                }
            }
            .create()

        dialog.show()
    }

    private fun checkStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                AlertDialog.Builder(this)
                    .setTitle("存储权限")
                    .setMessage("本应用需要存储权限以保存您添加的文字、图片和视频内容，并支持数据导入导出。\n\n默认保存位置：手机根目录/肿瘤内科医生值班手册a/")
                    .setPositiveButton("去授权") { _, _ ->
                        try {
                            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                            intent.data = Uri.parse("package:$packageName")
                            manageStorageLauncher.launch(intent)
                        } catch (e: Exception) {
                            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                            manageStorageLauncher.launch(intent)
                        }
                    }
                    .setNegativeButton("暂不", null)
                    .show()
            } else {
                StorageHelper.ensureDirs()
            }
        } else {
            val permissions = mutableListOf<String>()
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            if (permissions.isNotEmpty()) {
                storagePermissionLauncher.launch(permissions.toTypedArray())
            } else {
                StorageHelper.ensureDirs()
            }
        }
    }
}
