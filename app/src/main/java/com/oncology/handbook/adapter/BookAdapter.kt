package com.oncology.handbook.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.oncology.handbook.databinding.ItemBookBinding
import com.oncology.handbook.ui.bookshelf.BookItem

class BookAdapter(
    private val books: List<BookItem>,
    private val onItemClick: (BookItem) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    inner class BookViewHolder(val binding: ItemBookBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(book: BookItem) {
            binding.tvTitle.text = book.title
            binding.tvDescription.text = book.description
            binding.tvFormat.text = "PDF"
            binding.root.setOnClickListener { onItemClick(book) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(books[position])
    }

    override fun getItemCount() = books.size
}
