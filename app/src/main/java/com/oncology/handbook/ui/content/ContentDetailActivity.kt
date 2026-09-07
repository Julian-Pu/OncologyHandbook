package com.oncology.handbook.ui.content

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.VideoView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.oncology.handbook.App
import com.oncology.handbook.adapter.BlockViewerAdapter
import com.oncology.handbook.data.entity.ContentBlock
import com.oncology.handbook.data.entity.UserContent
import com.oncology.handbook.databinding.ActivityContentDetailBinding
import com.oncology.handbook.util.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private val displayBlocks = mutableListOf<ContentBlock>()
    private lateinit var adapter: BlockViewerAdapter
    private var activeVideoView: VideoView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContentDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        contentId = intent.getLongExtra(EXTRA_CONTENT_ID, -1L)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

        setupBlocksList()
        loadContent()
    }

    private fun setupBlocksList() {
        adapter = BlockViewerAdapter(
            blocks = displayBlocks,
            onImageClick = { filePath ->
                val intent = Intent(this, ImageViewerActivity::class.java).apply {
                    putExtra(ImageViewerActivity.EXTRA_IMAGE_PATH, filePath)
                }
                startActivity(intent)
            },
            onVideoFullscreen = { filePath ->
                val intent = Intent(this, VideoPlayerActivity::class.java).apply {
                    putExtra(VideoPlayerActivity.EXTRA_VIDEO_PATH, filePath)
                }
                startActivity(intent)
            },
            onVideoViewReady = { videoView ->
                activeVideoView = videoView
            }
        )
        binding.rvBlocks.layoutManager = LinearLayoutManager(this)
        binding.rvBlocks.adapter = adapter
    }

    private fun loadContent() {
        lifecycleScope.launch {
            val (c, blockList) = withContext(Dispatchers.IO) {
                val db = App.instance.database
                val content = db.userContentDao().getById(contentId)
                val blocks = db.contentBlockDao().getByNoteId(contentId)
                content to blocks
            }

            content = c
            c?.let { displayContent(it, blockList) } ?: run {
                Toast.makeText(this@ContentDetailActivity, "内容不存在", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun displayContent(c: UserContent, blockList: List<ContentBlock>) {
        supportActionBar?.title = c.title.ifEmpty { "无标题" }

        binding.tvTitle.text = c.title.ifEmpty { "无标题" }
        binding.tvCategory.text = "分类：${c.category}"

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        binding.tvDate.text = "更新于：${dateFormat.format(Date(c.updatedAt))}"

        displayBlocks.clear()

        if (blockList.isNotEmpty()) {
            // 新版数据
            displayBlocks.addAll(blockList)
        } else {
            // 旧版数据兼容
            if (c.content.isNotEmpty()) {
                displayBlocks.add(
                    ContentBlock(noteId = contentId, type = ContentBlock.TYPE_TEXT, text = c.content, orderIndex = 0)
                )
            }
            var order = 1
            c.imagePaths.split("|").filter { it.isNotEmpty() }.forEach { path ->
                displayBlocks.add(
                    ContentBlock(noteId = contentId, type = ContentBlock.TYPE_IMAGE, filePath = path, orderIndex = order++)
                )
            }
            if (c.videoPath.isNotEmpty()) {
                displayBlocks.add(
                    ContentBlock(noteId = contentId, type = ContentBlock.TYPE_VIDEO, filePath = c.videoPath, orderIndex = order)
                )
            }
        }

        adapter.notifyDataSetChanged()
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
                    .setPositiveButton("删除") { _, _ -> deleteContent() }
                    .setNegativeButton("取消", null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deleteContent() {
        content?.let { c ->
            // 收集所有关联文件路径（包括 blocks 中的）
            val allFilePaths = mutableListOf<String>()
            c.imagePaths.split("|").filter { it.isNotEmpty() }.forEach { allFilePaths.add(it) }
            if (c.videoPath.isNotEmpty()) allFilePaths.add(c.videoPath)
            displayBlocks.forEach { block ->
                if (block.filePath.isNotEmpty()) allFilePaths.add(block.filePath)
            }

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val db = App.instance.database
                    db.contentBlockDao().deleteByNoteId(contentId)
                    db.userContentDao().delete(c)
                    // 删除文件
                    allFilePaths.distinct().forEach { FileUtils.deleteFile(it) }
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

    override fun onPause() {
        super.onPause()
        activeVideoView?.let { if (it.isPlaying) it.pause() }
    }

    override fun onDestroy() {
        activeVideoView?.stopPlayback()
        activeVideoView = null
        super.onDestroy()
    }
}
