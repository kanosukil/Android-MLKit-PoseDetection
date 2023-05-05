package edu.blazepose.fallencheck.process

import android.os.SystemClock
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase
import edu.blazepose.fallencheck.process.check.FallViewModel
import edu.blazepose.fallencheck.view.GraphicInfo
import edu.blazepose.fallencheck.view.PoseLandmark
import edu.blazepose.fallencheck.view.PoseView
import java.util.Timer
import java.util.TimerTask

/**
 * CameraX 分析器
 */
class PoseAnalyzer(
    realTimeOpt: PoseDetectorOptionsBase,
    private val lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    private val fallCheck: FallViewModel
) : ImageAnalysis.Analyzer {
    // FPS 显示
    private var frameProcessedInOneSecondInterval = 0 // 记录
    private var framesPerSecond = 0 // 显示
    private val fpsTimer = Timer().apply {
        // 定时任务, 每秒统计帧数
        scheduleAtFixedRate(
            object : TimerTask() {
                override fun run() {
                    framesPerSecond = frameProcessedInOneSecondInterval
                    frameProcessedInOneSecondInterval = 0
                }
            }, 0, 1000
        )
    }

    private var showInFrameLikelihood: Boolean = true
    private var visualizeZ: Boolean = true
    private var rescaleZForVisualization: Boolean = true
    private var isHideInfo: Boolean = false

    // 姿态检测器
    private var detector: PoseDetector = PoseDetection.getClient(realTimeOpt)

    // 姿态分析结果展示界面
    var view: PoseView? = null

    fun setRealTimeOpt(opt: PoseDetectorOptionsBase) {
        detector = PoseDetection.getClient(opt)
    }

    fun setShow(
        showInFrameLikelihood: Boolean = this.showInFrameLikelihood,
        visualizeZ: Boolean = this.visualizeZ,
        rescaleZForVisualization: Boolean = this.rescaleZForVisualization,
        isHideInfo: Boolean = this.isHideInfo,
    ) {
        this.showInFrameLikelihood = showInFrameLikelihood
        this.visualizeZ = visualizeZ
        this.rescaleZForVisualization = rescaleZForVisualization
        this.isHideInfo = isHideInfo
    }

    /**
     * 分析主体
     */
    @OptIn(ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        // 帧开始处理时间
        val frameStartMs = SystemClock.elapsedRealtime()
        image.image?.let { img ->
            // 图像初始处理
            val rotationDegrees = image.imageInfo.rotationDegrees
            val inputImage = InputImage.fromMediaImage(img, rotationDegrees)

            // 分析界面处理
            view?.run {
                val isFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT
                if (rotationDegrees == 0 || rotationDegrees == 180) {
                    setImageSourceInfo(
                        imageWidth = img.width,
                        imageHeight = img.height,
                        isFlipped
                    )
                } else {
                    setImageSourceInfo(
                        imageWidth = img.height,
                        imageHeight = img.width,
                        isFlipped
                    )
                }
            }

            // 处理器开始时间
            val detectorStartMs = SystemClock.elapsedRealtime()
            // 图像分析处理
            detector.process(inputImage)
                .addOnSuccessListener { pose ->
                    view?.run {
                        // 执行姿态检测
//                        FallDown.check(landmarks = pose.allPoseLandmarks, view = this)
                        fallCheck.check(landmarks = pose.allPoseLandmarks, view = this)
                        // 延迟统计
                        val endMs = SystemClock.elapsedRealtime()
                        val frameLatency = endMs - frameStartMs
                        val detectorLatency = endMs - detectorStartMs
                        frameProcessedInOneSecondInterval++
                        // 布局
                        clear()
                        add(
                            PoseLandmark(
                                view = this,
                                pose = pose,
                                showInFrameLikelihood = showInFrameLikelihood,
                                visualizeZ = visualizeZ,
                                rescaleZForVisualization = rescaleZForVisualization
                            )
                        ) // 骨架姿态展示
                        if (!isHideInfo) {
                            add(
                                GraphicInfo(
                                    view = this,
                                    frameLatency = frameLatency,
                                    detectorLatency = detectorLatency,
                                    framesPerSecond = framesPerSecond,
                                )
                            )
                        } // 处理信息展示
                        postInvalidate()
                    }
                }
                .addOnFailureListener { ex ->
                    Log.e(this::class.java.simpleName, "分析器异常: ${ex.localizedMessage}", ex)
                    view?.run {
                        clear()
                        postInvalidate()
                    }
                    fpsTimer.cancel()
                }
                .addOnCanceledListener {
                    fpsTimer.cancel()
                }
                .addOnCompleteListener {
                    // ImageProxy 对象在处理完成后必须关闭
                    image.close()
                }
        }
    }
}