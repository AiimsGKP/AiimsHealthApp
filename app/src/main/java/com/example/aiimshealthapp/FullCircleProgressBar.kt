package com.example.aiimshealthapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class FullCircleProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var progressBarWidth = 40f // Default width

    private val backgroundPaint = Paint().apply {
        strokeWidth = progressBarWidth
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
    }

    private val foregroundPaint = Paint().apply {
        strokeWidth = progressBarWidth
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
    }

    private var progress = 0
    private var max = 100 // Default max value is 100
    private var gradientColors: IntArray? = null

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.FullCircleProgressBar,
            0, 0
        ).apply {
            try {
                backgroundPaint.color = getColor(
                    R.styleable.FullCircleProgressBar_fullBackgroundColor,
                    ContextCompat.getColor(context, android.R.color.darker_gray)
                )
                val gradientStartColor = getColor(
                    R.styleable.FullCircleProgressBar_fullGradientStartColor,
                    ContextCompat.getColor(context, android.R.color.holo_blue_light)
                )
                val gradientEndColor = getColor(
                    R.styleable.FullCircleProgressBar_fullGradientEndColor,
                    ContextCompat.getColor(context, android.R.color.holo_blue_dark)
                )
                gradientColors = intArrayOf(gradientStartColor, gradientEndColor)

                // Fetch progressBarWidth from the custom attribute
                progressBarWidth = getDimension(
                    R.styleable.FullCircleProgressBar_progressBarWidth,
                    40f
                )
                backgroundPaint.strokeWidth = progressBarWidth
                foregroundPaint.strokeWidth = progressBarWidth
            } finally {
                recycle()
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        gradientColors?.let { colors ->
            val shader = LinearGradient(
                0f, 0f, width.toFloat(), 0f,
                colors,
                null,
                Shader.TileMode.CLAMP
            )
            foregroundPaint.shader = shader
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width
        val height = height
        val radius = Math.min(width, height) / 2f
        val cx = width / 2f
        val cy = height / 2f

        // Draw background circle
        canvas.drawCircle(cx, cy, radius - backgroundPaint.strokeWidth / 2, backgroundPaint)

        // Draw foreground circle
        val sweepAngle = (progress / max.toFloat()) * 360f
        canvas.drawArc(
            cx - radius + backgroundPaint.strokeWidth / 2,
            cy - radius + backgroundPaint.strokeWidth / 2,
            cx + radius - backgroundPaint.strokeWidth / 2,
            cy + radius - backgroundPaint.strokeWidth / 2,
            -90f,
            sweepAngle,
            false,
            foregroundPaint
        )
    }

    // Function to set progress
    fun setProgress(progress: Int) {
        this.progress = progress.coerceIn(0, max) // Ensure progress is within bounds
        invalidate()
    }

    // Function to set max value
    fun setMax(max: Int) {
        if (max > 0) {
            this.max = max
            invalidate() // Redraw the view with the new max value
        }
    }

    // Function to get max value
    fun getMax(): Int {
        return max
    }
}
