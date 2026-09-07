package com.oncology.handbook.ui.content

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.oncology.handbook.App
import com.oncology.handbook.adapter.BlockEditorAdapter
import com.oncology.handbook.data.entity.ContentBlock
import com.oncology.handbook.data.entity.UserContent
import com.oncology.handbook.databinding.ActivityAddContentBinding
import com.oncology.handbook.util.FileUtils
import com.oncology.handbook.util.StorageHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddContentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddContentBinding
    private val blocks = mutableListOf<EditorBlock>()
    private lateinit var adapter: BlockEditorAdapter
    private var editContentId: Long = -1L

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            saveImages(uris)
        }
    }

    private val pickVideoLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            saveVideo(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddContentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        editContentId = intent.getLongExtra(EXTRA_EDIT_ID, -1L)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = if (editContentId > 0) "编辑内容" else "新增内容"
        }

        setupBlocksList()

        binding.btnAddText.setOnClickListener {
            blocks.add(EditorBlock(type = ContentBlock.TYPE_TEXT, text = ""))
            adapter.notifyItemInserted(blocks.size - 1)
            binding.rvBlocks.scrollToPosition(blocks.size - 1)
        }

        binding.btnAddImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnAddVideo.setOnClickListener {
            pickVideoLauncher.launch("video/*")
        }

        binding.btnSave.setOnClickListener {
            saveContent()
        }

        if (editContentId > 0) {
            loadContentForEdit()
        }
    }

    private fun setupBlocksList() {
        adapter = BlockEditorAdapter(
            blocks = blocks,
            onBlockDeleted = { position ->
                if (position in blocks.indices) {
                    blocks.removeAt(position)
                    adapter.notifyItemRemoved(position)
                    adapter.notifyItemRangeChanged(position, blocks.size)
                }
            }
        )
        binding.rvBlocks.layoutManager = LinearLayoutManager(this)
        binding.rvBlocks.adapter = adapter
    }

    private fun loadContentForEdit() {
        lifecycleScope.launch {
            val (content, blockList) = withContext(Dispatchers.IO) {
                val dao = App.instance.database
                val c = dao.userContentDao().getById(editContentId)
                val b = dao.contentBlockDao().getByNoteId(editContentId)
                c to b
            }

            content?.let {
                binding.etTitle.setText(it.title)
                binding.etCategory.setText(it.category)

                if (blockList.isNotEmpty()) {
                    // 新版数据：使用 blocks
                    blocks.clear()
                    blocks.addAll(blockList.map { block -> EditorBlock.fromContentBlock(block) })
                    adapter.notifyDataSetChanged()
                } else {
                    // 旧版数据兼容：从 content/imagePaths/videoPath 构建虚拟 blocks
                    blocks.clear()
                    if (it.content.isNotEmpty()) {
                        blocks.add(EditorBlock(type = ContentBlock.TYPE_TEXT, text = it.content))
                    }
                    it.imagePaths.split("|").filter { p -> p.isNotEmpty() }.forEach { path ->
                        blocks.add(EditorBlock(type = ContentBlock.TYPE_IMAGE, filePath = path))
                    }
                    if (it.videoPath.isNotEmpty()) {
                        blocks.add(EditorBlock(type = ContentBlock.TYPE_VIDEO, filePath = it.videoPath))
                    }
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun saveImages(uris: List<Uri>) {
        lifecycleScope.launch {
            StorageHelper.ensureDirs()
            val imagesDir = StorageHelper.getImagesDir()
            var added = 0
            for (uri in uris) {
                val path = withContext(Dispatchers.IO) {
                    FileUtils.copyFromUri(this@AddContentActivity, uri, imagesDir, "img")
                }
                if (path != null) {
                    blocks.add(EditorBlock(type = ContentBlock.TYPE_IMAGE, filePath = path))
                    added++
                }
            }
            if (added > 0) {
                adapter.notifyItemRangeInserted(blocks.size - added, added)
                binding.rvBlocks.scrollToPosition(blocks.size - 1)
                Toast.makeText(this@AddContentActivity, "已添加 $added 张图片", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveVideo(uri: Uri) {
        lifecycleScope.launch {
            StorageHelper.ensureDirs()
            val videosDir = StorageHelper.getVideosDir()
            val path = withContext(Dispatchers.IO) {
                FileUtils.copyFromUri(this@AddContentActivity, uri, videosDir, "video")
            }
            if (path != null) {
                blocks.add(EditorBlock(type = ContentBlock.TYPE_VIDEO, filePath = path))
                adapter.notifyItemInserted(blocks.size - 1)
                binding.rvBlocks.scrollToPosition(blocks.size - 1)
                Toast.makeText(this@AddContentActivity, "视频已添加", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@AddContentActivity, "视频保存失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveContent() {
        val title = binding.etTitle.text.toString().trim()
        val category = binding.etCategory.text.toString().trim().ifEmpty { "未分类" }

        // 过滤掉空的文字块
        val validBlocks = blocks.filter { block ->
            when (block.type) {
                ContentBlock.TYPE_TEXT -> block.text.isNotBlank()
                ContentBlock.TYPE_IMAGE, ContentBlock.TYPE_VIDEO -> block.filePath.isNotEmpty()
                else -> false
            }
        }

        if (title.isEmpty() && validBlocks.isEmpty()) {
            Toast.makeText(this, "请至少输入标题或添加内容块", Toast.LENGTH_SHORT).show()
            return
        }

        // 从文字块生成预览文本，用于列表展示
        val contentPreview = validBlocks
            .filter { it.type == ContentBlock.TYPE_TEXT }
            .joinToString(" ") { it.text.trim() }
            .take(200)

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val db = App.instance.database
                val now = System.currentTimeMillis()

                if (editContentId > 0) {
                    // 更新模式
                    db.userContentDao().getById(editContentId)?.let { existing ->
                        db.userContentDao().update(
                            existing.copy(
                                title = title.ifEmpty { "无标题" },
                                content = contentPreview,
                                category = category,
                                updatedAt = now
                            )
                        )
                        // 重建 blocks
                        db.contentBlockDao().deleteByNoteId(editContentId)
                        val contentBlocks = validBlocks.mapIndexed { index, block ->
                            block.toContentBlock(editContentId, index)
                        }
                        if (contentBlocks.isNotEmpty()) {
                            db.contentBlockDao().insertAll(contentBlocks)
                        } else { }
                    }
                } else {
                    // 新增模式
                    val noteId = db.userContentDao().insert(
                        UserContent(
                            title = title.ifEmpty { "无标题" },
                            content = contentPreview,
                            category = category,
                            createdAt = now,
                            updatedAt = now
                        )
                    )
                    val contentBlocks = validBlocks.mapIndexed { index, block ->
                        block.toContentBlock(noteId, index)
                    }
                    if (contentBlocks.isNotEmpty()) {
                        db.contentBlockDao().insertAll(contentBlocks)
                    } else { }
                }
            }
            Toast.makeText(this@AddContentActivity, "保存成功", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val EXTRA_EDIT_ID = "extra_edit_id"
    }
}
