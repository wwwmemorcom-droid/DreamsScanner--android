package com.dreams.scanner.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * All document images live in the app's private storage (filesDir/docs/<documentId>/...),
 * so no storage permission is needed and nothing is visible to other apps.
 * Paths stored in the database are relative to filesDir.
 */
class FileStore(context: Context) {
    private val appContext = context.applicationContext
    private val root = File(appContext.filesDir, "docs")

    fun documentDir(documentId: Long): File =
        File(root, documentId.toString()).also { it.mkdirs() }

    fun resolve(relativePath: String): File = File(appContext.filesDir, relativePath)

    fun relative(file: File): String = file.relativeTo(appContext.filesDir).path

    fun deleteDocument(documentId: Long) {
        File(root, documentId.toString()).deleteRecursively()
    }

    /**
     * Decodes an image from a content Uri. EXIF rotation is applied by ImageDecoder.
     * Very large photos are down-sampled (power of two) so the longest side stays <= MAX_SIDE,
     * which keeps memory use safe on low-RAM phones.
     */
    fun decode(uri: Uri): Bitmap {
        val source = ImageDecoder.createSource(appContext.contentResolver, uri)
        return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            val longest = max(info.size.width, info.size.height)
            var sample = 1
            while (longest / sample > MAX_SIDE) sample *= 2
            if (sample > 1) decoder.setTargetSampleSize(sample)
        }
    }

    @Throws(IOException::class)
    fun saveJpeg(bitmap: Bitmap, file: File, quality: Int) {
        file.parentFile?.mkdirs()
        val flattened = if (bitmap.hasAlpha()) flattenOnWhite(bitmap) else null
        val toSave = flattened ?: bitmap
        try {
            FileOutputStream(file).use { out ->
                if (!toSave.compress(Bitmap.CompressFormat.JPEG, quality, out)) {
                    throw IOException("JPEG compression failed")
                }
            }
        } finally {
            flattened?.recycle()
        }
    }

    @Throws(IOException::class)
    fun saveThumbnail(bitmap: Bitmap, file: File) {
        val longest = max(bitmap.width, bitmap.height)
        if (longest <= THUMB_SIDE) {
            saveJpeg(bitmap, file, 85)
            return
        }
        val scale = THUMB_SIDE.toFloat() / longest
        val thumb = Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * scale).roundToInt().coerceAtLeast(1),
            (bitmap.height * scale).roundToInt().coerceAtLeast(1),
            true
        )
        try {
            saveJpeg(thumb, file, 85)
        } finally {
            if (thumb !== bitmap) thumb.recycle()
        }
    }

    private fun flattenOnWhite(bitmap: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        return result
    }

    companion object {
        const val MAX_SIDE = 4096
        const val THUMB_SIDE = 480
    }
}
