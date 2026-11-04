package com.dreams.scanner.ui.document

import androidx.lifecycle.viewModelScope
import com.dreams.scanner.data.DocumentRepository
import com.dreams.scanner.data.db.DocumentEntity
import com.dreams.scanner.data.db.PageEntity
import com.dreams.scanner.ui.LibraryViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class DocumentState(val loaded: Boolean = false, val document: DocumentEntity? = null)

class DocumentViewModel(
    repo: DocumentRepository,
    documentId: Long
) : LibraryViewModel(repo, importFolderId = null) {

    val state: StateFlow<DocumentState> = repo.observeDocument(documentId)
        .map { DocumentState(loaded = true, document = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DocumentState())

    val pages: StateFlow<List<PageEntity>> = repo.observePages(documentId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
