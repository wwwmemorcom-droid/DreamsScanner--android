package com.dreams.scanner.ui

import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dreams.scanner.R
import com.dreams.scanner.data.DocumentRepository
import com.dreams.scanner.data.SortOrder
import com.dreams.scanner.data.db.DocumentEntity
import com.dreams.scanner.data.db.FolderWithCount
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiEvent {
    data class Message(@StringRes val res: Int, val args: List<Any> = emptyList()) : UiEvent
    data class OpenDocument(val id: Long) : UiEvent
}

/** Simple factory so ViewModels with constructor arguments can be created without a DI framework. */
fun <VM : ViewModel> factoryOf(builder: () -> VM): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = builder() as T
    }

fun List<DocumentEntity>.sortedByOrder(order: SortOrder): List<DocumentEntity> = when (order) {
    SortOrder.DATE -> sortedByDescending { it.updatedAt }
    SortOrder.NAME -> sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
}

/**
 * Shared behaviour of every screen that lists documents: import, rename, move, delete,
 * favorite, and folder management.
 *
 * @param importFolderId folder that imported documents are placed in (null = no folder).
 */
abstract class LibraryViewModel(
    protected val repo: DocumentRepository,
    private val importFolderId: Long?
) : ViewModel() {

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val _importing = MutableStateFlow(false)
    val importing: StateFlow<Boolean> = _importing.asStateFlow()

    val folders: StateFlow<List<FolderWithCount>> = repo.observeFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun importImages(uris: List<Uri>) {
        if (uris.isEmpty() || _importing.value) return
        _importing.value = true
        viewModelScope.launch {
            try {
                val result = repo.importImages(uris, importFolderId)
                val id = result.documentId
                if (id == null) {
                    _events.send(UiEvent.Message(R.string.import_failed))
                } else {
                    if (result.failed > 0) {
                        _events.send(UiEvent.Message(R.string.import_partial, listOf(result.imported, result.failed)))
                    }
                    _events.send(UiEvent.OpenDocument(id))
                }
            } finally {
                _importing.value = false
            }
        }
    }

    fun toggleFavorite(doc: DocumentEntity) {
        viewModelScope.launch { repo.setFavorite(doc.id, !doc.isFavorite) }
    }

    fun renameDocument(doc: DocumentEntity, name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { repo.renameDocument(doc.id, name) }
    }

    fun moveDocument(doc: DocumentEntity, folderId: Long?) {
        viewModelScope.launch { repo.moveDocument(doc.id, folderId) }
    }

    fun deleteDocument(doc: DocumentEntity) {
        viewModelScope.launch { repo.deleteDocument(doc.id) }
    }

    fun createFolder(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { repo.createFolder(name) }
    }

    fun renameFolder(id: Long, name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { repo.renameFolder(id, name) }
    }

    fun deleteFolder(id: Long) {
        viewModelScope.launch { repo.deleteFolder(id) }
    }
}
