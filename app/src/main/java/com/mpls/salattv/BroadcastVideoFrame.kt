package com.mpls.salattv

import android.content.Context
import android.graphics.Outline
import android.view.Gravity
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import kotlin.math.roundToInt

/** Uniform top-anchored crop hides the broadcaster's bottom clock/reader strip.
 * The native parent clips its child SurfaceView without adding a texture pass.
 */
internal class BroadcastVideoFrame(context: Context) : FrameLayout(context) {
    init {
        clipChildren = true
        clipToPadding = true
        clipToOutline = true
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, 24f * resources.displayMetrics.density)
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (childCount > 0 && w > 0 && h > 0) {
            // 1/1.14 retains the top 87.7% of the already-filled panel.
            getChildAt(0).layoutParams = LayoutParams(
                (w * 1.14f).roundToInt(), (h * 1.14f).roundToInt(),
                Gravity.TOP or Gravity.CENTER_HORIZONTAL)
        }
    }
}
