package edu.blazepose.fallencheck.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.common.base.Preconditions
import com.google.common.primitives.Ints

class PoseView(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    abstract class Graphic(
        private val view: PoseView
    ) {
        abstract fun draw(canvas: Canvas)
        fun getApplicationContext(): Context =
            view.context.applicationContext

        fun getTransformationMatrix(): Matrix =
            view.transformationMatrix

        fun isFlipped(): Boolean =
            view.isFlipped

        fun scale(pixel: Float): Float =
            view.scale(pixel)

        fun translateX(x: Float): Float =
            view.translateX(x)

        fun translateY(y: Float): Float =
            view.translateY(y)

        fun postInvalidate() {
            view.postInvalidate()
        }

        protected fun drawText(
            canvas: Canvas,
            text: String,
            x: Float,
            y: Float,
            paint: Paint
        ) {
            canvas.drawText(text, x, y, paint)
        }

        /**
         * 通过给定的 zInImagePixel 为传入的 paint 更新色彩.
         * zInImagePixel 较小时, 色彩更红, 反之更蓝.
         * 体现 Z 值变化.
         *
         * @param paint 更新色彩的内容
         * @param canvas 使用 paint 的画布
         * @param visualizeZ true 时, 色彩将根据 Z 值变化而变化
         * @param rescaleZForVisualization true 时, 将根据 zMin&zMax 重设 Z 值比例, 使色彩更以区分
         * @param zInImagePixel 用于更新 paint 色彩的 Z 值
         * @param zMin 传入 Z 值的最小值
         * @param zMax 传入 Z 值的最大值
         */
        fun updatePaintColorByZValue(
            paint: Paint,
            canvas: Canvas,
            visualizeZ: Boolean,
            rescaleZForVisualization: Boolean,
            zInImagePixel: Float,
            zMin: Float,
            zMax: Float
        ) {
            if (!visualizeZ) {
                return
            }

            // 根据 Z 值设定不同的色彩
            // 获取 Z 值的范围
            val zLowerBoundInScreenPixel: Float
            val zUpperBoundInScreenPixel: Float
            if (rescaleZForVisualization) {
                zLowerBoundInScreenPixel = (-0.001f).coerceAtMost(scale(zMin))
                zUpperBoundInScreenPixel = (0.001f).coerceAtLeast(scale(zMax))
            } else {
                // 默认情况下, 屏幕 Z 值范围 =  [-canvasWidth, canvasWidth]
                zLowerBoundInScreenPixel = -1f * canvas.width
                zUpperBoundInScreenPixel = 1f * canvas.width
            }

            // 获取视图 Z 值
            val zInScreenPixel = scale(zInImagePixel)
            if (zInScreenPixel < 0) {
                // 若对象位于 Z 原点之前, 色彩偏红.
                // [zLowerBoundInScreenPixel, 0) 数值范围应为 [255, 0), 数值越大, 色彩越红.
                val v = Ints.constrainToRange(
                    (zInScreenPixel / zLowerBoundInScreenPixel * 255).toInt(),
                    0,
                    255
                )
                paint.setARGB(255, 255, 255 - v, 255 - v)
            } else {
                // 若对象位于 Z 原点之后, 色彩偏蓝.
                // [0, zUpperBoundInScreenPixel] 数值范围应为 [0, 255], 数值越大, 色彩越蓝.
                val v = Ints.constrainToRange(
                    (zInScreenPixel / zUpperBoundInScreenPixel * 255).toInt(),
                    0,
                    255
                )
                paint.setARGB(255, 255 - v, 255 - v, 255)
            }
        }
    }

    private val lock = Any()
    private val graphics: MutableList<Graphic> = ArrayList()
    private val transformationMatrix: Matrix = Matrix()
    private var postScaleWidthOffset = 0f
    private var postScaleHeightOffset = 0f
    private var isFlipped = false
    private var needUpdateTransformation = true
    var imageWidth = 0
        private set
    var imageHeight = 0
        private set
    var scaleFactor = 1f
        private set

    init {
        addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            needUpdateTransformation = true
        }
    }

    fun scale(pixel: Float): Float =
        pixel * scaleFactor

    fun translateX(x: Float): Float =
        if (isFlipped) {
            width - (scale(x) - postScaleWidthOffset)
        } else {
            scale(x) - postScaleWidthOffset
        }

    fun translateY(y: Float): Float =
        scale(y) - postScaleHeightOffset

    fun clear() {
        synchronized(lock) { graphics.clear() }
        postInvalidate()

    }

    fun add(graphic: Graphic) {
        synchronized(lock) { graphics.add(graphic) }
    }

    fun remove(graphic: Graphic) {
        synchronized(lock) { graphics.remove(graphic) }
        postInvalidate()
    }

    /**
     * 设置检测器处理的图像源信息(尺寸, 是否翻转), 确定转换图像坐标的方式.
     *
     * @param imageWidth 发送给 ML Kit 的图像宽度
     * @param imageHeight 发送给 ML Kit 的图像高度
     * @param isFlipped 图像是否翻转(前置需要翻转)
     */
    fun setImageSourceInfo(
        imageWidth: Int,
        imageHeight: Int,
        isFlipped: Boolean
    ) {
        // 信息检测
        Preconditions.checkState(
            imageWidth > 0,
            "image width must be positive\n" +
                    "图像宽度必须为正数"
        )
        Preconditions.checkState(
            imageHeight > 0,
            "image height must be positive\n" +
                    "图像高度必须为正数"
        )
        // 同步设定信息
        synchronized(lock) {
            this.imageWidth = imageWidth
            this.imageHeight = imageHeight
            this.isFlipped = isFlipped
            needUpdateTransformation = true
        }
        postInvalidate()
    }

    private fun updateTransformationIfNeeded() {
        if (!needUpdateTransformation || (imageWidth <= 0) || (imageHeight <= 0)) {
            return
        }
        // 重算剪切缩放因数
        val imageAspectRatio = imageWidth.toFloat() / imageHeight
        postScaleWidthOffset = 0f
        postScaleHeightOffset = 0f
        if ((width.toFloat() / height) > imageAspectRatio) {
            // 图像需要垂直裁剪才能显示在视图中
            scaleFactor = width.toFloat() / imageWidth
            postScaleHeightOffset = (width.toFloat() / imageAspectRatio - height) / 2
        } else {
            // 图像需要水平裁剪才能显示在视图中
            scaleFactor = height.toFloat() / imageHeight
            postScaleWidthOffset = (height.toFloat() * imageAspectRatio - width) / 2
        }
        // 重设转换矩阵
        transformationMatrix.reset()
        transformationMatrix.setScale(scaleFactor, scaleFactor)
        transformationMatrix.postTranslate(-postScaleWidthOffset, -postScaleHeightOffset)
        // 翻转
        if (isFlipped) {
            transformationMatrix.postScale(-1f, 1f, width / 2f, height / 2f)
        }
        needUpdateTransformation = false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        synchronized(lock) {
            updateTransformationIfNeeded()
            for (graphic in graphics) {
                graphic.draw(canvas = canvas)
            }
        }
    }
}