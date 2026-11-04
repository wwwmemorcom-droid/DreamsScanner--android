package com.dreams.scanner.data

import android.content.Context
import android.net.Uri
import com.dreams.scanner.R
import com.dreams.scanner.data.db.AppDatabase
import com.dreams.scanner.data.db.DocumentEntity
import com.dreams.scanner.data.db.FolderEntity
import com.dreams.scanner.data.db.FolderWithCount
import com.dreams.scanner.data.db.PageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ImportResult(val documentId: Long?, val imported: Int, val failed: Int)

class DocumentRepository(
    context: Context,
    private val db: AppDatabase,
    private val files: FileStore
) {
    private val appContext = context.applicationContext

    // ---- Observation -------------------------------------------------------------------------

    fun observeFolders(): Flow<List<FolderWithCount>> = db.folderDao().observeAll()
    fun observeFolder(id: Long): Flow<FolderEntity?> = db.folderDao().observe(id)

    fun observeDocuments(): Flow<List<DocumentEntity>> = db.documentDao().observeAll()
    fun observeInFolder(folderId: Long): Flow<List<DocumentEntity>> = db.documentDao().observeInFolder(folderId)
    fun observeFavorites(): Flow<List<DocumentEntity>> = db.documentDao().observeFavorites()
    fun search(query: String): Flow<List<DocumentEntity>> = db.documentDao().search(query)
    fun observeDocument(id: Long): Flow<DocumentEntity?> = db.documentDao().observe(id)
    fun observePages(documentId: Long): Flow<List<PageEntity>> = db.pageDao().observeForDocument(documentId)

    // ---- Folders -----------------------------------------------------------------------------

    suspend fun createFolder(name: String): Long =
        db.folderDao().insert(FolderEntity(name = name.trim(), createdAt = System.currentTimeMillis()))

    suspend fun renameFolder(id: Long, name: String) = db.folderDao().rename(id, name.trim())

    /** Documents of a deleted folder are kept: the foreign key sets their folderId to NULL. */
    suspend fun deleteFolder(id: Long) = withContext(NonCancellable) { db.folderDao().delete(id) }

    // ---- Documents ---------------------------------------------------------------------------

    suspend fun renameDocument(id: Long, name: String) =
        db.documentDao().rename(id, name.trim(), System.currentTimeMillis())

    suspend fun setFavorite(id: Long, favorite: Boolean) = db.documentDao().setFavorite(id, favorite)

    suspend fun moveDocument(id: Long, folderId: Long?) =
        db.documentDao().move(id, folderId, System.currentTimeMillis())

    /** Deletes the row first, then the image files. Runs to completion even if the caller goes away. */
    suspend fun deleteDocument(id: Long) = withContext(NonCancellable + Dispatchers.IO) {
        db.documentDao().delete(id)
        files.deleteDocument(id)
    }

    // ---- Import ------------------------------------------------------------------------------

    /**
     * Creates one document from the chosen images (one page per image).
     * Runs to completion even if the caller is cancelled, so a document is never left half-written.
     */
    suspend fun importImages(uris: List<Uri>, folderId: Long?): ImportResult =
        withContext(NonCancellable + Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val stamp = SimpleDateFormat("yyyy-MM-dd HH.mm", Locale.US).format(Date(now))
            val documentId = db.documentDao().insert(
                DocumentEntity(
                    name = appContext.getString(R.string.default_document_name, stamp),
                    folderId = folderId,
                    createdAt = now,
                    updatedAt = now
                )
            )
            val dir = files.documentDir(documentId)
            var cover: String? = null
            var imported = 0
            var failed = 0

            for (uri in uris) {
                try {
                    val bitmap = files.decode(uri)
                    try {
                        val file = File(dir, "page_${System.currentTimeMillis()}_$imported.jpg")
                        files.saveJpeg(bitmap, file, 92)
                        if (cover == null) {
                            val coverFile = File(dir, "cover.jpg")
                            files.saveThumbnail(bitmap, coverFile)
                            cover = files.relative(coverFile)
                        }
                        val relative = files.relative(file)
                        db.pageDao().insert(
                            PageEntity(
                                documentId = documentId,
                                position = imported,
                                originalPath = relative,
                                processedPath = relative
                            )
                        )
                        imported++
                    } finally {
                        bitmap.recycle()
                    }
                } catch (e: Exception) {
                    failed++
                } catch (e: OutOfMemoryError) {
                    failed++
                }
            }

            if (imported == 0) {
                db.documentDao().delete(documentId)
                files.deleteDocument(documentId)
                ImportResult(null, 0, failed)
            } else {
                db.documentDao().setPagesAndCover(documentId, imported, cover, System.currentTimeMillis())
                ImportResult(documentId, imported, failed)
            }
        }
}
