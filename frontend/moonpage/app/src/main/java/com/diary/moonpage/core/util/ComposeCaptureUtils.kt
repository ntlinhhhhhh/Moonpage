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
        height: Int,
        onBitmapCaptured: (Bitmap) -> Unit
    ) {
        val context = view.context
        val composeView = ComposeView(context).apply {
            setContent(content)
        }

        val frameLayout = FrameLayout(context).apply {
            addView(composeView, ViewGroup.LayoutParams(width, height))
        }

        // Measure and layout the view
        frameLayout.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        )
        frameLayout.layout(0, 0, width, height)

        // Draw to bitmap
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        frameLayout.draw(canvas)
        
        onBitmapCaptured(bitmap)
    }
}
