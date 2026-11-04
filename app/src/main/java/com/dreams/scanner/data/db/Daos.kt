package com.dreams.scanner.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Query(
        "SELECT f.id AS id, f.name AS name, f.createdAt AS createdAt, " +
            "(SELECT COUNT(*) FROM documents d WHERE d.folderId = f.id) AS documentCount " +
            "FROM folders f ORDER BY f.name COLLATE NOCASE ASC"
    )
    fun observeAll(): Flow<List<FolderWithCount>>

    @Query("SELECT * FROM folders WHERE id = :id")
    fun observe(id: Long): Flow<FolderEntity?>

    @Insert
    suspend fun insert(folder: FolderEntity): Long

    @Query("UPDATE folders SET name = :name WHERE id = :id")
    suspend fun rename(id: Long, name: String)

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE folderId = :folderId ORDER BY updatedAt DESC")
    fun observeInFolder(folderId: Long): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE isFavorite = 1 ORDER BY updatedAt DESC")
    fun observeFavorites(): Flow<List<DocumentEntity>>

    @Query(
        "SELECT * FROM documents WHERE name LIKE '%' || :query || '%' " +
            "OR ocrText LIKE '%' || :query || '%' ORDER BY updatedAt DESC"
    )
    fun search(query: String): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :id")
    fun observe(id: Long): Flow<DocumentEntity?>

    @Insert
    suspend fun insert(document: DocumentEntity): Long

    @Update
    suspend fun update(document: DocumentEntity)

    @Query("UPDATE documents SET name = :name, updatedAt = :now WHERE id = :id")
    suspend fun rename(id: Long, name: String, now: Long)

    @Query("UPDATE documents SET isFavorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: Long, favorite: Boolean)

    @Query("UPDATE documents SET folderId = :folderId, updatedAt = :now WHERE id = :id")
    suspend fun move(id: Long, folderId: Long?, now: Long)

    @Query("UPDATE documents SET pageCount = :count, coverPath = :cover, updatedAt = :now WHERE id = :id")
    suspend fun setPagesAndCover(id: Long, count: Int, cover: String?, now: Long)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface PageDao {
    @Query("SELECT * FROM pages WHERE documentId = :documentId ORDER BY position ASC")
    fun observeForDocument(documentId: Long): Flow<List<PageEntity>>

    @Query("SELECT * FROM pages WHERE documentId = :documentId ORDER BY position ASC")
    suspend fun getForDocument(documentId: Long): List<PageEntity>

    @Insert
    suspend fun insert(page: PageEntity): Long

    @Update
    suspend fun update(page: PageEntity)

    @Query("DELETE FROM pages WHERE id = :id")
    suspend fun delete(id: Long)
}
