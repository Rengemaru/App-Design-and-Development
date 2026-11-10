package com.example.app_design_and_development

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable

/** A quiet ruled-paper background shared by every page of the quiz notebook. */
class NotebookDrawable(density: Float) : Drawable() {
    private val spacing = 32f * density
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeWidth = density }

    override fun draw(canvas: Canvas) {
        canvas.drawColor(0xFFE8F4FC.toInt())
        paint.color = 0xFF9BCDEB.toInt()
        var y = bounds.top + spacing
        while (y < bounds.bottom) {
            canvas.drawLine(bounds.left.toFloat(), y, bounds.right.toFloat(), y, paint)
            y += spacing
        }
    }

    override fun setAlpha(alpha: Int) { paint.alpha = alpha }
    override fun setColorFilter(colorFilter: ColorFilter?) { paint.colorFilter = colorFilter }
    @Deprecated("Deprecated in Android")
    override fun getOpacity(): Int = PixelFormat.OPAQUE
}
