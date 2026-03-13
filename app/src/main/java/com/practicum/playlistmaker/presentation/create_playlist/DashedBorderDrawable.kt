package com.practicum.playlistmaker.presentation.create_playlist

import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt

class DashedBorderDrawable(
    @ColorInt private val color: Int,
    private val strokeWidth: Float,
    private val cornerRadius: Float
) : Drawable() {

    private val cornerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = this@DashedBorderDrawable.color
        style = Paint.Style.STROKE
        strokeWidth = this@DashedBorderDrawable.strokeWidth
        isAntiAlias = true
    }

    private val dashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = this@DashedBorderDrawable.color
        style = Paint.Style.STROKE
        strokeWidth = this@DashedBorderDrawable.strokeWidth
        isAntiAlias = true
    }

    private val rect = RectF()
    private val path = Path()

    override fun draw(canvas: Canvas) {
        rect.set(
            strokeWidth / 2,
            strokeWidth / 2,
            bounds.width() - strokeWidth / 2,
            bounds.height() - strokeWidth / 2
        )

        val innerLeft = rect.left + cornerRadius
        val innerRight = rect.right - cornerRadius
        val innerTop = rect.top + cornerRadius
        val innerBottom = rect.bottom - cornerRadius

        val straightSegment = innerRight - innerLeft
        val dash = straightSegment / 11f
        dashPaint.pathEffect = DashPathEffect(floatArrayOf(dash, dash), 0f)


        path.reset()
        path.addArc(rect.left, rect.top, rect.left + cornerRadius * 2, rect.top + cornerRadius * 2, 180f, 90f)
        canvas.drawPath(path, cornerPaint)
        path.reset()
        path.addArc(rect.right - cornerRadius * 2, rect.top, rect.right, rect.top + cornerRadius * 2, 270f, 90f)
        canvas.drawPath(path, cornerPaint)
        path.reset()
        path.addArc(rect.right - cornerRadius * 2, rect.bottom - cornerRadius * 2, rect.right, rect.bottom, 0f, 90f)
        canvas.drawPath(path, cornerPaint)
        path.reset()
        path.addArc(rect.left, rect.bottom - cornerRadius * 2, rect.left + cornerRadius * 2, rect.bottom, 90f, 90f)
        canvas.drawPath(path, cornerPaint)


        path.reset()
        path.moveTo(innerLeft, rect.top)
        path.lineTo(innerRight, rect.top)
        canvas.drawPath(path, dashPaint)

        path.reset()
        path.moveTo(rect.right, innerTop)
        path.lineTo(rect.right, innerBottom)
        canvas.drawPath(path, dashPaint)

        path.reset()
        path.moveTo(innerRight, rect.bottom)
        path.lineTo(innerLeft, rect.bottom)
        canvas.drawPath(path, dashPaint)

        path.reset()
        path.moveTo(rect.left, innerBottom)
        path.lineTo(rect.left, innerTop)
        canvas.drawPath(path, dashPaint)
    }

    override fun setAlpha(alpha: Int) {
        cornerPaint.alpha = alpha
        dashPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {
        cornerPaint.colorFilter = colorFilter
        dashPaint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int = android.graphics.PixelFormat.TRANSLUCENT
}
