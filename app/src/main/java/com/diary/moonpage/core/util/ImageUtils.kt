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
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.*

object ImageUtils {

    suspend fun shareImage(context: Context, bitmap: Bitmap, title: String = "Share Mood") {
        withContext(Dispatchers.IO) {
            try {
                val shareBitmap = bitmap.toSoftwareBitmap()
                val cachePath = File(context.cacheDir, "shared_images")
                if (!cachePath.exists()) cachePath.mkdirs()
                
                // Cleanup old shares (older than 1 hour)
                cachePath.listFiles()?.forEach { 
                    if (it.lastModified() < System.currentTimeMillis() - 3600000) it.delete() 
                }

                val file = File(cachePath, "MP_Share_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { stream ->
                    shareBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                }

                val contentUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                if (contentUri != null) {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        putExtra(Intent.EXTRA_STREAM, contentUri)
                        type = "image/jpeg"
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    val chooser = Intent.createChooser(shareIntent, title)
                    if (context !is android.app.Activity) {
                        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    withContext(Dispatchers.Main) {
                        delay(300)
                        context.startActivity(chooser)
                    }
                } else {
                    throw Exception("Failed to generate content URI")
                }
            } catch (e: Exception) {
                android.util.Log.e("ImageUtils", "Share failed: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to share image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    suspend fun shareImageFromUrl(context: Context, imageUrl: String) {
        withContext(Dispatchers.IO) {
            try {
                val request = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .build()
                val result = context.imageLoader.execute(request)
                val drawable = (result as? coil.request.SuccessResult)?.drawable
                val bitmap = drawable?.toBitmap()
                if (bitmap != null) {
                    withContext(Dispatchers.Main) {
                        shareImage(context, bitmap, "Share Photo")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ImageUtils", "shareImageFromUrl failed", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to load image for sharing", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun applyRoundedCorners(bitmap: Bitmap, cornerRadius: Float): Bitmap {
        val source = bitmap.toSoftwareBitmap()
        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(output)
        
        val paint = android.graphics.Paint()
        paint.isAntiAlias = true
        paint.color = android.graphics.Color.WHITE
        
        val rect = android.graphics.RectF(0f, 0f, source.width.toFloat(), source.height.toFloat())
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
        
        paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(source, 0f, 0f, paint)
        
        return output
    }

    private fun Bitmap.toSoftwareBitmap(): Bitmap {
        if (config != Bitmap.Config.HARDWARE && config != null) return this
        return copy(Bitmap.Config.ARGB_8888, false)
    }

    suspend fun compressAndCropSquare(
        context: Context,
        uri: Uri,
        lensFacing: Int = CameraSelector.LENS_FACING_BACK,
        quality: Int = 80
    ): File? = withContext(Dispatchers.IO) {
        return@withContext try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true 
            }
            context.contentResolver.openInputStream(uri)?.use { 
                BitmapFactory.decodeStream(it, null, options) 
            }

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

            val size = min(processedBitmap.width, processedBitmap.height)
            if (size <= 0) return@withContext null

            val x = (processedBitmap.width - size) / 2
            val y = (processedBitmap.height - size) / 2
            val squareBitmap = Bitmap.createBitmap(processedBitmap, x, y, size, size)

            val finalBitmap = if (size > targetSize) {
                Bitmap.createScaledBitmap(squareBitmap, targetSize, targetSize, true)
            } else {
                squareBitmap
            }

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

    suspend fun saveBitmapToGallery(context: Context, bitmap: Bitmap) {
        withContext(Dispatchers.IO) {
            var success = false
            try {
                val filename = "MP_Log_${System.currentTimeMillis()}.jpg"
                val resolver = context.contentResolver
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/MoonPage")
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                    }
                    
                    val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    if (imageUri != null) {
                        resolver.openOutputStream(imageUri).use { os ->
                            if (os != null) {
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
                                success = true
                            }
                        }
                        
                        if (success) {
                            contentValues.clear()
                            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                            resolver.update(imageUri, contentValues, null, null)
                        } else {
                            resolver.delete(imageUri, null, null)
                        }
                    }
                } else {
                    val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
                    val dir = File(imagesDir, "MoonPage")
                    if (!dir.exists()) dir.mkdirs()
                    
                    val file = File(dir, filename)
                    FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                        success = true
                    }
                    
                    val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    intent.data = Uri.fromFile(file)
                    context.sendBroadcast(intent)
                }

                withContext(Dispatchers.Main) {
                    if (success) {
                        Toast.makeText(context, "Saved to gallery!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ImageUtils", "Save to gallery failed: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error saving: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    suspend fun downloadAndSaveImage(context: Context, imageUrl: String) {
        withContext(Dispatchers.IO) {
            try {
                val request = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .build()
                val result = context.imageLoader.execute(request)
                val drawable = (result as? coil.request.SuccessResult)?.drawable
                val bitmap = drawable?.toBitmap()
                if (bitmap != null) {
                    saveBitmapToGallery(context, bitmap)
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to download image", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to download image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
