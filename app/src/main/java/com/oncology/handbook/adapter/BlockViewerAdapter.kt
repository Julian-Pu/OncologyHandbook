package com.oncology.handbook.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.oncology.handbook.data.entity.ContentBlock
import com.oncology.handbook.databinding.ItemBlockMediaViewBinding
import com.oncology.handbook.databinding.ItemBlockTextViewBinding

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
            if (block.type == ContentBlock.TYPE_IMAGE) {
                // 图片：显示 ImageView，点击放大查看
                binding.ivMedia.visibility = View.VISIBLE
                binding.videoView.visibility = View.GONE
                binding.btnFullscreen.visibility = View.GONE
                binding.ivPlayOverlay.visibility = View.GONE
                binding.ivMedia.load(java.io.File(block.filePath)) { crossfade(true) }
                binding.root.setOnClickListener { onImageClick(block.filePath) }
            } else {
                // 视频：显示深色背景+播放图标，点击跳转全屏播放
                binding.ivMedia.visibility = View.VISIBLE
                binding.ivMedia.setImageDrawable(null)
                binding.ivMedia.setBackgroundColor(android.graphics.Color.BLACK)
                binding.videoView.visibility = View.GONE
                binding.btnFullscreen.visibility = View.GONE
                binding.ivPlayOverlay.visibility = View.VISIBLE
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
