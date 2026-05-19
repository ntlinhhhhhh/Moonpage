package com.diary.moonpage.core.util

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream

fun Context.saveBitmapToInternalStorage(bitmap: Bitmap, fileName: String): String {
    val themesDir = File(filesDir, "custom_themes").apply {
        if (!exists()) mkdirs()
    }
    val safeName = fileName
        .replace(Regex("[^A-Za-z0-9._-]"), "_")
        .let { if (it.endsWith(".png")) it else "$it.png" }
    val outputFile = File(themesDir, safeName)

    FileOutputStream(outputFile).use { stream ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    }

    return outputFile.absolutePath
}
