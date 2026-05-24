package com.diary.moonpage.core.util

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import java.io.File
import java.io.FileOutputStream

fun Context.saveBitmapToInternalStorage(
    bitmap: Bitmap,
    fileName: String,
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
    quality: Int = 100
): String {
    val themesDir = File(filesDir, "custom_themes").apply {
        if (!exists()) mkdirs()
    }
    val extension = when (format) {
        Bitmap.CompressFormat.JPEG -> ".jpg"
        Bitmap.CompressFormat.PNG -> ".png"
        else -> ".webp"
    }
    val safeName = fileName
        .replace(Regex("[^A-Za-z0-9._-]"), "_")
        .let { name ->
            if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".webp")) {
                name
            } else {
                "$name$extension"
            }
        }
    val outputFile = File(themesDir, safeName)

    FileOutputStream(outputFile).use { stream ->
        bitmap.compress(format, quality, stream)
    }

    return outputFile.absolutePath
}

fun customThemeImageFormat(): Bitmap.CompressFormat {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Bitmap.CompressFormat.WEBP_LOSSY
    } else {
        @Suppress("DEPRECATION")
        Bitmap.CompressFormat.WEBP
    }
}
