package com.kyecg.glyphlab

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.SweepGradient
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

class ColorWheelView(context: Context) : View(context) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var hue = 0f
    private var saturation = 1f

    var onColorChanged: ((Int) -> Unit)? = null

    private val colors = intArrayOf(
        Color.RED,
        Color.YELLOW,
        Color.GREEN,
        Color.CYAN,
        Color.BLUE,
        Color.MAGENTA,
        Color.RED
    )

    fun setColor(color: Int) {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)

        hue = hsv[0]
        saturation = hsv[1]

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val radius = min(cx, cy) * 0.88f

        paint.shader = SweepGradient(cx, cy, colors, null)
        canvas.drawCircle(cx, cy, radius, paint)
        paint.shader = null

        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
        canvas.drawCircle(cx, cy, radius, paint)

        paint.style = Paint.Style.FILL

        val angle = Math.toRadians(hue.toDouble())
        val markerRadius = radius * saturation

        val markerX = cx + cos(angle).toFloat() * markerRadius
        val markerY = cy + sin(angle).toFloat() * markerRadius

        paint.color = Color.WHITE
        canvas.drawCircle(markerX, markerY, 12f, paint)

        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        canvas.drawCircle(markerX, markerY, 12f, paint)

        paint.style = Paint.Style.FILL
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN &&
            event.action != MotionEvent.ACTION_MOVE
        ) {
            return true
        }

        val cx = width / 2f
        val cy = height / 2f

        val dx = event.x - cx
        val dy = event.y - cy

        hue = (
            Math.toDegrees(
                atan2(dy.toDouble(), dx.toDouble())
            ).toFloat() + 360f
        ) % 360f

        val radius = min(cx, cy) * 0.88f

        saturation = (
            sqrt(dx * dx + dy * dy) / radius
        ).coerceIn(0f, 1f)

        val hsv = floatArrayOf(hue, saturation, 1f)

        onColorChanged?.invoke(Color.HSVToColor(hsv))

        invalidate()
        return true
    }
}
