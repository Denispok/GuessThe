package com.gamesbars.guessthe.screen.coins

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.toPx
import kotlin.math.roundToInt

class ProductDecoration(context: Context) : RecyclerView.ItemDecoration() {

    private val divider = context.getDrawable(R.drawable.divider)
    private val bounds = Rect()
    private val horizontalMargin = 8.toPx(context).toInt()

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (divider == null) return

        canvas.save()

        val left = parent.paddingLeft + horizontalMargin
        val right = parent.width - parent.paddingRight - horizontalMargin
        canvas.clipRect(left, parent.paddingTop, right, parent.height - parent.paddingBottom)

        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            parent.getDecoratedBoundsWithMargins(child, bounds)
            val bottom = bounds.bottom + child.translationY.roundToInt()
            val top = bottom - divider.intrinsicHeight
            divider.setBounds(left, top, right, bottom)
            divider.draw(canvas)
        }

        canvas.restore()
    }
}