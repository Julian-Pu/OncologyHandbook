package com.oncology.handbook.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.oncology.handbook.data.entity.UserContent
import com.oncology.handbook.databinding.ItemContentBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ContentAdapter(
    private val onItemClick: (UserContent) -> Unit,
    private val onDeleteClick: (UserContent) -> Unit
) : ListAdapter<UserContent, ContentAdapter.ContentViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<UserContent>() {
            override fun areItemsTheSame(oldItem: UserContent, newItem: UserContent) =
                oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: UserContent, newItem: UserContent) =
                oldItem == newItem
        }
    }

    inner class ContentViewHolder(val binding: ItemContentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(content: UserContent) {
            binding.tvTitle.text = content.title.ifEmpty { "无标题" }
            binding.tvPreview.text = content.content.ifEmpty { "（无文字内容）" }
            binding.tvCategory.text = content.category

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            binding.tvDate.text = dateFormat.format(Date(content.updatedAt))

            // 标记是否有图片/视频
            val hasImages = content.imagePaths.isNotEmpty()
            val hasVideo = content.videoPath.isNotEmpty()
            binding.ivImageIcon.visibility = if (hasImages) android.view.View.VISIBLE else android.view.View.GONE
            binding.ivVideoIcon.visibility = if (hasVideo) android.view.View.VISIBLE else android.view.View.GONE

            binding.root.setOnClickListener { onItemClick(content) }
            binding.btnDelete.setOnClickListener { onDeleteClick(content) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentViewHolder {
        val binding = ItemContentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
