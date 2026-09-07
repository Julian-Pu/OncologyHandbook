package com.oncology.handbook.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.oncology.handbook.data.entity.ContentBlock
import com.oncology.handbook.databinding.ItemBlockMediaViewBinding
import com.oncology.handbook.databinding.ItemBlockTextViewBinding
import java.io.File

class BlockViewerAdapter(
    private val blocks: List<ContentBlock>,
    private val onImageClick: (filePath: String) -> Unit,
    private val onVideoClick: (filePath: String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_TEXT = 0
        private const val TYPE_MEDIA = 1
    }

    inner class TextViewHolder(val binding: ItemBlockTextViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(block: ContentBlock) {
            binding.tvBlockText.text = block.text
        }
    }

    inner class MediaViewHolder(val binding: ItemBlockMediaViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(block: ContentBlock) {
            val file = File(block.filePath)
            if (block.type == ContentBlock.TYPE_IMAGE) {
                binding.ivPlayOverlay.visibility = android.view.View.GONE
                binding.ivMedia.load(file) { crossfade(true) }
                binding.root.setOnClickListener { onImageClick(block.filePath) }
            } else {
                binding.ivPlayOverlay.visibility = android.view.View.VISIBLE
                binding.ivMedia.setBackgroundColor(android.graphics.Color.BLACK)
                binding.ivMedia.setImageDrawable(null)
                binding.root.setOnClickListener { onVideoClick(block.filePath) }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (blocks[position].type == ContentBlock.TYPE_TEXT) TYPE_TEXT else TYPE_MEDIA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_TEXT) {
            TextViewHolder(ItemBlockTextViewBinding.inflate(inflater, parent, false))
        } else {
            MediaViewHolder(ItemBlockMediaViewBinding.inflate(inflater, parent, false))
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
