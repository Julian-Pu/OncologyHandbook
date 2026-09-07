package com.oncology.handbook.ui.settings

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.oncology.handbook.App
import com.oncology.handbook.databinding.FragmentSettingsBinding
import com.oncology.handbook.util.JsonExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateContentCount()
        updateVersionInfo()

        binding.btnExport.setOnClickListener { exportData() }
        binding.btnImport.setOnClickListener { importFileLauncher.launch("application/json") }
    }

    private fun updateVersionInfo() {
        val versionName = try {
            requireContext().packageManager
                .getPackageInfo(requireContext().packageName, 0).versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
        binding.tvVersion.text = "版本 $versionName"
    }

    private fun updateContentCount() {
        viewLifecycleOwner.lifecycleScope.launch {
            val count = withContext(Dispatchers.IO) {
                App.instance.database.userContentDao().getCount()
            }
            binding.tvContentCount.text = "$count 条"
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

    override fun onResume() {
        super.onResume()
        updateContentCount()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
