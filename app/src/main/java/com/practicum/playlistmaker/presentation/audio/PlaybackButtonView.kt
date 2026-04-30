package com.practicum.playlistmaker.presentation.audio

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import com.practicum.playlistmaker.R

class PlaybackButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val dstRect = RectF()
    private val playDrawable = loadDrawableFromAttrs(attrs, R.styleable.PlaybackButtonView_playIcon)
    private val pauseDrawable = loadDrawableFromAttrs(attrs, R.styleable.PlaybackButtonView_pauseIcon)

    private var isPlaying: Boolean = false
    private val iconTintColor: Int

    private var onToggleListener: ((isPlaying: Boolean) -> Unit)? = null

    init {
        iconTintColor = resolveThemeColor(com.google.android.material.R.attr.colorOnSurface)
        tint(playDrawable)
        tint(pauseDrawable)
    }

    fun setPlaying(playing: Boolean) {
        if (isPlaying == playing) return
        isPlaying = playing
        invalidate()
    }

    fun setOnToggleListener(listener: ((isPlaying: Boolean) -> Unit)?) {
        onToggleListener = listener
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_UP -> {
                isPlaying = !isPlaying
                invalidate()
                onToggleListener?.invoke(isPlaying)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        dstRect.set(
            paddingLeft.toFloat(),
            paddingTop.toFloat(),
            (w - paddingRight).toFloat(),
            (h - paddingBottom).toFloat()
        )
        val l = dstRect.left.toInt()
        val t = dstRect.top.toInt()
        val r = dstRect.right.toInt()
        val b = dstRect.bottom.toInt()
        playDrawable?.setBounds(l, t, r, b)
        pauseDrawable?.setBounds(l, t, r, b)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val d = if (isPlaying) pauseDrawable else playDrawable
        d?.draw(canvas)
    }

    private fun tint(drawable: android.graphics.drawable.Drawable?) {
        if (drawable == null) return
        DrawableCompat.setTint(drawable, iconTintColor)
    }

    private fun resolveThemeColor(attrResId: Int): Int {
        val tv = TypedValue()
        val resolved = context.theme.resolveAttribute(attrResId, tv, true)
        if (!resolved) return 0
        return tv.data
    }

    private fun loadDrawableFromAttrs(attrs: AttributeSet?, styleableIndex: Int): android.graphics.drawable.Drawable? {
        val a = context.obtainStyledAttributes(attrs, R.styleable.PlaybackButtonView)
        val resId = a.getResourceId(styleableIndex, 0)
        a.recycle()
        if (resId == 0) return null
        return AppCompatResources.getDrawable(context, resId)?.let { DrawableCompat.wrap(it).mutate() }
    }
}

