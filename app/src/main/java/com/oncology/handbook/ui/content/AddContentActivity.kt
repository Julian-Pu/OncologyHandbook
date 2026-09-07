package com.oncology.handbook.ui.content

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.oncology.handbook.App
import com.oncology.handbook.data.entity.UserContent
import com.oncology.handbook.databinding.ActivityAddContentBinding
import com.oncology.handbook.util.FileUtils
import com.oncology.handbook.util.StorageHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddContentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddContentBinding
    private val selectedImagePaths = mutableListOf<String>()
    private var selectedVideoPath: String? = null
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

        // 如果是编辑模式，加载现有内容
        if (editContentId > 0) {
            loadContentForEdit()
        }

        binding.btnAddImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnAddVideo.setOnClickListener {
            pickVideoLauncher.launch("video/*")
        }

        binding.btnRemoveVideo.setOnClickListener {
            selectedVideoPath = null
            binding.videoContainer.visibility = View.GONE
        }

        binding.btnSave.setOnClickListener {
            saveContent()
        }
    }

    private fun loadContentForEdit() {
        lifecycleScope.launch {
            val content = withContext(Dispatchers.IO) {
                App.instance.database.userContentDao().getById(editContentId)
            }
            content?.let {
                binding.etTitle.setText(it.title)
                binding.etContent.setText(it.content)
                binding.etCategory.setText(it.category)
                if (it.imagePaths.isNotEmpty()) {
                    selectedImagePaths.addAll(it.imagePaths.split("|").filter { p -> p.isNotEmpty() })
                    updateImageCount()
                }
                if (it.videoPath.isNotEmpty()) {
                    selectedVideoPath = it.videoPath
                    binding.videoContainer.visibility = View.VISIBLE
                    binding.tvVideoName.text = selectedVideoPath?.substringAfterLast("/")
                }
            }
        }
    }

    private fun saveImages(uris: List<Uri>) {
        lifecycleScope.launch {
            StorageHelper.ensureDirs()
            val imagesDir = StorageHelper.getImagesDir()
            for (uri in uris) {
                val path = withContext(Dispatchers.IO) {
                    FileUtils.copyFromUri(this@AddContentActivity, uri, imagesDir, "img")
                }
                if (path != null) {
                    selectedImagePaths.add(path)
                }
            }
            updateImageCount()
            Toast.makeText(this@AddContentActivity, "已添加 ${uris.size} 张图片", Toast.LENGTH_SHORT).show()
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
                selectedVideoPath = path
                binding.videoContainer.visibility = View.VISIBLE
                binding.tvVideoName.text = path.substringAfterLast("/")
                Toast.makeText(this@AddContentActivity, "视频已添加", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@AddContentActivity, "视频保存失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateImageCount() {
        binding.tvImageCount.text = "已添加 ${selectedImagePaths.size} 张图片"
    }

    private fun saveContent() {
        val title = binding.etTitle.text.toString().trim()
        val content = binding.etContent.text.toString().trim()
        val category = binding.etCategory.text.toString().trim().ifEmpty { "未分类" }

        if (title.isEmpty() && content.isEmpty() && selectedImagePaths.isEmpty() && selectedVideoPath == null) {
            Toast.makeText(this, "请至少输入标题或内容", Toast.LENGTH_SHORT).show()
            return
        }

        val imagePathsStr = selectedImagePaths.joinToString("|")

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val dao = App.instance.database.userContentDao()
                if (editContentId > 0) {
                    dao.getById(editContentId)?.let { existing ->
                        dao.update(
                            existing.copy(
                                title = title.ifEmpty { "无标题" },
                                content = content,
                                category = category,
                                imagePaths = imagePathsStr,
                                videoPath = selectedVideoPath ?: "",
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                    }
                } else {
                    dao.insert(
                        UserContent(
                            title = title.ifEmpty { "无标题" },
                            content = content,
                            category = category,
                            imagePaths = imagePathsStr,
                            videoPath = selectedVideoPath ?: ""
                        )
                    )
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
