package edu.blazepose.fallencheck.process.check

import android.app.Application
import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.mlkit.vision.pose.PoseLandmark
import edu.blazepose.fallencheck.util.round
import edu.blazepose.fallencheck.util.shortToast
import edu.blazepose.fallencheck.view.PoseView
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.pow

class FallViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val cacheSize: Int = 5 // 缓存大小
        private const val posePointNumber: Int = 33
        private val fiveFrameCache: Array<Array<FloatArray>> =
            Array(cacheSize) { Array(posePointNumber) { FloatArray(2) } } // 姿态缓存区
        private const val VelxThr: Float = 20f
        private const val AngleCenterThr: Float = 45f
        private const val RatioWHBoxThr: Float = 1f

        private const val AlertIntervalMs: Long = 5000L
        private const val TAG = "Fall check ViewModel"
    }

    private var zeroFrameNumber: Int by mutableStateOf(0)
    private var frameFlag: Int by mutableStateOf(0)
    private var isframeCacheFull: Boolean by mutableStateOf(false)
    private var alertFlag: Boolean by mutableStateOf(false)
    private var alertTime: Long by mutableStateOf(0L)

    private var _shouldAlert: MutableLiveData<Boolean> = MutableLiveData(alertFlag)
    val shouldAlert: LiveData<Boolean> get() = _shouldAlert
    fun closeAlert() {
        _shouldAlert.postValue(false)
    }

    // 对外检测接口
    fun check(landmarks: List<PoseLandmark>, view: PoseView) {
        // 5s 内只进行一次警报
        if (alertFlag && SystemClock.elapsedRealtime() - alertTime >= AlertIntervalMs)
            alertFlag = false
        // 数据初始化
        val pose2D: Array<FloatArray> = Array(posePointNumber) { FloatArray(2) }
        for ((i, lm) in landmarks.withIndex()) {
            pose2D[i][0] = view.translateX(lm.position3D.x)
            pose2D[i][1] = view.translateY(lm.position3D.y)
        }
        if (pose2D[0][0] != 0f && pose2D[0][1] != 0f) {
            zeroFrameNumber = 0
            fillCache(pose2D)
            frameFlag++
            checkFlag()
            if (isframeCacheFull) check()
        } else {
            zeroFrameNumber++
            resetCache()
        }
    }

    private fun alert() {
        shortToast(getApplication<Application>().applicationContext, "跌倒了!")
    }

    private fun fillCache(pose2D: Array<FloatArray>) {
        for ((index, pose) in pose2D.withIndex()) {
            fiveFrameCache[frameFlag][index][0] = pose[0]
            fiveFrameCache[frameFlag][index][1] = pose[1]
        }
    }

    private fun checkFlag() {
        if (frameFlag == cacheSize) {
            isframeCacheFull = true
            frameFlag = 0
        }
    }

    private fun resetCache() {
        if (zeroFrameNumber == cacheSize) {
            frameFlag = 0
            isframeCacheFull = false
        }
    }

    /**
     * 计算速度
     */
    private fun velocity(arr: FloatArray): Float {
        try {
            val n = arr.size
            var sumX = 0f
            var sumX2 = 0f
            var sumY = 0f
            var sumY2 = 0f

            for ((i, yy) in arr.withIndex()) {
                sumX += i.toFloat()
                sumY += yy
                sumX2 += i.toFloat().pow(2)
                sumY2 += yy * i
            }

            /**
             * n * X1 + sumX * x2 = sumY
             * sumX * x1 + sumX2 * x2 = sumY2
             *
             * x2 = (sumY2 * n - sumX * sumY) / (sumX2 * n - sumX * sumX) = v
             */

            return (sumY2 * n - sumX * sumY) / (sumX2 * n - sumX * sumX)
        } catch (ex: Exception) {
            Log.w(TAG, "velocity() -> 计算跌倒速度异常: ${ex.localizedMessage}", ex)
            return Float.NaN
        }
    }

    private fun velocity(arr: Array<Float>): Float =
        velocity(arr.toFloatArray())

    /**
     * 求人体纵向中心线角度
     */
    private fun humanCenterOrientation(neck: Array<Float>, hip: Array<Float>): Float {
        val dx = neck[0] - hip[0]
        val dy = neck[1] - hip[1]
        return atan2(dy, dx) * 180 / PI.toFloat()
    }

    /**
     * 求人体纵向中心线角度
     */
    private fun humanCenterOrinetation(neck: FloatArray, hip: FloatArray): Float =
        humanCenterOrientation(neck.toTypedArray(), hip.toTypedArray())

    /**
     * <del>求人体中心点坐标: 用于求下坠速度(已弃用, 精度劣于头部)</del>
     * 求两点中点坐标
     */
    private fun humanCenter(neck: Array<Float>, hip: Array<Float>): Array<Float> {
        return arrayOf((neck[0] + hip[0]) / 2, (neck[1] + hip[1]) / 2)
    }

    /**
     * <del>求人体中心点坐标: 用于求下坠速度(已弃用, 精度劣于头部)</del>
     * 求两点中点坐标
     */
    private fun humanCenter(neck: FloatArray, hip: FloatArray): Array<Float> =
        humanCenter(neck.toTypedArray(), hip.toTypedArray())

    /**
     * 求人体外接框宽高比
     */
    private fun humanBoxWHRatio(x: Array<Float>, y: Array<Float>): Float {
        val x1 = x.max()
        val x2 = x.min()
        val y1 = y.max()
        val y2 = y.min()
        return (x1 - x2) / (y1 - y2)
    }

    /**
     * 求人体外接框宽高比
     */
    private fun humanBoxWHRatio(x: FloatArray, y: FloatArray): Float =
        humanBoxWHRatio(x.toTypedArray(), y.toTypedArray())

    /**
     * 检测主体
     */
    private fun check() {
        // 中心点(均值求速度)
//        val center: Array<FloatArray> = Array(cacheSize) { FloatArray(2) }
//        for ((index, frame) in fiveFrame.withIndex()) {
//            center[index] = humanCenter(
//                neck = arrayOf(
//                    (frame[11][0] + frame[12][0]) / 2f,
//                    (frame[11][1] + frame[12][1]) / 2f
//                ),
//                hip = arrayOf(
//                    (frame[23][0] + frame[24][0]) / 2f,
//                    (frame[23][1] + frame[24][1]) / 2f
//                )
//            ).toFloatArray()
//        }
//        val xVel = fallVelocity(Array(cacheSize) { center[it][0] }).absoluteValue
//        val yVel = fallVelocity(Array(cacheSize) { center[it][1] }).absoluteValue
        // 取鼻子作为头部中心点
        val xVel = velocity(Array(cacheSize) { fiveFrameCache[it][0][0] }).absoluteValue
        val yVel = velocity(Array(cacheSize) { fiveFrameCache[it][0][1] }).absoluteValue

        fiveFrameCache.forEach {
            // 中心线角度(以脖子和髋部确定中心线)
            val angle = humanCenterOrientation(
                neck = humanCenter(it[11], it[12]),
                hip = humanCenter(it[23], it[24])
            ).absoluteValue
            // 外接框宽高比
            val ratio = humanBoxWHRatio(
                x = Array(posePointNumber) { i -> it[i][0] },
                y = Array(posePointNumber) { i -> it[i][1] }
            )

            Log.w(
                TAG,
                "?!?!?!?!?!?!: Vx=${
                    String.format("%8.2f", round(xVel))
                }|Vy=${
                    String.format("%8.2f", round(yVel))
                }|Angle=${
                    String.format("%8.2f", round(angle))
                }|Ratio=${
                    String.format("%8.2f", round(ratio))
                }"
            )

            if (xVel >= VelxThr &&
                angle in 0f..(180 - AngleCenterThr) &&
                ratio <= RatioWHBoxThr
            ) {
                Log.e(TAG, "?!?!?!?!?!?!: 跌倒了!")
                if (!alertFlag) {
                    alertFlag = true
                    _shouldAlert.postValue(alertFlag)
                    alertTime = SystemClock.elapsedRealtime()
                    alert()
                }
            }
        }
    }
}