@file:OptIn(ExperimentalMaterial3Api::class)

package com.dreams.scanner.ui.folder

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dreams.scanner.R
import com.dreams.scanner.ui.UiEvent
import com.dreams.scanner.ui.components.ConfirmDialog
import com.dreams.scanner.ui.components.DocDialog
import com.dreams.scanner.ui.components.DocumentDialogHost
import com.dreams.scanner.ui.components.EmptyState
import com.dreams.scanner.ui.components.ImportingDialog
import com.dreams.scanner.ui.components.NameDialog
import com.dreams.scanner.ui.components.documentItems

@Composable
fun FolderScreen(
    vm: FolderViewModel,
    onBack: () -> Unit,
    onOpenDocument: (Long) -> Unit
) {
    val context = LocalContext.current
    val state by vm.state.collectAsStateWithLifecycle()
    val documents by vm.documents.collectAsStateWithLifecycle()
    val folders by vm.folders.collectAsStateWithLifecycle()
    val importing by vm.importing.collectAsStateWithLifecycle()

    var docDialog by remember { mutableStateOf<DocDialog?>(null) }
    var menuOpen by remember { mutableStateOf(false) }
    var showRename by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
        vm.importImages(uris)
    }

    // Once the folder is gone (deleted from here), leave the screen.
    LaunchedEffect(state) {
        if (state.loaded && state.folder == null) onBack()
    }

    LaunchedEffect(Unit) {
        vm.events.collect { event ->
            when (event) {
                is UiEvent.Message ->
                    snackbar.showSnackbar(context.getString(event.res, *event.args.toTypedArray()))

                is UiEvent.OpenDocument -> onOpenDocument(event.id)
            }
        }
    }

    val folder = state.folder

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Text(folder?.name.orEmpty(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if (folder != null) {
                        Box {
                            IconButton(onClick = { menuOpen = true }) {
                                Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.more_options))
                            }
                            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.rename)) },
                                    leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                                    onClick = { menuOpen = false; showRename = true }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.delete)) },
                                    leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                                    onClick = { menuOpen = false; showDelete = true }
                                )
                            }
                        }
                    }
                }
            )
        },
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
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (documents.isEmpty()) {
                EmptyState(
                    Icons.Filled.Description,
                    stringResource(R.string.empty_folder_title),
                    stringResource(R.string.empty_folder_body)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    documentItems(
                        docs = documents,
                        onOpen = { onOpenDocument(it.id) },
                        onToggleFavorite = vm::toggleFavorite,
                        onDialog = { docDialog = it }
                    )
                }
            }
        }
    }

    DocumentDialogHost(
        dialog = docDialog,
        folders = folders,
        onDismiss = { docDialog = null },
        onRename = vm::renameDocument,
        onMove = vm::moveDocument,
        onDelete = vm::deleteDocument
    )

    if (showRename && folder != null) {
        NameDialog(
            title = stringResource(R.string.rename),
            label = stringResource(R.string.folder_name),
            initial = folder.name,
            confirmLabel = stringResource(R.string.save),
            onDismiss = { showRename = false },
            onConfirm = { vm.renameFolder(folder.id, it); showRename = false }
        )
    }

    if (showDelete && folder != null) {
        ConfirmDialog(
            title = stringResource(R.string.delete_folder_title),
            message = stringResource(R.string.delete_folder_message, folder.name),
            confirmLabel = stringResource(R.string.delete),
            onDismiss = { showDelete = false },
            onConfirm = { vm.deleteFolder(folder.id); showDelete = false }
        )
    }

    if (importing) ImportingDialog()
}
