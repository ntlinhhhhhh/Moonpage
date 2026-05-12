package com.diary.moonpage.core.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.findViewTreeCompositionContext
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

object ComposeCaptureUtils {
    /**
     * Captures a Composable as a Bitmap.
     * Note: This works by adding a temporary ComposeView to a FrameLayout and manually rendering it.
     */
    fun captureComposable(
        view: View,
        content: @Composable () -> Unit,
        width: Int,
        height: Int = -1,
        parentContext: CompositionContext? = null,
        onBitmapCaptured: (Bitmap) -> Unit
    ) {
        // Enforce Main Thread
        if (android.os.Looper.myLooper() != android.os.Looper.getMainLooper()) {
            view.post {
                captureComposable(view, content, width, height, parentContext, onBitmapCaptured)
            }
            return
        }

        try {
            val context = view.context
            val composeView = ComposeView(context).apply {
                // Set the necessary ViewTree owners so Compose can function.
                setViewTreeLifecycleOwner(view.findViewTreeLifecycleOwner())
                setViewTreeViewModelStoreOwner(view.findViewTreeViewModelStoreOwner())
                setViewTreeSavedStateRegistryOwner(view.findViewTreeSavedStateRegistryOwner())
                
                val compositionContext = parentContext ?: view.findViewTreeCompositionContext()
                if (compositionContext != null) {
                    setParentCompositionContext(compositionContext)
                }
                
                setContent(content)
            }

            val frameLayout = FrameLayout(context).apply {
                addView(composeView, ViewGroup.LayoutParams(width, if (height > 0) height else ViewGroup.LayoutParams.WRAP_CONTENT))
            }

            // Measure & Layout
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
            
            if (width <= 0 || finalHeight <= 0) {
                composeView.disposeComposition()
                return
            }
            
            frameLayout.layout(0, 0, width, finalHeight)

            // Compose needs at least one frame to render.
            // Since this view is not attached to a window, we wait a bit for the recomposer.
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                try {
                    // Draw to bitmap
                    val bitmap = Bitmap.createBitmap(width, finalHeight, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    frameLayout.draw(canvas)
                    
                    onBitmapCaptured(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                    android.widget.Toast.makeText(context, "Capture failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                } finally {
                    // Cleanup
                    composeView.disposeComposition()
                }
            }, 150) // 150ms delay is usually enough for simple composables
            
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(view.context, "Failed to start capture", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}
