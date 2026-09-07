package com.oncology.handbook.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.oncology.handbook.R
import com.oncology.handbook.databinding.FragmentHomeBinding
import com.oncology.handbook.ui.bookshelf.PdfViewerActivity
import com.oncology.handbook.ui.content.AddContentActivity
import com.oncology.handbook.util.ManualContent

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 快捷入口卡片
        binding.cardEmergency.setOnClickListener {
            val intent = Intent(requireContext(), PdfViewerActivity::class.java).apply {
                putExtra(PdfViewerActivity.EXTRA_PDF_NAME, "急诊医生心电图手册（第一册）（高清中文版）.pdf")
                putExtra(PdfViewerActivity.EXTRA_PDF_TITLE, "急诊医生心电图手册（第一册）")
            }
            startActivity(intent)
        }

        binding.cardEcglib.setOnClickListener {
            val intent = Intent(requireContext(), PdfViewerActivity::class.java).apply {
                putExtra(PdfViewerActivity.EXTRA_PDF_NAME, "心电图从入门到精通.pdf")
                putExtra(PdfViewerActivity.EXTRA_PDF_TITLE, "心电图从入门到精通")
            }
            startActivity(intent)
        }

        binding.cardAddNote.setOnClickListener {
            startActivity(Intent(requireContext(), AddContentActivity::class.java))
        }

        // 手册卡片：跳转到手册标签页
        binding.cardManual.setOnClickListener {
            findNavController().navigate(R.id.manualFragment)
        }

        // 显示手册概览
        val categoryCount = ManualContent.categories.size
        val sectionCount = ManualContent.categories.sumOf { it.sections.size }
        binding.tvManualOverview.text = "内置手册 $categoryCount 大模块，共 $sectionCount 个章节\n含3本PDF参考书籍，支持离线阅读"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
