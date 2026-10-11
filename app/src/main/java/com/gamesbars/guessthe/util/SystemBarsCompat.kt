package com.gamesbars.guessthe.util

import android.app.Activity
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.gamesbars.guessthe.R

/**
 * Keeps the pre-edge-to-edge look of the screens.
 *
 * Starting with targetSdk 36 an app can no longer opt out of edge-to-edge on Android 16+:
 * the status and navigation bars are transparent and the window content is laid out behind them.
 * This pads the activity content by the system bar insets and paints the bar areas with the colors
 * the theme uses for `android:statusBarColor` / `android:navigationBarColor`, so the screens look
 * the same as when the system drew opaque bars.
 *
 * On Android 15 and below the window is not edge-to-edge (see values-v35/styles.xml), the system
 * decor consumes the insets before they reach the content view and this is effectively a no-op.
 *
 * Must be called after [Activity.setContentView].
 */
fun Activity.applySystemBarsCompat() {
    val content = findViewById<View>(android.R.id.content)
    val barsDrawable = SystemBarsDrawable(
        statusBarColor = ContextCompat.getColor(this, R.color.colorStatusBar),
        navigationBarColor = ContextCompat.getColor(this, R.color.colorNavigationBar),
    )
    content.background = barsDrawable
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // The navigation bar area is painted with an opaque color, no system scrim is needed on top of it
        window.isNavigationBarContrastEnforced = false
    }
    ViewCompat.setOnApplyWindowInsetsListener(content) { view, windowInsets ->
        val insets = windowInsets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
        barsDrawable.insets = insets
        WindowInsetsCompat.CONSUMED
    }
}

/** Paints the system bar areas (the padding of the view it is set on) with opaque colors. */
private class SystemBarsDrawable(
    private val statusBarColor: Int,
    private val navigationBarColor: Int,
) : Drawable() {

    var insets: Insets = Insets.NONE
        set(value) {
            if (field != value) {
                field = value
                invalidateSelf()
            }
        }

    private val paint = Paint()
    private val rect = Rect()

    override fun draw(canvas: Canvas) {
        val b = bounds
        if (insets.top > 0) {
            rect.set(b.left, b.top, b.right, b.top + insets.top)
            fill(canvas, statusBarColor)
        }
        if (insets.bottom > 0) {
            rect.set(b.left, b.bottom - insets.bottom, b.right, b.bottom)
            fill(canvas, navigationBarColor)
        }
        if (insets.left > 0) {
            rect.set(b.left, b.top + insets.top, b.left + insets.left, b.bottom - insets.bottom)
            fill(canvas, navigationBarColor)
        }
        if (insets.right > 0) {
            rect.set(b.right - insets.right, b.top + insets.top, b.right, b.bottom - insets.bottom)
            fill(canvas, navigationBarColor)
        }
    }

    private fun fill(canvas: Canvas, color: Int) {
        paint.color = color
        canvas.drawRect(rect, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
