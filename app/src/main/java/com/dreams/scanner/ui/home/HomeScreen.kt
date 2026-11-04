@file:OptIn(ExperimentalMaterial3Api::class)

package com.dreams.scanner.ui.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dreams.scanner.R
import com.dreams.scanner.data.SortOrder
import com.dreams.scanner.data.db.DocumentEntity
import com.dreams.scanner.data.db.FolderWithCount
import com.dreams.scanner.ui.UiEvent
import com.dreams.scanner.ui.components.DocDialog
import com.dreams.scanner.ui.components.DocumentDialogHost
import com.dreams.scanner.ui.components.ConfirmDialog
import com.dreams.scanner.ui.components.EmptyState
import com.dreams.scanner.ui.components.FolderRow
import com.dreams.scanner.ui.components.ImportingDialog
import com.dreams.scanner.ui.components.NameDialog
import com.dreams.scanner.ui.components.Thumbnail
import com.dreams.scanner.ui.components.documentItems

@Composable
fun HomeScreen(
    vm: HomeViewModel,
    onOpenDocument: (Long) -> Unit,
    onOpenFolder: (Long) -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val documents by vm.documents.collectAsStateWithLifecycle()
    val recent by vm.recent.collectAsStateWithLifecycle()
    val favorites by vm.favorites.collectAsStateWithLifecycle()
    val folders by vm.folders.collectAsStateWithLifecycle()
    val results by vm.searchResults.collectAsStateWithLifecycle()
    val sort by vm.sortOrder.collectAsStateWithLifecycle()
    val importing by vm.importing.collectAsStateWithLifecycle()

    var tab by rememberSaveable { mutableIntStateOf(0) }
    var query by rememberSaveable { mutableStateOf("") }
    var docDialog by remember { mutableStateOf<DocDialog?>(null) }
    var showNewFolder by remember { mutableStateOf(false) }
    var renameFolder by remember { mutableStateOf<FolderWithCount?>(null) }
    var deleteFolder by remember { mutableStateOf<FolderWithCount?>(null) }
    val snackbar = remember { SnackbarHostState() }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
        vm.importImages(uris)
    }

    LaunchedEffect(query) { vm.setQuery(query) }

    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                is UiEvent.Message ->
                    snackbar.showSnackbar(context.getString(event.res, *event.args.toTypedArray()))

                is UiEvent.OpenDocument -> onOpenDocument(event.id)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                icon = { Icon(Icons.Filled.PhotoLibrary, contentDescription = null) },
                text = { Text(stringResource(R.string.import_from_gallery)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 8.dp, top = 8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings))
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                placeholder = { Text(stringResource(R.string.search_hint)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.clear))
                        }
                    }
                }
            )

            val searching = query.isNotBlank()

            if (!searching) {
                TabRow(
                    selectedTabIndex = tab,
                    containerColor = Color.Transparent
                ) {
                    Tab(
                        selected = tab == 0,
                        onClick = { tab = 0 },
                        text = { Text(stringResource(R.string.tab_documents)) }
                    )
                    Tab(
                        selected = tab == 1,
                        onClick = { tab = 1 },
                        text = { Text(stringResource(R.string.tab_folders)) }
                    )
                    Tab(
                        selected = tab == 2,
                        onClick = { tab = 2 },
                        text = { Text(stringResource(R.string.tab_favorites)) }
                    )
                }
            }

            val listPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp)

            when {
                searching -> {
                    if (results.isEmpty()) {
                        EmptyState(
                            Icons.Filled.Search,
                            stringResource(R.string.no_results_title),
                            stringResource(R.string.no_results_body)
                        )
                    } else {
                        LazyColumn(
                            contentPadding = listPadding,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            documentItems(
                                docs = results,
                                onOpen = { onOpenDocument(it.id) },
                                onToggleFavorite = vm::toggleFavorite,
                                onDialog = { docDialog = it }
                            )
                        }
                    }
                }

                tab == 0 -> {
                    if (documents.isEmpty()) {
                        EmptyState(
                            Icons.Filled.Description,
                            stringResource(R.string.empty_documents_title),
                            stringResource(R.string.empty_documents_body)
                        )
                    } else {
                        LazyColumn(
                            contentPadding = listPadding,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (recent.size > 1) {
                                item(key = "recent_header") {
                                    SectionTitle(stringResource(R.string.recent))
                                }
                                item(key = "recent_row") {
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        items(recent, key = { it.id }) { doc ->
                                            RecentCard(doc) { onOpenDocument(doc.id) }
                                        }
                                    }
                                }
                            }
                            item(key = "all_header") {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    SectionTitle(
                                        stringResource(R.string.all_documents),
                                        Modifier.weight(1f)
                                    )
                                    SortMenu(sort, vm::setSortOrder)
                                }
                            }
                            documentItems(
                                docs = documents,
                                onOpen = { onOpenDocument(it.id) },
                                onToggleFavorite = vm::toggleFavorite,
                                onDialog = { docDialog = it }
                            )
                        }
                    }
                }

                tab == 1 -> {
                    LazyColumn(
                        contentPadding = listPadding,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item(key = "new_folder") {
                            OutlinedButton(
                                onClick = { showNewFolder = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.CreateNewFolder, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.new_folder))
                            }
                        }
                        if (folders.isEmpty()) {
                            item(key = "folders_empty") {
                                EmptyState(
                                    Icons.Filled.Folder,
                                    stringResource(R.string.empty_folders_title),
                                    stringResource(R.string.empty_folders_body)
                                )
                            }
                        } else {
                            items(folders, key = { it.id }) { folder ->
                                FolderRow(
                                    folder = folder,
                                    onClick = { onOpenFolder(folder.id) },
                                    onRename = { renameFolder = folder },
                                    onDelete = { deleteFolder = folder }
                                )
                            }
                        }
                    }
                }

                else -> {
                    if (favorites.isEmpty()) {
                        EmptyState(
                            Icons.Filled.Star,
                            stringResource(R.string.empty_favorites_title),
                            stringResource(R.string.empty_favorites_body)
                        )
                    } else {
                        LazyColumn(
                            contentPadding = listPadding,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            documentItems(
                                docs = favorites,
                                onOpen = { onOpenDocument(it.id) },
                                onToggleFavorite = vm::toggleFavorite,
                                onDialog = { docDialog = it }
                            )
                        }
                    }
                }
            }
        }
    }

    // ---- Dialogs ----
    DocumentDialogHost(
        dialog = docDialog,
        folders = folders,
        onDismiss = { docDialog = null },
        onRename = vm::renameDocument,
        onMove = vm::moveDocument,
        onDelete = vm::deleteDocument
    )

    if (showNewFolder) {
        NameDialog(
            title = stringResource(R.string.new_folder),
            label = stringResource(R.string.folder_name),
            initial = "",
            confirmLabel = stringResource(R.string.create),
            onDismiss = { showNewFolder = false },
            onConfirm = { vm.createFolder(it); showNewFolder = false }
        )
    }

    renameFolder?.let { folder ->
        NameDialog(
            title = stringResource(R.string.rename),
            label = stringResource(R.string.folder_name),
            initial = folder.name,
            confirmLabel = stringResource(R.string.save),
            onDismiss = { renameFolder = null },
            onConfirm = { vm.renameFolder(folder.id, it); renameFolder = null }
        )
    }

    deleteFolder?.let { folder ->
        ConfirmDialog(
            title = stringResource(R.string.delete_folder_title),
            message = stringResource(R.string.delete_folder_message, folder.name),
            confirmLabel = stringResource(R.string.delete),
            onDismiss = { deleteFolder = null },
            onConfirm = { vm.deleteFolder(folder.id); deleteFolder = null }
        )
    }

    if (importing) ImportingDialog()
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier.padding(vertical = 4.dp),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun RecentCard(doc: DocumentEntity, onClick: () -> Unit) {
    Column(
        Modifier
            .width(104.dp)
            .clickable(onClick = onClick)
    ) {
        Thumbnail(doc.coverPath, Modifier.size(width = 104.dp, height = 136.dp))
        Spacer(Modifier.height(6.dp))
        Text(
            doc.name,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SortMenu(current: SortOrder, onSelect: (SortOrder) -> Unit) {
    var open by remember { mutableStateOf(false) }
    androidx.compose.foundation.layout.Box {
        TextButton(onClick = { open = true }) {
            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = stringResource(R.string.sort_by))
            Spacer(Modifier.width(6.dp))
            Text(
                stringResource(if (current == SortOrder.DATE) R.string.sort_date else R.string.sort_name)
            )
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.sort_date)) },
                onClick = { open = false; onSelect(SortOrder.DATE) }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.sort_name)) },
                onClick = { open = false; onSelect(SortOrder.NAME) }
            )
        }
    }
}
