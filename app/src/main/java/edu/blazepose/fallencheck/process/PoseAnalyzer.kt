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
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase
import edu.blazepose.fallencheck.process.check.FallViewModel
import edu.blazepose.fallencheck.view.GraphicInfo
import edu.blazepose.fallencheck.view.PoseLandmark
import edu.blazepose.fallencheck.view.PoseView
import java.util.Timer
import java.util.TimerTask

class PoseAnalyzer(
    realTimeOpt: PoseDetectorOptionsBase,
    private val showInFrameLikelihood: Boolean = true,
    private val visualizeZ: Boolean = true,
    private val rescaleZForVisualization: Boolean = true,
    private val isHideInfo: Boolean = false,
    private val lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    private val fallCheck: FallViewModel
) : ImageAnalysis.Analyzer {
    // FPS 显示
    private var frameProcessedInOneSecondInterval = 0
    private var framesPerSecond = 0
    private val fpsTimer = Timer().apply {
        scheduleAtFixedRate(
            object : TimerTask() {
                override fun run() {
                    framesPerSecond = frameProcessedInOneSecondInterval
                    frameProcessedInOneSecondInterval = 0
                }
            }, 0, 1000
        )
    }

    private val detector = PoseDetection.getClient(realTimeOpt)

    var view: PoseView? = null

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        val frameStartMs = SystemClock.elapsedRealtime()
        image.image?.let { img ->
            val rotationDegrees = image.imageInfo.rotationDegrees
            val inputImage = InputImage.fromMediaImage(img, rotationDegrees)

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

            val detectorStartMs = SystemClock.elapsedRealtime()
            detector.process(inputImage)
                .addOnSuccessListener { pose ->
                    view?.run {
                        // 执行姿态检测
//                        FallDown.check(landmarks = pose.allPoseLandmarks, view = this)
                        fallCheck.check(landmarks = pose.allPoseLandmarks, view = this)
                        // 延迟
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
                        )
                        if (!isHideInfo) {
                            add(
                                GraphicInfo(
                                    view = this,
                                    frameLatency = frameLatency,
                                    detectorLatency = detectorLatency,
                                    framesPerSecond = framesPerSecond,
                                )
                            )
                        }
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
                    image.close()
                }
        }
    }
}