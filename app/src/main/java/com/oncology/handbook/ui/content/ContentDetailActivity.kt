package com.oncology.handbook.ui.content

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import coil.load
import com.oncology.handbook.App
import com.oncology.handbook.data.entity.UserContent
import com.oncology.handbook.databinding.ActivityContentDetailBinding
import com.oncology.handbook.util.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ContentDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CONTENT_ID = "extra_content_id"
    }

    private lateinit var binding: ActivityContentDetailBinding
    private var content: UserContent? = null
    private var contentId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContentDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        contentId = intent.getLongExtra(EXTRA_CONTENT_ID, -1L)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

        loadContent()
    }

    private fun loadContent() {
        lifecycleScope.launch {
            content = withContext(Dispatchers.IO) {
                App.instance.database.userContentDao().getById(contentId)
            }
            content?.let { displayContent(it) } ?: run {
                Toast.makeText(this@ContentDetailActivity, "内容不存在", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun displayContent(c: UserContent) {
        supportActionBar?.title = c.title.ifEmpty { "无标题" }

        binding.tvTitle.text = c.title.ifEmpty { "无标题" }
        binding.tvCategory.text = "分类：${c.category}"

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        binding.tvDate.text = "更新于：${dateFormat.format(Date(c.updatedAt))}"

        if (c.content.isNotEmpty()) {
            binding.tvContent.text = c.content
            binding.tvContent.visibility = View.VISIBLE
        } else {
            binding.tvContent.visibility = View.GONE
        }

        // 显示图片
        val imagePaths = c.imagePaths.split("|").filter { it.isNotEmpty() }
        if (imagePaths.isNotEmpty()) {
            binding.imagesContainer.visibility = View.VISIBLE
            binding.imagesContainer.removeAllViews()
            for (path in imagePaths) {
                val file = File(path)
                if (file.exists()) {
                    val imageView = ImageView(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            600
                        ).apply { bottomMargin = 16 }
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        setOnClickListener { openImage(file) }
                    }
                    imageView.load(file)
                    binding.imagesContainer.addView(imageView)
                }
            }
        } else {
            binding.imagesContainer.visibility = View.GONE
        }

        // 显示视频
        if (c.videoPath.isNotEmpty()) {
            val videoFile = File(c.videoPath)
            if (videoFile.exists()) {
                binding.videoContainer.visibility = View.VISIBLE
                val mediaController = MediaController(this)
                mediaController.setAnchorView(binding.videoView)
                binding.videoView.setVideoURI(Uri.fromFile(videoFile))
                binding.videoView.setMediaController(mediaController)
                binding.videoView.setOnPreparedListener {
                    binding.videoView.start()
                }
            } else {
                binding.videoContainer.visibility = View.GONE
            }
        } else {
            binding.videoContainer.visibility = View.GONE
        }
    }

    private fun openImage(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "image/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "无法打开图片", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(com.oncology.handbook.R.menu.menu_content_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { finish(); true }
            com.oncology.handbook.R.id.action_edit -> {
                val intent = Intent(this, AddContentActivity::class.java).apply {
                    putExtra(AddContentActivity.EXTRA_EDIT_ID, contentId)
                }
                startActivity(intent)
                true
            }
            com.oncology.handbook.R.id.action_delete -> {
                AlertDialog.Builder(this)
                    .setTitle("删除确认")
                    .setMessage("确定要删除这条内容吗？关联的图片和视频文件也将被删除。")
                    .setPositiveButton("删除") { _, _ ->
                        deleteContent()
                    }
                    .setNegativeButton("取消", null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deleteContent() {
        content?.let { c ->
            // 删除关联文件
            c.imagePaths.split("|").filter { it.isNotEmpty() }.forEach { FileUtils.deleteFile(it) }
            if (c.videoPath.isNotEmpty()) FileUtils.deleteFile(c.videoPath)

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    App.instance.database.userContentDao().delete(c)
                }
                Toast.makeText(this@ContentDetailActivity, "已删除", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (contentId > 0) loadContent()
    }

    override fun onDestroy() {
        binding.videoView.stopPlayback()
        super.onDestroy()
    }
}
