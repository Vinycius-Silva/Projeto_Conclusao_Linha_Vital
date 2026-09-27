package com.linhavital.app.ui.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.linhavital.app.R
import kotlin.math.min

/** Decorative Figma-style curve. Touch handling stays on the existing navigation items. */
class NavigationWaveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    init { setWillNotDraw(false) }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.lv_brand)
        style = Paint.Style.FILL
    }
    private val wave = Path()
    var selectedIndex: Int = 1
        set(value) {
            field = value.coerceIn(0, 2)
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width == 0 || height == 0) return
        val density = resources.displayMetrics.density
        val w = width.toFloat()
        val h = height.toFloat()
        val baseline = h - 18f * density
        val column = if (layoutDirection == LAYOUT_DIRECTION_RTL) 2 - selectedIndex else selectedIndex
        val center = w * (column + 0.5f) / 3f
        val half = min(w / 6f - 2f * density, 78f * density).coerceAtLeast(1f)
        val crest = (baseline - 50f * density).coerceAtLeast(0f)
        wave.reset()
        wave.moveTo(0f, baseline)
        wave.lineTo(center - half, baseline)
        wave.cubicTo(center - half * 0.45f, baseline,
            center - half * 0.55f, crest, center, crest)
        wave.cubicTo(center + half * 0.55f, crest,
            center + half * 0.45f, baseline, center + half, baseline)
        wave.lineTo(w, baseline)
        wave.lineTo(w, h)
        wave.lineTo(0f, h)
        wave.close()
        canvas.drawPath(wave, fill)
    }
}
