package com.example.aiimshealthapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class FullCircleProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val backgroundPaint = Paint().apply {
        strokeWidth = 40f
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND // Rounded edges for background
    }

    private val foregroundPaint = Paint().apply {
        strokeWidth = 40f
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND // Rounded edges for foreground
    }

    private var progress = 0
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
        val sweepAngle = (progress / 100f) * 360
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

    fun setProgress(progress: Int) {
        this.progress = progress
        invalidate()
    }
}
