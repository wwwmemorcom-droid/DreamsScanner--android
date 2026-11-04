@file:OptIn(ExperimentalMaterial3Api::class)

package com.dreams.scanner.ui.document

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.dreams.scanner.R
import com.dreams.scanner.ui.components.DocDialog
import com.dreams.scanner.ui.components.DocumentDialogHost
import com.dreams.scanner.ui.components.LocalFileStore

/**
 * Phase 1 document viewer: swipe through the pages. Phase 2/3 add editing (crop, filters,
 * rotate, reorder) and PDF export on top of this screen.
 */
@Composable
fun DocumentScreen(
    vm: DocumentViewModel,
    onBack: () -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val pages by vm.pages.collectAsStateWithLifecycle()
    val folders by vm.folders.collectAsStateWithLifecycle()
    val files = LocalFileStore.current

    var dialog by remember { mutableStateOf<DocDialog?>(null) }
    var menuOpen by remember { mutableStateOf(false) }

    // The document disappeared (deleted from this screen): go back.
    LaunchedEffect(state) {
        if (state.loaded && state.document == null) onBack()
    }

    val doc = state.document

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(doc?.name.orEmpty(), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if (doc != null) {
                        IconButton(onClick = { vm.toggleFavorite(doc) }) {
                            Icon(
                                imageVector = if (doc.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                                contentDescription = stringResource(
                                    if (doc.isFavorite) R.string.remove_favorite else R.string.add_favorite
                                ),
                                tint = if (doc.isFavorite) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Box {
                            IconButton(onClick = { menuOpen = true }) {
                                Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.more_options))
                            }
                            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.rename)) },
                                    leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                                    onClick = { menuOpen = false; dialog = DocDialog.Rename(doc) }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.move)) },
                                    leadingIcon = { Icon(Icons.Filled.Folder, contentDescription = null) },
                                    onClick = { menuOpen = false; dialog = DocDialog.Move(doc) }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.delete)) },
                                    leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                                    onClick = { menuOpen = false; dialog = DocDialog.Delete(doc) }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (pages.isEmpty()) {
                if (state.loaded) {
                    Text(
                        stringResource(R.string.no_pages),
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val pagerState = rememberPagerState(pageCount = { pages.size })
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    key = { index -> pages[index].id }
                ) { index ->
                    val page = pages[index]
                    val file = remember(page.processedPath) { files.resolve(page.processedPath) }
                    AsyncImage(
                        model = file,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.page_indicator, pagerState.currentPage + 1, pages.size),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 20.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.85f), RoundedCornerShape(50))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    }

    DocumentDialogHost(
        dialog = dialog,
        folders = folders,
        onDismiss = { dialog = null },
        onRename = vm::renameDocument,
        onMove = vm::moveDocument,
        onDelete = vm::deleteDocument
    )
}
