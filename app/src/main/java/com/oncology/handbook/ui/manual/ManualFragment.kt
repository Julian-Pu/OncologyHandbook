package com.oncology.handbook.ui.manual

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.oncology.handbook.R
import com.oncology.handbook.adapter.ManualCategoryAdapter
import com.oncology.handbook.databinding.FragmentManualBinding
import com.oncology.handbook.util.ManualCategory
import com.oncology.handbook.util.ManualRepository
import kotlinx.coroutines.launch

class ManualFragment : Fragment() {

    private var _binding: FragmentManualBinding? = null
    private val binding get() = _binding!!

    private var allCategories: List<ManualCategory> = emptyList()
    private lateinit var adapter: ManualCategoryAdapter

    private val editLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) {
        loadCategories()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManualBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ManualCategoryAdapter(emptyList(), { category, section ->
            val intent = Intent(requireContext(), ManualDetailActivity::class.java).apply {
                putExtra(ManualDetailActivity.EXTRA_CATEGORY_ID, category.id)
                putExtra(ManualDetailActivity.EXTRA_SECTION_ID, section.id)
                putExtra(ManualDetailActivity.EXTRA_TITLE, section.title)
            }
            startActivity(intent)
        }, { category ->
            // 长按分类 → 删除
            confirmDeleteCategory(category)
        })

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // 搜索功能
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val keyword = s?.toString()?.trim() ?: ""
                if (keyword.isEmpty()) {
                    adapter.updateData(allCategories)
                } else {
                    viewLifecycleOwner.lifecycleScope.launch {
                        val results = ManualRepository.search(requireContext(), keyword)
                        val filteredCategories = results.map { it.first }.distinct()
                        adapter.updateData(filteredCategories)
                    }
                }
            }
        })

        // 新增 FAB：选择新增章节或新增大类
        binding.fabAddSection.setOnClickListener {
            showAddChoiceDialog()
        }

        loadCategories()
    }

    override fun onResume() {
        super.onResume()
        loadCategories()
    }

    private fun loadCategories() {
        viewLifecycleOwner.lifecycleScope.launch {
            allCategories = ManualRepository.getCategories(requireContext())
            val keyword = binding.etSearch.text?.toString()?.trim() ?: ""
            if (keyword.isEmpty()) {
                adapter.updateData(allCategories)
            } else {
                val results = ManualRepository.search(requireContext(), keyword)
                adapter.updateData(results.map { it.first }.distinct())
            }
        }
    }

    private fun showAddChoiceDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("新增内容")
            .setItems(arrayOf("新增章节", "新增大类")) { _, which ->
                when (which) {
                    0 -> showAddSectionDialog()
                    1 -> showAddCategoryDialog()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showAddCategoryDialog() {
        val titleInput = EditText(requireContext()).apply {
            hint = "请输入大类名称"
            setSingleLine()
            setPadding(48, 24, 48, 24)
        }
        AlertDialog.Builder(requireContext())
            .setTitle("新增大类")
            .setView(titleInput)
            .setPositiveButton("创建") { _, _ ->
                val title = titleInput.text?.toString()?.trim()
                if (title.isNullOrEmpty()) {
                    android.widget.Toast.makeText(requireContext(), "请输入大类名称", android.widget.Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                viewLifecycleOwner.lifecycleScope.launch {
                    ManualRepository.addUserCategory(requireContext(), title)
                    loadCategories()
                    android.widget.Toast.makeText(requireContext(), "大类「$title」已创建", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun confirmDeleteCategory(category: ManualCategory) {
        val isUser = ManualRepository.isUserCategory(category.id)
        val message = if (isUser) {
            "确定删除大类「${category.title}」吗？该大类下的所有章节也将被删除，不可恢复。"
        } else {
            "确定从列表中移除内置大类「${category.title}」吗？该大类下的所有章节将同时隐藏。"
        }
        AlertDialog.Builder(requireContext())
            .setTitle("删除大类")
            .setMessage(message)
            .setPositiveButton("确定") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    if (isUser) {
                        ManualRepository.deleteUserCategory(requireContext(), category.id)
                    } else {
                        ManualRepository.markBuiltInCategoryDeleted(requireContext(), category.id)
                    }
                    loadCategories()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showAddSectionDialog() {
        val categoryTitles = allCategories.map { it.title }.toTypedArray()
        var selectedCategoryIndex = 0

        val titleInput = EditText(requireContext()).apply {
            hint = "请输入章节标题"
            setSingleLine()
            setPadding(48, 24, 48, 24)
        }

        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 16, 32, 0)
            addView(
                android.widget.TextView(requireContext()).apply {
                    text = "选择分类"
                    textSize = 14f
                    setTextColor(resources.getColor(R.color.text_secondary, null))
                    setPadding(16, 8, 16, 8)
                }
            )
            addView(
                android.widget.Spinner(requireContext()).apply {
                    adapter = android.widget.ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        categoryTitles
                    )
                    onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                            selectedCategoryIndex = position
                        }
                        override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                    }
                }
            )
            addView(
                android.widget.TextView(requireContext()).apply {
                    text = "章节标题"
                    textSize = 14f
                    setTextColor(resources.getColor(R.color.text_secondary, null))
                    setPadding(16, 16, 16, 4)
                }
            )
            addView(titleInput)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("新增章节")
            .setView(container)
            .setPositiveButton("创建并编辑") { _, _ ->
                val title = titleInput.text?.toString()?.trim()
                if (title.isNullOrEmpty()) {
                    android.widget.Toast.makeText(requireContext(), "请输入章节标题", android.widget.Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val category = allCategories[selectedCategoryIndex]
                viewLifecycleOwner.lifecycleScope.launch {
                    val dbId = ManualRepository.addUserSection(requireContext(), category.id, title)
                    val sectionId = ManualRepository.buildUserSectionId(dbId)
                    val intent = Intent(requireContext(), ManualEditActivity::class.java).apply {
                        putExtra(ManualEditActivity.EXTRA_SECTION_ID, sectionId)
                        putExtra(ManualEditActivity.EXTRA_SECTION_TITLE, title)
                        putExtra(ManualEditActivity.EXTRA_INITIAL_HTML, "<p>在此输入内容...</p>")
                        putExtra(ManualEditActivity.EXTRA_IS_USER_SECTION, true)
                    }
                    editLauncher.launch(intent)
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
