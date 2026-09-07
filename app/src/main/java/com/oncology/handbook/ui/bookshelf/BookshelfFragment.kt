package com.oncology.handbook.ui.bookshelf

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.oncology.handbook.adapter.BookAdapter
import com.oncology.handbook.databinding.FragmentBookshelfBinding

data class BookItem(
    val fileName: String,
    val title: String,
    val description: String
)

class BookshelfFragment : Fragment() {

    private var _binding: FragmentBookshelfBinding? = null
    private val binding get() = _binding!!

    private val books = listOf(
        BookItem(
            "心电图从入门到精通.pdf",
            "心电图从入门到精通",
            "系统学习心电图基础与临床应用，适合入门到进阶"
        ),
        BookItem(
            "急诊医生心电图手册（第一册）（高清中文版）.pdf",
            "急诊医生心电图手册（第一册）",
            "急诊场景下心电图书谱与判读要点"
        ),
        BookItem(
            "急诊医生心电图手册（第二册）（高清中文版）.pdf",
            "急诊医生心电图手册（第二册）",
            "复杂心律失常与危急心电图病例解析"
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookshelfBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = BookAdapter(books) { book ->
            val intent = Intent(requireContext(), PdfViewerActivity::class.java).apply {
                putExtra(PdfViewerActivity.EXTRA_PDF_NAME, book.fileName)
                putExtra(PdfViewerActivity.EXTRA_PDF_TITLE, book.title)
            }
            startActivity(intent)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
