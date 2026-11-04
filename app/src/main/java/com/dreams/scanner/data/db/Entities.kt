package com.dreams.scanner.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long
)

/** Result row of the folder list query: a folder plus how many documents it holds. */
data class FolderWithCount(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val documentCount: Int
)

@Entity(
    tableName = "documents",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("folderId")]
)
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val folderId: Long? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val isFavorite: Boolean = false,
    val pageCount: Int = 0,
    /** Path of the cover thumbnail, relative to the app files directory. */
    val coverPath: String? = null,
    /** Full OCR text of the document (filled in phase 4), used by search. */
    val ocrText: String = ""
)

@Entity(
    tableName = "pages",
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("documentId")]
)
data class PageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val documentId: Long,
    val position: Int,
    /** Untouched source image (relative path). Lets filters/crop be redone non-destructively. */
    val originalPath: String,
    /** Image shown/exported after crop + filter (relative path). */
    val processedPath: String,
    val filter: String = "ORIGINAL",
    val rotation: Int = 0,
    /** Four crop corners as "x1,y1;x2,y2;x3,y3;x4,y4" in original-image pixels (phase 2). */
    val corners: String? = null,
    val brightness: Float = 0f,
    val contrast: Float = 1f,
    val ocrText: String = ""
)
