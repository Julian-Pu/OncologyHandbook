package com.oncology.handbook.ui.manual

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.oncology.handbook.adapter.ManualCategoryAdapter
import com.oncology.handbook.databinding.FragmentManualBinding
import com.oncology.handbook.util.ManualContent

class ManualFragment : Fragment() {

    private var _binding: FragmentManualBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManualBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ManualCategoryAdapter(ManualContent.categories) { category, section ->
            val intent = Intent(requireContext(), ManualDetailActivity::class.java).apply {
                putExtra(ManualDetailActivity.EXTRA_CATEGORY_ID, category.id)
                putExtra(ManualDetailActivity.EXTRA_SECTION_ID, section.id)
                putExtra(ManualDetailActivity.EXTRA_TITLE, section.title)
            }
            startActivity(intent)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // 搜索功能
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val keyword = s?.toString()?.trim() ?: ""
                if (keyword.isEmpty()) {
                    adapter.updateData(ManualContent.categories)
                } else {
                    val results = ManualContent.search(keyword)
                    val filteredCategories = results.map { it.first }.distinct()
                    // 简化：显示匹配的分类和章节
                    adapter.updateData(filteredCategories)
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
