package com.oncology.handbook.ui.content

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.oncology.handbook.App
import com.oncology.handbook.data.entity.UserContent
import com.oncology.handbook.data.repository.ContentRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ContentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ContentRepository((application as App).database)

    val allContents = repository.getAllContents()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val categories = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun insertContent(content: UserContent, onResult: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insertContent(content)
            onResult(id)
        }
    }

    fun updateContent(content: UserContent) {
        viewModelScope.launch {
            repository.updateContent(content)
        }
    }

    fun deleteContent(content: UserContent) {
        viewModelScope.launch {
            repository.deleteContent(content)
        }
    }

    fun getContentById(id: Long, onResult: (UserContent?) -> Unit) {
        viewModelScope.launch {
            val content = repository.getContentById(id)
            onResult(content)
        }
    }
}
