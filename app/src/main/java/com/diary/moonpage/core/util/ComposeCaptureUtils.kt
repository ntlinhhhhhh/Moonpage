package com.diary.moonpage.core.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.compositionContext
import androidx.compose.ui.platform.findViewTreeCompositionContext
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
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
        if (android.os.Looper.myLooper() != android.os.Looper.getMainLooper()) {
            view.post {
                captureComposable(view, content, width, height, parentContext, onBitmapCaptured)
            }
            return
        }

        try {
            val context = view.context
            val lifecycleOwner = view.findViewTreeLifecycleOwner() ?: (context as? LifecycleOwner)
            val viewModelStoreOwner = view.findViewTreeViewModelStoreOwner() ?: (context as? ViewModelStoreOwner)
            val savedStateRegistryOwner = view.findViewTreeSavedStateRegistryOwner() ?: (context as? SavedStateRegistryOwner)
            
            val compositionContext = parentContext ?: view.findViewTreeCompositionContext()
            
            if (compositionContext == null) {
                android.util.Log.e("ComposeCaptureUtils", "Cannot capture composable: CompositionContext not found")
            }

            val composeView = ComposeView(context).apply {
                if (lifecycleOwner != null) setViewTreeLifecycleOwner(lifecycleOwner)
                if (viewModelStoreOwner != null) setViewTreeViewModelStoreOwner(viewModelStoreOwner)
                if (savedStateRegistryOwner != null) setViewTreeSavedStateRegistryOwner(savedStateRegistryOwner)
                
                if (compositionContext != null) {
                    setParentCompositionContext(compositionContext)
                    this.compositionContext = compositionContext
                }
                
                setContent(content)
            }

            val frameLayout = FrameLayout(context).apply {
                // Also set owners on the container to be safe
                if (lifecycleOwner != null) setViewTreeLifecycleOwner(lifecycleOwner)
                if (viewModelStoreOwner != null) setViewTreeViewModelStoreOwner(viewModelStoreOwner)
                if (savedStateRegistryOwner != null) setViewTreeSavedStateRegistryOwner(savedStateRegistryOwner)
                if (compositionContext != null) {
                    this.compositionContext = compositionContext
                }
                
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
                    android.util.Log.e("ComposeCaptureUtils", "Capture failed during drawing: ${e.message}")
                } finally {
                    // Cleanup
                    composeView.disposeComposition()
                }
            }, 300) // Increased delay slightly to ensure rendering is complete
            
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("ComposeCaptureUtils", "Failed to start capture: ${e.message}")
        }
    }
}
