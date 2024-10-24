package com.example.aiimshealthapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class SquareCornerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, android.R.color.white)
        strokeWidth = 8f // Thickness of the corner lines
        style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val borderLength = 100 // Length of the corner lines
        val padding = 30 // Decreased padding from edges

        // Define a square that's closer to the edges of the view by decreasing padding
        val rect = Rect(
            padding, padding,
            width - padding, height - padding
        )

        // Draw corner lines
        // Top-left corner
        canvas.drawLine(rect.left.toFloat(), rect.top.toFloat(), rect.left.toFloat() + borderLength, rect.top.toFloat(), paint)
        canvas.drawLine(rect.left.toFloat(), rect.top.toFloat(), rect.left.toFloat(), rect.top.toFloat() + borderLength, paint)

        // Top-right corner
        canvas.drawLine(rect.right.toFloat(), rect.top.toFloat(), rect.right.toFloat() - borderLength, rect.top.toFloat(), paint)
        canvas.drawLine(rect.right.toFloat(), rect.top.toFloat(), rect.right.toFloat(), rect.top.toFloat() + borderLength, paint)

        // Bottom-left corner
        canvas.drawLine(rect.left.toFloat(), rect.bottom.toFloat(), rect.left.toFloat() + borderLength, rect.bottom.toFloat(), paint)
        canvas.drawLine(rect.left.toFloat(), rect.bottom.toFloat(), rect.left.toFloat(), rect.bottom.toFloat() - borderLength, paint)

        // Bottom-right corner
        canvas.drawLine(rect.right.toFloat(), rect.bottom.toFloat(), rect.right.toFloat() - borderLength, rect.bottom.toFloat(), paint)
        canvas.drawLine(rect.right.toFloat(), rect.bottom.toFloat(), rect.right.toFloat(), rect.bottom.toFloat() - borderLength, paint)
    }
}
