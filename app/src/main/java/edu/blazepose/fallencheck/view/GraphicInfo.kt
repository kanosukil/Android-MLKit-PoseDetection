package edu.blazepose.fallencheck.view

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class GraphicInfo(
    private val view: PoseView,
    private val frameLatency: Long,
    private val detectorLatency: Long,
    private val framesPerSecond: Int?,
    private val textPaint: Paint = Paint(),
    private val showLatencyInfo: Boolean = true
) : PoseView.Graphic(view) {

    companion object {
        private const val TEXT_COLOR = Color.WHITE
        private const val TEXT_SIZE = 60.0f
    }

    init {
        textPaint.color = TEXT_COLOR
        textPaint.textSize = TEXT_SIZE
        textPaint.setShadowLayer(5.0f, 0f, 0f, Color.BLACK)
        postInvalidate()
    }

    @Synchronized
    override fun draw(canvas: Canvas) {
        val x = TEXT_SIZE * 0.5f
        val y = TEXT_SIZE * 1.5f

        // 分辨率
        drawText(
            canvas = canvas,
            text = "输入图片分辨率(宽x高): ${view.imageWidth}x${view.imageHeight}",
            x = x, y = y, paint = textPaint
        )

        if (!showLatencyInfo) {
            return
        }

        // FPS & 延迟
        if (framesPerSecond != null) {
            drawText(
                canvas = canvas,
                text = "FPS: $framesPerSecond, 帧延迟: $frameLatency ms",
                x = x, y = y + TEXT_SIZE, paint = textPaint
            )
        } else {
            drawText(
                canvas = canvas,
                text = "帧延迟: $frameLatency ms",
                x = x, y = y + TEXT_SIZE,
                paint = textPaint
            )
        }

        drawText(
            canvas = canvas,
            text = "检测器延迟: $detectorLatency ms",
            x = x, y = y + TEXT_SIZE * 2,
            paint = textPaint
        )
    }
}