package com.oncology.handbook.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.oncology.handbook.data.entity.ContentBlock
import com.oncology.handbook.databinding.ItemBlockMediaViewBinding
import com.oncology.handbook.databinding.ItemBlockTextViewBinding

class BlockViewerAdapter(
    private val blocks: List<ContentBlock>,
    private val onImageClick: (filePath: String) -> Unit,
    private val onVideoFullscreen: (filePath: String) -> Unit,
    private val onVideoViewReady: (VideoView) -> Unit
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
                // 图片：显示 ImageView，隐藏 VideoView 和全屏按钮
                binding.ivMedia.visibility = View.VISIBLE
                binding.videoView.visibility = View.GONE
                binding.btnFullscreen.visibility = View.GONE
                binding.ivPlayOverlay.visibility = View.GONE
                binding.ivMedia.load(java.io.File(block.filePath)) { crossfade(true) }
                binding.root.setOnClickListener { onImageClick(block.filePath) }
            } else {
                // 视频：显示 VideoView 内联播放，隐藏 ImageView，显示全屏按钮
                binding.ivMedia.visibility = View.GONE
                binding.ivPlayOverlay.visibility = View.GONE
                binding.videoView.visibility = View.VISIBLE
                binding.btnFullscreen.visibility = View.VISIBLE
                binding.root.setOnClickListener(null)

                binding.videoView.setVideoPath(block.filePath)
                binding.videoView.setOnPreparedListener { mp ->
                    mp.isLooping = true
                    // centerCrop: 缩放视频填充整个容器，超出部分由父布局裁剪
                    val videoWidth = mp.videoWidth
                    val videoHeight = mp.videoHeight
                    val container = binding.videoView.parent as View
                    container.post {
                        val cw = container.width
                        val ch = container.height
                        if (cw > 0 && ch > 0 && videoWidth > 0 && videoHeight > 0) {
                            val scale = maxOf(
                                cw.toFloat() / videoWidth,
                                ch.toFloat() / videoHeight
                            )
                            val lp = binding.videoView.layoutParams as FrameLayout.LayoutParams
                            lp.width = (videoWidth * scale).toInt()
                            lp.height = (videoHeight * scale).toInt()
                            binding.videoView.layoutParams = lp
                        }
                    }
                    binding.videoView.start()
                }
                binding.videoView.setOnErrorListener { _, _, _ ->
                    binding.ivPlayOverlay.visibility = View.VISIBLE
                    true
                }

                // 通知 Activity 管理此 VideoView 生命周期
                onVideoViewReady(binding.videoView)

                binding.btnFullscreen.setOnClickListener {
                    onVideoFullscreen(block.filePath)
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
