package com.android.picmosaic

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class CropOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var cropRect = RectF()
    private var startX = 0f
    private var startY = 0f
    private var isDrawing = false

    private val overlayPaint = Paint().apply {
        color = Color.BLACK
        alpha = 160
        style = Paint.Style.FILL
    }

    private val cropPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    fun setCropRect(rect: RectF) {
        cropRect.set(rect)
        invalidate()
    }

    fun getCropRect(): RectF = cropRect

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw semi-transparent overlay outside crop area
        canvas.drawRect(0f, 0f, width.toFloat(), cropRect.top, overlayPaint)
        canvas.drawRect(0f, cropRect.bottom, width.toFloat(), height.toFloat(), overlayPaint)
        canvas.drawRect(0f, cropRect.top, cropRect.left, cropRect.bottom, overlayPaint)
        canvas.drawRect(cropRect.right, cropRect.top, width.toFloat(), cropRect.bottom, overlayPaint)

        // Draw crop rectangle border
        canvas.drawRect(cropRect, cropPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                isDrawing = true
                cropRect.set(startX, startY, startX, startY)
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDrawing) {
                    cropRect.set(
                        minOf(startX, event.x),
                        minOf(startY, event.y),
                        maxOf(startX, event.x),
                        maxOf(startY, event.y)
                    )
                    invalidate()
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                isDrawing = false
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}