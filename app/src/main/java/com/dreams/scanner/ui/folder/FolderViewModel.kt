package com.dreams.scanner.ui.folder

import androidx.lifecycle.viewModelScope
import com.dreams.scanner.data.DocumentRepository
import com.dreams.scanner.data.SortOrder
import com.dreams.scanner.data.db.DocumentEntity
import com.dreams.scanner.data.db.FolderEntity
import com.dreams.scanner.ui.LibraryViewModel
import com.dreams.scanner.ui.sortedByOrder
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** `loaded` distinguishes "still loading" from "folder no longer exists" (e.g. just deleted). */
data class FolderState(val loaded: Boolean = false, val folder: FolderEntity? = null)

class FolderViewModel(
    repo: DocumentRepository,
    folderId: Long
) : LibraryViewModel(repo, importFolderId = folderId) {

    val state: StateFlow<FolderState> = repo.observeFolder(folderId)
        .map { FolderState(loaded = true, folder = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FolderState())

    val documents: StateFlow<List<DocumentEntity>> = repo.observeInFolder(folderId)
        .map { it.sortedByOrder(SortOrder.DATE) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
