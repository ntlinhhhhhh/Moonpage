package com.diary.moonpage.core.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.camera.core.CameraSelector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import kotlin.math.min

import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.*

object ImageUtils {
    /**
     */
    fun shareImage(context: Context, bitmap: Bitmap, title: String = "Share Mood") {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val cachePath = File(context.cacheDir, "shared_images")
                if (!cachePath.exists()) cachePath.mkdirs()
                
                val file = File(cachePath, "share_${System.currentTimeMillis()}.jpg")
                val stream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                stream.close()

                val contentUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                if (contentUri != null) {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        setDataAndType(contentUri, context.contentResolver.getType(contentUri))
                        putExtra(Intent.EXTRA_STREAM, contentUri)
                        type = "image/jpeg"
                    }
                    val chooser = Intent.createChooser(shareIntent, title).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    withContext(Dispatchers.Main) {
                        context.startActivity(chooser)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to share image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    suspend fun compressAndCropSquare(
        context: Context,
        uri: Uri,
        lensFacing: Int = CameraSelector.LENS_FACING_BACK,
        quality: Int = 80 // 80% là mức vàng để giữ chất lượng và nhẹ dung lượng
    ): File? = withContext(Dispatchers.IO) {
        return@withContext try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true 
            }
            context.contentResolver.openInputStream(uri)?.use { 
                BitmapFactory.decodeStream(it, null, options) 
            }

            // Tính toán inSampleSize để không load full ảnh gốc vào RAM
            val targetSize = 1080
            var inSampleSize = 1
            if (options.outHeight > targetSize || options.outWidth > targetSize) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while (halfHeight / inSampleSize >= targetSize && halfWidth / inSampleSize >= targetSize) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            
            val originalBitmap = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOptions)
            } ?: return@withContext null

            // 1. Fix xoay ảnh
            val rotation = getRotation(context, uri)
            val matrix = Matrix()
            if (rotation != 0) matrix.postRotate(rotation.toFloat())
            if (lensFacing == CameraSelector.LENS_FACING_FRONT) matrix.postScale(-1f, 1f)

            val processedBitmap = if (rotation != 0 || lensFacing == CameraSelector.LENS_FACING_FRONT) {
                if (originalBitmap.width > 0 && originalBitmap.height > 0) {
                    Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)
                } else {
                    originalBitmap
                }
            } else {
                originalBitmap
            }

            // 2. Center Crop Square
            val size = min(processedBitmap.width, processedBitmap.height)
            if (size <= 0) return@withContext null

            val x = (processedBitmap.width - size) / 2
            val y = (processedBitmap.height - size) / 2
            val squareBitmap = Bitmap.createBitmap(processedBitmap, x, y, size, size)

            // 3. Final Resize to 1080px
            val finalBitmap = if (size > targetSize) {
                Bitmap.createScaledBitmap(squareBitmap, targetSize, targetSize, true)
            } else {
                squareBitmap
            }

            // 4. Save with high compression
            val compressedFile = File(context.cacheDir, "up_${System.currentTimeMillis()}.webp")
            FileOutputStream(compressedFile).use { out ->
                val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
                finalBitmap.compress(format, quality, out)
            }

            // Cleanup RAM
            if (originalBitmap != processedBitmap) originalBitmap.recycle()
            if (processedBitmap != squareBitmap) processedBitmap.recycle()
            if (squareBitmap != finalBitmap) squareBitmap.recycle()
            finalBitmap.recycle()

            compressedFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun saveAvatarLocally(context: Context, sourceFile: File): String? = withContext(Dispatchers.IO) {
        try {
            val avatarDir = File(context.filesDir, "avatars")
            if (!avatarDir.exists()) avatarDir.mkdirs()
            
            val localFile = File(avatarDir, "current_avatar.webp")
            sourceFile.copyTo(localFile, overwrite = true)
            localFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getRotation(context: Context, uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val exif = ExifInterface(inputStream)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (e: Exception) {
            0
        }
    }

    suspend fun downloadAndSaveImage(context: Context, imageUrl: String) {
        withContext(Dispatchers.IO) {
            try {
                val url = URL(imageUrl)
                val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                saveBitmapToGallery(context, bitmap)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Image saved to gallery", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to download image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun saveBitmapToGallery(context: Context, bitmap: Bitmap) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val filename = "MP_${System.currentTimeMillis()}.jpg"
                val resolver = context.contentResolver
                
                val outputStream = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                        // Use standard "Pictures/MoonPage" path
                        put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/MoonPage")
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                    }
                    
                    val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    if (imageUri != null) {
                        val os = resolver.openOutputStream(imageUri)
                        if (os != null) {
                            // Write bitmap
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
                            os.close()
                            
                            // Remove pending status
                            contentValues.clear()
                            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                            resolver.update(imageUri, contentValues, null, null)
                            os
                        } else {
                            resolver.delete(imageUri, null, null)
                            null
                        }
                    } else null
                } else {
                    // Legacy path (< Android 10)
                    val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
                    val dir = File(imagesDir, "MoonPage")
                    if (!dir.exists()) {
                        if (!dir.mkdirs()) {
                            android.util.Log.e("ImageUtils", "Failed to create directory: ${dir.absolutePath}")
                        }
                    }
                    val file = File(dir, filename)
                    try {
                        FileOutputStream(file).apply {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, this)
                            close()
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ImageUtils", "Failed to open FileOutputStream: ${e.message}")
                        null
                    }
                }

                if (outputStream != null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Image saved to gallery", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    throw Exception("Failed to create output stream for gallery")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("ImageUtils", "Error saving to gallery: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to save image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
