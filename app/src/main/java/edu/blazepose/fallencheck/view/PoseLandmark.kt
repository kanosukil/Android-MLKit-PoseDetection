package edu.blazepose.fallencheck.view

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.mlkit.vision.pose.Pose
import java.util.Locale
import com.google.mlkit.vision.pose.PoseLandmark as PLM

class PoseLandmark(
    view: PoseView,
    private val pose: Pose,
    private val showInFrameLikelihood: Boolean, // 帧相似展示标识
    private val visualizeZ: Boolean, // Z 值可视化标识
    private val rescaleZForVisualization: Boolean // Z 值色彩变幻标识
) : PoseView.Graphic(view) {

    companion object {
        private const val DOT_RADIUS = 8.0f // 点半径
        private const val TEXT_SIZE = 30.0f // 帧相似文本大小
        private const val STROKE_WIDTH = 10.0f // 线宽
    }

    private var zMin = Float.MAX_VALUE
    private var zMax = Float.MIN_VALUE
    private val leftPaint: Paint = Paint() // 左侧涂料
    private val rightPaint: Paint = Paint() // 右侧涂料
    private val whitePaint: Paint = Paint() // 默认涂料(白)

    init {
        whitePaint.strokeWidth = STROKE_WIDTH
        whitePaint.color = Color.WHITE
        whitePaint.textSize = TEXT_SIZE

        leftPaint.strokeWidth = STROKE_WIDTH
        leftPaint.color = Color.GREEN

        rightPaint.strokeWidth = STROKE_WIDTH
        rightPaint.color = Color.YELLOW
    }

    override fun draw(canvas: Canvas) {
        val landmarks = pose.allPoseLandmarks
        if (landmarks.isEmpty()) {
            return
        }

        // 画点
        for (landmark in landmarks) {
            drawPoint(canvas, landmark, whitePaint)
            if (visualizeZ && rescaleZForVisualization) {
                zMin = zMin.coerceAtMost(landmark.position3D.z)
                zMax = zMax.coerceAtLeast(landmark.position3D.z)
            }
        }

        // 获取姿态坐标
        val nose = pose.getPoseLandmark(PLM.NOSE)
        val leftEyeInner = pose.getPoseLandmark(PLM.LEFT_EYE_INNER)
        val leftEye = pose.getPoseLandmark(PLM.LEFT_EYE)
        val leftEyeOuter = pose.getPoseLandmark(PLM.LEFT_EYE_OUTER)
        val rightEyeInner = pose.getPoseLandmark(PLM.RIGHT_EYE_INNER)
        val rightEye = pose.getPoseLandmark(PLM.RIGHT_EYE)
        val rightEyeOuter = pose.getPoseLandmark(PLM.RIGHT_EYE_OUTER)
        val leftEar = pose.getPoseLandmark(PLM.LEFT_EAR)
        val rightEar = pose.getPoseLandmark(PLM.RIGHT_EAR)
        val leftMouth = pose.getPoseLandmark(PLM.LEFT_MOUTH)
        val rightMouth = pose.getPoseLandmark(PLM.RIGHT_MOUTH)
        val leftShoulder = pose.getPoseLandmark(PLM.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PLM.RIGHT_SHOULDER)
        val leftElbow = pose.getPoseLandmark(PLM.LEFT_ELBOW)
        val rightElbow = pose.getPoseLandmark(PLM.RIGHT_ELBOW)
        val leftWrist = pose.getPoseLandmark(PLM.LEFT_WRIST)
        val rightWrist = pose.getPoseLandmark(PLM.RIGHT_WRIST)
        val leftHip = pose.getPoseLandmark(PLM.LEFT_HIP)
        val rightHip = pose.getPoseLandmark(PLM.RIGHT_HIP)
        val leftKnee = pose.getPoseLandmark(PLM.LEFT_KNEE)
        val rightKnee = pose.getPoseLandmark(PLM.RIGHT_KNEE)
        val leftAnkle = pose.getPoseLandmark(PLM.LEFT_ANKLE)
        val rightAnkle = pose.getPoseLandmark(PLM.RIGHT_ANKLE)
        val leftPinky = pose.getPoseLandmark(PLM.LEFT_PINKY)
        val rightPinky = pose.getPoseLandmark(PLM.RIGHT_PINKY)
        val leftIndex = pose.getPoseLandmark(PLM.LEFT_INDEX)
        val rightIndex = pose.getPoseLandmark(PLM.RIGHT_INDEX)
        val leftThumb = pose.getPoseLandmark(PLM.LEFT_THUMB)
        val rightThumb = pose.getPoseLandmark(PLM.RIGHT_THUMB)
        val leftHeel = pose.getPoseLandmark(PLM.LEFT_HEEL)
        val rightHeel = pose.getPoseLandmark(PLM.RIGHT_HEEL)
        val leftFootIndex = pose.getPoseLandmark(PLM.LEFT_FOOT_INDEX)
        val rightFootIndex = pose.getPoseLandmark(PLM.RIGHT_FOOT_INDEX)

        // 画线
        // Face
        drawLine(canvas, nose, leftEyeInner, whitePaint)
        drawLine(canvas, leftEyeInner, leftEye, whitePaint)
        drawLine(canvas, leftEye, leftEyeOuter, whitePaint)
        drawLine(canvas, leftEyeOuter, leftEar, whitePaint)
        drawLine(canvas, nose, rightEyeInner, whitePaint)
        drawLine(canvas, rightEyeInner, rightEye, whitePaint)
        drawLine(canvas, rightEye, rightEyeOuter, whitePaint)
        drawLine(canvas, rightEyeOuter, rightEar, whitePaint)
        drawLine(canvas, leftMouth, rightMouth, whitePaint)
        drawLine(canvas, leftShoulder, rightShoulder, whitePaint)
        drawLine(canvas, leftHip, rightHip, whitePaint)

        // Left body
        drawLine(canvas, leftShoulder, leftElbow, leftPaint)
        drawLine(canvas, leftElbow, leftWrist, leftPaint)
        drawLine(canvas, leftShoulder, leftHip, leftPaint)
        drawLine(canvas, leftHip, leftKnee, leftPaint)
        drawLine(canvas, leftKnee, leftAnkle, leftPaint)
        drawLine(canvas, leftWrist, leftThumb, leftPaint)
        drawLine(canvas, leftWrist, leftPinky, leftPaint)
        drawLine(canvas, leftWrist, leftIndex, leftPaint)
        drawLine(canvas, leftIndex, leftPinky, leftPaint)
        drawLine(canvas, leftAnkle, leftHeel, leftPaint)
        drawLine(canvas, leftHeel, leftFootIndex, leftPaint)

        // Right body
        drawLine(canvas, rightShoulder, rightElbow, rightPaint)
        drawLine(canvas, rightElbow, rightWrist, rightPaint)
        drawLine(canvas, rightShoulder, rightHip, rightPaint)
        drawLine(canvas, rightHip, rightKnee, rightPaint)
        drawLine(canvas, rightKnee, rightAnkle, rightPaint)
        drawLine(canvas, rightWrist, rightThumb, rightPaint)
        drawLine(canvas, rightWrist, rightPinky, rightPaint)
        drawLine(canvas, rightWrist, rightIndex, rightPaint)
        drawLine(canvas, rightIndex, rightPinky, rightPaint)
        drawLine(canvas, rightAnkle, rightHeel, rightPaint)
        drawLine(canvas, rightHeel, rightFootIndex, rightPaint)

        // 标记数值
        if (showInFrameLikelihood) {
            for (landmark in landmarks) {
                drawText(
                    canvas = canvas,
                    text = String.format(
                        Locale.CHINA,
                        "%.2f",
                        landmark.inFrameLikelihood
                    ),
                    x = translateX(landmark.position.x),
                    y = translateY(landmark.position.y),
                    paint = whitePaint
                )
            }
        }
    }

    /**
     * 画点
     */
    private fun drawPoint(
        canvas: Canvas,
        landmark: PLM,
        paint: Paint
    ) {
        val point = landmark.position3D
        // 根据 Z 值变换色彩
        updatePaintColorByZValue(
            paint, canvas,
            visualizeZ,
            rescaleZForVisualization,
            point.z, zMin, zMax
        )
        // 绘制
        canvas.drawCircle(
            translateX(point.x),
            translateY(point.y),
            DOT_RADIUS,
            paint
        )
    }

    /**
     * 画线
     */
    private fun drawLine(
        canvas: Canvas,
        startLandmark: PLM?,
        endLandmark: PLM?,
        paint: Paint
    ) {
        val start = startLandmark!!.position3D
        val end = endLandmark!!.position3D

        // Z 值色彩变换
        updatePaintColorByZValue(
            paint, canvas,
            visualizeZ,
            rescaleZForVisualization,
            (start.z + end.z) / 2, // 获取当前线条的平均 Z 值
            zMin, zMax
        )

        // 绘制
        canvas.drawLine(
            translateX(start.x),
            translateY(start.y),
            translateX(end.x),
            translateY(end.y),
            paint
        )
    }
}