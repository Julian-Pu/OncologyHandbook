package com.oncology.handbook.ui.bookshelf

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.oncology.handbook.databinding.ActivityPdfViewerBinding
import java.io.File

class PdfViewerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PDF_NAME = "extra_pdf_name"
        const val EXTRA_PDF_TITLE = "extra_pdf_title"
    }

    private lateinit var binding: ActivityPdfViewerBinding
    private var totalPages = 0
    private var currentPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pdfName = intent.getStringExtra(EXTRA_PDF_NAME) ?: run {
            Toast.makeText(this, "未指定PDF文件", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val title = intent.getStringExtra(EXTRA_PDF_TITLE) ?: pdfName

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            this.title = title
        }

        loadPdfFromAssets(pdfName)

        binding.btnPrev.setOnClickListener {
            if (currentPage > 0) {
                binding.pdfView.jumpTo(currentPage - 1, true)
            }
        }
        binding.btnNext.setOnClickListener {
            if (currentPage < totalPages - 1) {
                binding.pdfView.jumpTo(currentPage + 1, true)
            }
        }
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && totalPages > 0) {
                    binding.pdfView.jumpTo(progress, true)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun loadPdfFromAssets(fileName: String) {
        binding.progressBar.visibility = View.VISIBLE
        try {
            binding.pdfView.fromAsset("books/$fileName")
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .defaultPage(0)
                .onLoad(OnLoadCompleteListener { nbPages ->
                    totalPages = nbPages
                    binding.progressBar.visibility = View.GONE
                    binding.seekBar.max = nbPages - 1
                    binding.tvPageInfo.text = "1 / $nbPages"
                })
                .onPageChange(OnPageChangeListener { page, pageCount ->
                    currentPage = page
                    totalPages = pageCount
                    binding.tvPageInfo.text = "${page + 1} / $pageCount"
                    binding.seekBar.progress = page
                })
                .load()
        } catch (e: Exception) {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, "加载PDF失败：${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
