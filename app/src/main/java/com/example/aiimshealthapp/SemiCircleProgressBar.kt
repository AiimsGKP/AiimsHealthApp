package com.example.aiimshealthapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class SemiCircleProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val backgroundPaint = Paint().apply {
        strokeWidth = 40f
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND // Rounded edges for background
    }

    private val foregroundPaint = Paint().apply {
        strokeWidth = 42f
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND // Rounded edges for foreground
    }

    private var progress = 0
    private var gradientColors: IntArray? = null

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.SemiCircleProgressBar,
            0, 0
        ).apply {
            try {
                backgroundPaint.color = getColor(
                    R.styleable.SemiCircleProgressBar_semiBackgroundColor,
                    ContextCompat.getColor(context, android.R.color.darker_gray)
                )
                val gradientStartColor = getColor(
                    R.styleable.SemiCircleProgressBar_semiGradientStartColor,
                    ContextCompat.getColor(context, android.R.color.holo_blue_light)
                )
                val gradientEndColor = getColor(
                    R.styleable.SemiCircleProgressBar_semiGradientEndColor,
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

        // Draw background semi-circle
        canvas.drawArc(
            30f, 30f, width - 30f, height - 30f,
            180f, 180f, false, backgroundPaint
        )

        // Draw foreground semi-circle
        canvas.drawArc(
            30f, 30f, width - 30f, height - 30f,
            180f, (progress / 100f) * 180, false, foregroundPaint
        )
    }

    fun setProgress(progress: Int) {
        this.progress = progress
        invalidate()
    }
}
