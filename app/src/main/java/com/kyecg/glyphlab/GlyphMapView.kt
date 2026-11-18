
package com.kyecg.glyphlab

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.PathParser
import kotlin.math.min

class GlyphMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val selectedZones = mutableSetOf<String>()

    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#181818")
        style = Paint.Style.FILL
    }

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#141414")
        style = Paint.Style.FILL
    }

    private val circleStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#333333")
        style = Paint.Style.STROKE
        strokeWidth = 1.5f
    }

    private val glyphPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 7f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 12f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = Color.WHITE
        maskFilter = BlurMaskFilter(
            10f,
            BlurMaskFilter.Blur.NORMAL
        )
    }

    // Левый верхний — не трогаем
    private val glyphC = PathParser.createPathFromPathData(
        "M 28.2 103.4 A 95 95 0 0 1 84.4 39.9"
    )!!

    // Правый — спущен чуть ниже по окружности
    private val glyphA = PathParser.createPathFromPathData(
        "M 209.6 97.6 A 95 95 0 0 1 190.6 191.6"
    )!!

    // Нижний — прямая палка, чуть выше по кругу и чуть левее
    private val glyphB = PathParser.createPathFromPathData(
        "M 64.0 205.0 L 39.0 175.0"
    )!!

    private val bodyBounds = RectF(
        10f, 10f, 230f, 510f
    )

    fun setZoneSelected(zone: String, selected: Boolean) {
        if (selected) {
            selectedZones.add(zone)
        } else {
            selectedZones.remove(zone)
        }

        invalidate()
    }

    fun clearSelection() {
        selectedZones.clear()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (width == 0 || height == 0) return

        val scale = min(
            width / 240f,
            height / 520f
        )

        val save = canvas.save()

        canvas.translate(
            (width - 240f * scale) / 2f,
            (height - 520f * scale) / 2f
        )

        canvas.scale(scale, scale)

        drawPhone(canvas)
        drawGlyph(canvas, glyphC, "C")
        drawGlyph(canvas, glyphA, "A")
        drawGlyph(canvas, glyphB, "B")

        canvas.restoreToCount(save)
    }

    private fun drawPhone(canvas: Canvas) {
        canvas.drawRoundRect(
            bodyBounds,
            34f,
            34f,
            bodyPaint
        )

        canvas.drawCircle(
            120f,
            128f,
            87f,
            circlePaint
        )

        canvas.drawCircle(
            120f,
            128f,
            87f,
            circleStroke
        )
    }

    private fun drawGlyph(
        canvas: Canvas,
        path: Path,
        zone: String
    ) {
        if (zone in selectedZones) {
            glowPaint.color = Color.WHITE
            canvas.drawPath(path, glowPaint)
        }

        glyphPaint.color = if (zone in selectedZones) {
            Color.WHITE
        } else {
            Color.parseColor("#888888")
        }

        canvas.drawPath(path, glyphPaint)
    }
}
