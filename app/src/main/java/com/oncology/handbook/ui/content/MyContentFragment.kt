package com.oncology.handbook.ui.content

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.oncology.handbook.adapter.ContentAdapter
import com.oncology.handbook.data.entity.UserContent
import com.oncology.handbook.databinding.FragmentMyContentBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MyContentFragment : Fragment() {

    private var _binding: FragmentMyContentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ContentViewModel by viewModels()
    private lateinit var adapter: ContentAdapter

    // 缓存全量数据，搜索时在内存中过滤
    private var allContentsCache: List<UserContent> = emptyList()
    private var currentKeyword: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyContentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ContentAdapter(
            onItemClick = { content ->
                val intent = Intent(requireContext(), ContentDetailActivity::class.java).apply {
                    putExtra(ContentDetailActivity.EXTRA_CONTENT_ID, content.id)
                }
                startActivity(intent)
            },
            onDeleteClick = { content ->
                viewModel.deleteContent(content)
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), AddContentActivity::class.java))
        }

        // 搜索
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                currentKeyword = s?.toString()?.trim() ?: ""
                applyFilter()
            }
        })

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allContents.collectLatest { contents ->
                allContentsCache = contents
                applyFilter()
            }
        }
    }

    private fun applyFilter() {
        val filtered = if (currentKeyword.isEmpty()) {
            allContentsCache
        } else {
            allContentsCache.filter {
                it.title.contains(currentKeyword, true) ||
                it.content.contains(currentKeyword, true) ||
                it.category.contains(currentKeyword, true)
            }
        }
        adapter.submitList(filtered)
        binding.tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        binding.tvCount.text = "共 ${allContentsCache.size} 条记录"
    }

    override fun onResume() {
        super.onResume()
        // 从详情页返回时刷新
        applyFilter()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
