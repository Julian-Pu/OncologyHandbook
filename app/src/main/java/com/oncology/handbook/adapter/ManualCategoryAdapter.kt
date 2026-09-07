package com.oncology.handbook.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.oncology.handbook.databinding.ItemManualCategoryBinding
import com.oncology.handbook.util.ManualCategory
import com.oncology.handbook.util.ManualSection

class ManualCategoryAdapter(
    private var categories: List<ManualCategory>,
    private val onSectionClick: (ManualCategory, ManualSection) -> Unit,
    private val onCategoryLongClick: (ManualCategory) -> Unit = {}
) : RecyclerView.Adapter<ManualCategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(val binding: ItemManualCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: ManualCategory) {
            binding.tvCategoryTitle.text = category.title
            binding.tvSectionCount.text = "${category.sections.size} 个章节"

            val sectionAdapter = SectionAdapter(category.sections) { section ->
                onSectionClick(category, section)
            }
            binding.recyclerViewSections.layoutManager = LinearLayoutManager(binding.root.context)
            binding.recyclerViewSections.adapter = sectionAdapter

            // 长按分类标题 → 删除分类
            binding.tvCategoryTitle.setOnLongClickListener {
                onCategoryLongClick(category)
                true
            }
        }
    }

    inner class SectionAdapter(
        private val sections: List<ManualSection>,
        private val onClick: (ManualSection) -> Unit
    ) : RecyclerView.Adapter<SectionAdapter.SectionViewHolder>() {

        inner class SectionViewHolder(val binding: com.oncology.handbook.databinding.ItemManualSectionBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(section: ManualSection) {
                binding.tvSectionTitle.text = section.title
                binding.root.setOnClickListener { onClick(section) }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
            val binding = com.oncology.handbook.databinding.ItemManualSectionBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return SectionViewHolder(binding)
        }

        override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
            holder.bind(sections[position])
        }

        override fun getItemCount() = sections.size
    }

    fun updateData(newCategories: List<ManualCategory>) {
        categories = newCategories
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemManualCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount() = categories.size
}
