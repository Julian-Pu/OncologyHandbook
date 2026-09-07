package com.oncology.handbook.ui.manual

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.oncology.handbook.App
import com.oncology.handbook.R
import com.oncology.handbook.data.entity.ManualEdit
import com.oncology.handbook.databinding.ActivityManualEditBinding
import com.oncology.handbook.util.FileUtils
import com.oncology.handbook.util.StorageHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ManualEditActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SECTION_ID = "extra_section_id"
        const val EXTRA_SECTION_TITLE = "extra_section_title"
        const val EXTRA_INITIAL_HTML = "extra_initial_html"
    }

    private lateinit var binding: ActivityManualEditBinding
    private var sectionId: String = ""
    private var sectionTitle: String = ""
    private var initialHtml: String = ""
    private var currentHtml: String = ""
    private var isSaving = false

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            insertImage(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManualEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sectionId = intent.getStringExtra(EXTRA_SECTION_ID) ?: ""
        sectionTitle = intent.getStringExtra(EXTRA_SECTION_TITLE) ?: "编辑手册"
        initialHtml = intent.getStringExtra(EXTRA_INITIAL_HTML) ?: ""
        currentHtml = initialHtml

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "编辑：$sectionTitle"
        }

        setupWebView()
    }

    private fun setupWebView() {
        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            webChromeClient = WebChromeClient()
            addJavascriptInterface(JsBridge(), "AndroidBridge")
        }

        val styledHtml = buildEditableHtml(initialHtml)
        binding.webView.loadDataWithBaseURL(null, styledHtml, "text/html", "UTF-8", null)
    }

    private fun buildEditableHtml(content: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body {
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
                    font-size: 16px;
                    line-height: 1.8;
                    color: #212121;
                    padding: 16px;
                    background-color: #ffffff;
                    max-width: 900px;
                    margin: 0 auto;
                    outline: none;
                }
                h3 { color: #1565c0; border-bottom: 2px solid #e3f2fd; padding-bottom: 8px; margin-top: 24px; }
                h4 { color: #1976d2; margin-top: 20px; }
                strong { color: #c62828; }
                ul, ol { padding-left: 24px; }
                li { margin-bottom: 6px; }
                img { max-width: 100%; height: auto; border-radius: 8px; margin: 12px 0; display: block; }
                table { width: 100%; margin: 12px 0; font-size: 14px; }
                th { background-color: #e3f2fd; text-align: left; }
                th, td { padding: 8px; border: 1px solid #bdbdbd; vertical-align: top; }
                .tip { background-color: #f5f5f5; padding: 12px; border-radius: 8px; margin-top: 16px; font-size: 14px; color: #616161; }
                .warning { background-color: #fff3e0; padding: 12px; border-radius: 8px; margin-top: 16px; border-left: 4px solid #ff9800; }
                hr { border: none; border-top: 1px solid #e0e0e0; margin: 20px 0; }
            </style>
            </head>
            <body contenteditable="true">
            $content
            </body>
            </html>
        """.trimIndent()
    }

    private fun insertImage(uri: Uri) {
        lifecycleScope.launch {
            StorageHelper.ensureDirs()
            val imagesDir = File(StorageHelper.getRootDir(), "manual_images").apply { mkdirs() }
            val path = withContext(Dispatchers.IO) {
                FileUtils.copyFromUri(this@ManualEditActivity, uri, imagesDir, "manual")
            }
            if (path != null) {
                // 在 WebView 光标处插入图片
                val fileUrl = "file:///$path"
                binding.webView.evaluateJavascript(
                    "document.execCommand('insertImage', false, '$fileUrl')", null
                )
                Toast.makeText(this@ManualEditActivity, "图片已插入", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@ManualEditActivity, "图片保存失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveContent() {
        if (isSaving) return
        isSaving = true

        // 通过 JS 获取 body.innerHTML
        binding.webView.evaluateJavascript("document.body.innerHTML") { result ->
            // evaluateJavascript 返回 JSON 编码的字符串，需要去除引号
            val html = if (result != null && result.length >= 2 && result.startsWith("\"") && result.endsWith("\"")) {
                result.substring(1, result.length - 1)
                    .replace("\\\"", "\"")
                    .replace("\\n", "\n")
                    .replace("\\/", "/")
            } else {
                result ?: ""
            }

            currentHtml = html

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val edit = ManualEdit(
                        sectionId = sectionId,
                        htmlContent = html,
                        updatedAt = System.currentTimeMillis()
                    )
                    App.instance.database.manualEditDao().insert(edit)
                }
                Toast.makeText(this@ManualEditActivity, "已保存修改", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                isSaving = false
                finish()
            }
        }
    }

    private fun confirmExit() {
        // 简单检查是否有修改（通过比较长度，不精确但够用）
        binding.webView.evaluateJavascript("document.body.innerHTML.length") { lenStr ->
            val len = lenStr?.toIntOrNull() ?: 0
            if (len != initialHtml.length && !isSaving) {
                AlertDialog.Builder(this)
                    .setTitle("未保存的修改")
                    .setMessage("您有未保存的修改，确定要离开吗？")
                    .setPositiveButton("保存并离开") { _, _ -> saveContent() }
                    .setNegativeButton("放弃修改") { _, _ -> finish() }
                    .setNeutralButton("继续编辑", null)
                    .show()
            } else {
                finish()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_manual_edit, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                confirmExit()
                true
            }
            R.id.action_add_image -> {
                pickImageLauncher.launch("image/*")
                true
            }
            R.id.action_save -> {
                saveContent()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        confirmExit()
    }

    inner class JsBridge {
        @JavascriptInterface
        fun log(msg: String) {
            // 用于调试
        }
    }

    override fun onDestroy() {
        binding.webView.destroy()
        super.onDestroy()
    }
}
