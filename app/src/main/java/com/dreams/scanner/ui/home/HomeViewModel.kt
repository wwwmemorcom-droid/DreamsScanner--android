package com.dreams.scanner.ui.home

import androidx.lifecycle.viewModelScope
import com.dreams.scanner.data.DocumentRepository
import com.dreams.scanner.data.SettingsRepository
import com.dreams.scanner.data.SortOrder
import com.dreams.scanner.data.db.DocumentEntity
import com.dreams.scanner.ui.LibraryViewModel
import com.dreams.scanner.ui.sortedByOrder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    repo: DocumentRepository,
    private val settings: SettingsRepository
) : LibraryViewModel(repo, importFolderId = null) {

    private val whileSubscribed = SharingStarted.WhileSubscribed(5_000)

    val sortOrder: StateFlow<SortOrder> = settings.sortOrder
        .stateIn(viewModelScope, whileSubscribed, SortOrder.DATE)

    fun setSortOrder(order: SortOrder) {
        viewModelScope.launch { settings.setSortOrder(order) }
    }

    /** All documents, in the order chosen by the user. */
    val documents: StateFlow<List<DocumentEntity>> =
        combine(repo.observeDocuments(), sortOrder) { docs, order -> docs.sortedByOrder(order) }
            .stateIn(viewModelScope, whileSubscribed, emptyList())

    /** The most recently modified documents (the DAO already orders by updatedAt DESC). */
    val recent: StateFlow<List<DocumentEntity>> =
        repo.observeDocuments().map { it.take(RECENT_COUNT) }
            .stateIn(viewModelScope, whileSubscribed, emptyList())

    val favorites: StateFlow<List<DocumentEntity>> =
        combine(repo.observeFavorites(), sortOrder) { docs, order -> docs.sortedByOrder(order) }
            .stateIn(viewModelScope, whileSubscribed, emptyList())

    private val query = MutableStateFlow("")

    fun setQuery(value: String) {
        query.value = value
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<DocumentEntity>> =
        combine(
            query.debounce(200).map { it.trim() }.distinctUntilChanged().flatMapLatest { q ->
                if (q.isEmpty()) flowOf(emptyList()) else repo.search(q)
            },
            sortOrder
        ) { docs, order -> docs.sortedByOrder(order) }
            .stateIn(viewModelScope, whileSubscribed, emptyList())

    private companion object {
        const val RECENT_COUNT = 6
    }
}
