package com.oncology.handbook.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.oncology.handbook.R
import com.oncology.handbook.data.entity.ContentBlock
import com.oncology.handbook.databinding.ItemBlockMediaEditBinding
import com.oncology.handbook.databinding.ItemBlockTextEditBinding
import com.oncology.handbook.ui.content.EditorBlock
import java.io.File

class BlockEditorAdapter(
    private val blocks: MutableList<EditorBlock>,
    private val onBlockDeleted: (position: Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_TEXT = 0
        private const val TYPE_MEDIA = 1
    }

    inner class TextViewHolder(val binding: ItemBlockTextEditBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION && pos < blocks.size) {
                    blocks[pos].text = s?.toString() ?: ""
                }
            }
        }

        fun bind(block: EditorBlock) {
            binding.etBlockText.removeTextChangedListener(textWatcher)
            binding.etBlockText.setText(block.text)
            binding.etBlockText.addTextChangedListener(textWatcher)
            binding.btnDeleteBlock.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onBlockDeleted(pos)
                }
            }
        }
    }

    inner class MediaViewHolder(val binding: ItemBlockMediaEditBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(block: EditorBlock) {
            val file = File(block.filePath)
            if (block.type == ContentBlock.TYPE_IMAGE) {
                binding.ivPlayOverlay.visibility = android.view.View.GONE
                binding.ivMediaPreview.load(file) {
                    crossfade(true)
                }
            } else {
                // 视频：显示深色背景 + 播放图标 + 文件名
                binding.ivPlayOverlay.visibility = android.view.View.VISIBLE
                binding.ivMediaPreview.setBackgroundColor(android.graphics.Color.BLACK)
                binding.ivMediaPreview.setImageDrawable(null)
            }
            binding.tvMediaName.text = file.name
            binding.btnDeleteBlock.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onBlockDeleted(pos)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (blocks[position].type == ContentBlock.TYPE_TEXT) TYPE_TEXT else TYPE_MEDIA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_TEXT) {
            TextViewHolder(ItemBlockTextEditBinding.inflate(inflater, parent, false))
        } else {
            MediaViewHolder(ItemBlockMediaEditBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val block = blocks[position]
        when (holder) {
            is TextViewHolder -> holder.bind(block)
            is MediaViewHolder -> holder.bind(block)
        }
    }

    override fun getItemCount(): Int = blocks.size
}
