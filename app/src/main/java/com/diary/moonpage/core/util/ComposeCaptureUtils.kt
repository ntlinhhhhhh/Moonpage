package com.diary.moonpage.core.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.Constraints
import androidx.core.view.drawToBitmap

object ComposeCaptureUtils {
    /**
     * Captures a Composable as a Bitmap.
     * Note: This works by adding a temporary ComposeView to the window or a dummy layout.
     */
    fun captureComposable(
        view: View,
        content: @Composable () -> Unit,
        width: Int,
        height: Int = -1, // -1 means wrap content height
        onBitmapCaptured: (Bitmap) -> Unit
    ) {
        val context = view.context
        val composeView = ComposeView(context).apply {
            setContent(content)
        }

        val frameLayout = FrameLayout(context).apply {
            addView(composeView, ViewGroup.LayoutParams(width, if (height > 0) height else ViewGroup.LayoutParams.WRAP_CONTENT))
        }

        // Measure
        val heightSpec = if (height > 0) {
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        } else {
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        }
        
        frameLayout.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            heightSpec
        )
        
        val finalHeight = if (height > 0) height else frameLayout.measuredHeight
        frameLayout.layout(0, 0, width, finalHeight)

        // Draw to bitmap
        val bitmap = Bitmap.createBitmap(width, finalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        frameLayout.draw(canvas)
        
        onBitmapCaptured(bitmap)
    }
}
