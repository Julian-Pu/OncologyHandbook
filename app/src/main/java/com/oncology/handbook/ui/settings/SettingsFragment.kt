package com.oncology.handbook.ui.settings

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.oncology.handbook.App
import com.oncology.handbook.databinding.FragmentSettingsBinding
import com.oncology.handbook.util.JsonExporter
import com.oncology.handbook.util.StorageHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val importFileLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            importData(uri)
        }
    }

    private val manageStorageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        updateStorageStatus()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateStorageStatus()
        updateContentCount()

        // 存储权限
        binding.btnRequestPermission.setOnClickListener {
            requestStoragePermission()
        }

        // 重置存储路径
        binding.btnResetPath.setOnClickListener {
            StorageHelper.resetToDefault()
            StorageHelper.ensureDirs()
            updateStorageStatus()
            Toast.makeText(requireContext(), "已恢复默认存储路径", Toast.LENGTH_SHORT).show()
        }

        // 导出数据
        binding.btnExport.setOnClickListener {
            exportData()
        }

        // 导入数据
        binding.btnImport.setOnClickListener {
            importFileLauncher.launch("application/json")
        }

        // 打开存储目录
        binding.btnOpenFolder.setOnClickListener {
            openStorageFolder()
        }
    }

    private fun updateStorageStatus() {
        val rootDir = StorageHelper.getRootDir()
        binding.tvStoragePath.text = rootDir.absolutePath

        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true // 旧版本在 MainActivity 已请求
        }

        binding.tvPermissionStatus.text = if (hasPermission) "已授权" else "未授权"
        binding.tvPermissionStatus.setTextColor(
            if (hasPermission) android.graphics.Color.parseColor("#2e7d32")
            else android.graphics.Color.parseColor("#c62828")
        )
    }

    private fun updateContentCount() {
        viewLifecycleOwner.lifecycleScope.launch {
            val count = withContext(Dispatchers.IO) {
                App.instance.database.userContentDao().getCount()
            }
            binding.tvContentCount.text = "$count 条"
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.parse("package:${requireContext().packageName}")
                    manageStorageLauncher.launch(intent)
                } catch (e: Exception) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    manageStorageLauncher.launch(intent)
                }
            } else {
                Toast.makeText(requireContext(), "存储权限已授权", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "当前系统版本无需额外授权", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exportData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val contentList = withContext(Dispatchers.IO) {
                App.instance.database.userContentDao().getAll().first()
            }

            val result = JsonExporter.exportAll(requireContext(), contentList)
            if (result.success) {
                Toast.makeText(requireContext(), "${result.message}\n路径：${result.filePath}", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun importData(uri: Uri) {
        viewLifecycleOwner.lifecycleScope.launch {
            val (count, message) = JsonExporter.importFromJson(requireContext(), uri)
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            if (count > 0) {
                updateContentCount()
            }
        }
    }

    private fun openStorageFolder() {
        val folder = StorageHelper.getRootDir()
        val externalRoot = Environment.getExternalStorageDirectory().absolutePath

        // 尝试通过 DocumentsContract 打开文件夹（Android 7+ 通用方式）
        try {
            val relativePath = folder.absolutePath.removePrefix("$externalRoot/")
            val docUri = DocumentsContract.buildDocumentUri(
                "com.android.externalstorage.documents",
                "primary:$relativePath"
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(docUri, DocumentsContract.Document.MIME_TYPE_DIR)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
            startActivity(intent)
            return
        } catch (e: Exception) {
            // 继续尝试下一种方式
        }

        // 备用方式：尝试用文件管理器打开
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.fromFile(folder), "resource/folder")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(Intent.createChooser(intent, "打开文件夹"))
            return
        } catch (e: Exception) {
            // 继续到对话框方式
        }

        // 最终备用：显示路径对话框，支持复制路径
        showFolderPathDialog(folder.absolutePath)
    }

    private fun showFolderPathDialog(path: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        AlertDialog.Builder(requireContext())
            .setTitle("存储文件夹路径")
            .setMessage(path + "\n\n请使用文件管理器手动打开上述路径。")
            .setPositiveButton("复制路径") { _, _ ->
                clipboard.setPrimaryClip(ClipData.newPlainText("folder_path", path))
                Toast.makeText(requireContext(), "路径已复制到剪贴板", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("关闭", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        updateStorageStatus()
        updateContentCount()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
