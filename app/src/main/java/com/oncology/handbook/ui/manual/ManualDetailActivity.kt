package com.oncology.handbook.ui.manual

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.oncology.handbook.App
import com.oncology.handbook.R
import com.oncology.handbook.databinding.ActivityManualDetailBinding
import com.oncology.handbook.util.ManualContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManualDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CATEGORY_ID = "extra_category_id"
        const val EXTRA_SECTION_ID = "extra_section_id"
        const val EXTRA_TITLE = "extra_title"
    }

    private lateinit var binding: ActivityManualDetailBinding
    private var categoryId: String = ""
    private var sectionId: String = ""
    private var sectionTitle: String = ""
    private var currentHtml: String = ""
    private var isUserModified: Boolean = false

    private val editLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            loadContent()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManualDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoryId = intent.getStringExtra(EXTRA_CATEGORY_ID) ?: ""
        sectionId = intent.getStringExtra(EXTRA_SECTION_ID) ?: ""
        sectionTitle = intent.getStringExtra(EXTRA_TITLE) ?: "手册内容"

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = sectionTitle
        }

        loadContent()
    }

    private fun loadContent() {
        lifecycleScope.launch {
            // 优先从数据库加载用户修改的内容
            val userEdit = withContext(Dispatchers.IO) {
                App.instance.database.manualEditDao().getBySectionId(sectionId)
            }

            if (userEdit != null) {
                currentHtml = userEdit.htmlContent
                isUserModified = true
                setupWebView(currentHtml)
            } else {
                // 加载内置内容
                val section = ManualContent.findSection(categoryId, sectionId)
                if (section != null) {
                    currentHtml = section.htmlContent
                    isUserModified = false
                    setupWebView(currentHtml)
                } else {
                    binding.webView.loadData("<p>内容未找到</p>", "text/html; charset=utf-8", "UTF-8")
                }
            }
            invalidateOptionsMenu()
        }
    }

    private fun setupWebView(htmlContent: String) {
        val styledHtml = """
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
            <body>
            $htmlContent
            </body>
            </html>
        """.trimIndent()

        binding.webView.apply {
            settings.javaScriptEnabled = false
            settings.domStorageEnabled = false
            settings.allowFileAccess = true
            webViewClient = WebViewClient()
            loadDataWithBaseURL(null, styledHtml, "text/html", "UTF-8", null)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_manual_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { finish(); true }
            R.id.action_edit -> {
                val intent = Intent(this, ManualEditActivity::class.java).apply {
                    putExtra(ManualEditActivity.EXTRA_SECTION_ID, sectionId)
                    putExtra(ManualEditActivity.EXTRA_SECTION_TITLE, sectionTitle)
                    putExtra(ManualEditActivity.EXTRA_INITIAL_HTML, currentHtml)
                }
                editLauncher.launch(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        binding.webView.destroy()
        super.onDestroy()
    }
}
