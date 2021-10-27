package com.darekbx.temperaturemonitor.ui.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View

class ChartView(
    context: Context,
    attrs: AttributeSet?
): View(context, attrs) {

    private val chartPaint = Paint().apply {
        isAntiAlias = true
        color = Color.GREEN
    }

    private val guidePaint = Paint().apply {
        isAntiAlias = false
        strokeWidth = 1.0F
        color = Color.argb(50, 255, 255, 255)
    }

    private val textPaint = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
        textSize = 24.0F
    }

    private val leftPadding = 96F

    var unit = "°"
    var guideDigits = 2

    var values: List<Float> = emptyList()
        set(value) {
            field = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (values.isEmpty() || canvas == null) {
            return
        }

        canvas.translate(0F, (height * 0.3F) / 2)

        var height = (height * 0.7F)
        val width = width - leftPadding

        val count = values.count()
        val maxValue = (values.maxOrNull() ?: 1.0F)
        val minValue = (values.minOrNull() ?: 1.0F)

        val heightRatio = height / (maxValue - minValue)
        val widthRatio = width / count.toFloat()
        val firstPoint = PointF(leftPadding, height - ((values.first() - minValue) * heightRatio))

        drawGuide(canvas, 0F, maxValue)
        drawGuide(canvas, height, minValue)
        drawValues(widthRatio, height, heightRatio, canvas, firstPoint, minValue)
    }

    private fun drawValues(
        widthRatio: Float,
        height: Float,
        heightRatio: Float,
        canvas: Canvas,
        firstPoint: PointF,
        minValue: Float
    ) {
        values.forEachIndexed { index, value ->
            if (index == 0) {
                return@forEachIndexed
            }

            val x = leftPadding + (index * widthRatio)
            val y = height - ((value - minValue) * heightRatio)

            canvas.drawLine(
                firstPoint.x, firstPoint.y,
                x, y,
                chartPaint
            )
            firstPoint.x = x
            firstPoint.y = y
        }
    }

    private fun drawGuide(canvas: Canvas, guideLinePosition: Float, value: Float) {
        canvas.drawLine(leftPadding, guideLinePosition, width.toFloat(), guideLinePosition, guidePaint)
        canvas.drawText("${value.format(guideDigits)}$unit", 6F, guideLinePosition + 7F, textPaint)
    }

    private fun Float.format(digits: Int) = "%.${digits}f".format(this)
}