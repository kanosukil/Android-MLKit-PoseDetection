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
import edu.blazepose.fallencheck.util.average
import edu.blazepose.fallencheck.util.round
import edu.blazepose.fallencheck.util.shortToast
import edu.blazepose.fallencheck.view.PoseView
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.pow

/**
 * 跌倒检测 ViewModel
 */
class FallViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val cacheSize: Int = 5 // 缓存大小
        private const val posePointNumber: Int = 33 // BlazePose 模型骨架点数量
        private val fiveFrameCache: Array<Array<FloatArray>> =
            Array(cacheSize) { Array(posePointNumber) { FloatArray(2) } } // 姿态缓存区
        private const val VelXThr: Float = 21f // 下坠速度阈值
        private const val AngleCenterThr: Float = 120f // 人体纵向中心线倾斜角阈值
        private const val RatioWHBoxThr: Float = 0.98f // 人体外接框阈值
        private const val AlertIntervalMs: Long = 5000L // 警戒间隔 5s(放置多次警戒)

        private const val TAG = "Fall check ViewModel"
    }

    private var zeroFrameNumber: Int by mutableStateOf(0) // 清空缓存区中, 之前的内容, 以免影响结果
    private var frameFlag: Int by mutableStateOf(0) // 缓存区索引
    private var isFrameCacheFull: Boolean by mutableStateOf(false) // 缓存区空间是否已满
    private var alertFlag: Boolean by mutableStateOf(false) // 警戒标识
    private var alertTime: Long by mutableStateOf(0L) // 警戒发生时间
    private var fallTime: Long by mutableStateOf(0L)

    // 对外警戒标识
    private var _shouldAlert: MutableLiveData<Boolean> = MutableLiveData(alertFlag)
    val shouldAlert: LiveData<Boolean> get() = _shouldAlert
    fun closeAlert() {
        _shouldAlert.postValue(false)
    }

    fun resetAlert() {
        closeAlert()
        fallTime = 0L
    }

    private fun suspectedFall(msg: String) {
        alertFlag = true
        _shouldAlert.postValue(alertFlag)
        alertTime = SystemClock.elapsedRealtime()
        shortToast(getApplication<Application>().applicationContext, msg)
    }

    /**
     * 对外检测入口
     */
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
            // 检测到人体时
            zeroFrameNumber = 0
            // 填充
            fillCache(pose2D = pose2D)
            if (isFrameCacheFull) check() // 缓存区满时, 每变一帧都进行检测
        } else {
            // 未检测到人体时
            zeroFrameNumber++
            resetCache()
        }
    }

    /**
     * 填充缓存区（队列）
     */
    private fun fillCache(pose2D: Array<FloatArray>) {
        if (frameFlag == fiveFrameCache.lastIndex && isFrameCacheFull) {
            for ((i, _) in fiveFrameCache.withIndex()) {
                if (i == frameFlag) break
                change(i)
            }
        }
        for ((index, pose) in pose2D.withIndex()) {
            fiveFrameCache[frameFlag][index][0] = pose[0]
            fiveFrameCache[frameFlag][index][1] = pose[1]
        }
        frameFlag++
        if (frameFlag == cacheSize) {
            isFrameCacheFull = true
            frameFlag = cacheSize - 1
        }
    }

    /**
     * 数组迁移
     */
    private fun change(idx1: Int, idx2: Int = idx1 + 1) {
        for ((i, _) in fiveFrameCache[idx1].withIndex()) {
            fiveFrameCache[idx1][i][0] = fiveFrameCache[idx2][i][0]
            fiveFrameCache[idx1][i][1] = fiveFrameCache[idx2][i][1]
        }
    }

    /**
     * 未检测到人体时, 连续超过5帧将清空缓存区
     */
    private fun resetCache() {
        if (zeroFrameNumber == cacheSize) {
            frameFlag = 0
            isFrameCacheFull = false
            fallTime = 0L
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
             *
             * 解得 => y = x2 * x + x1 => x2 即为速度
             */

            return (sumY2 * n - sumX * sumY) / (sumX2 * n - sumX * sumX)
        } catch (ex: Exception) {
            Log.w(TAG, "velocity() -> 计算速度异常: ${ex.localizedMessage}", ex)
            return Float.NaN
        }
    }

    /**
     * 计算速度
     */
    private fun velocity(arr: Array<Float>): Float =
        velocity(arr.toFloatArray())

    /**
     * 求人体纵向中心线角度
     */
    private fun humanCenterOrientation(neck: Array<Float>, hip: Array<Float>): Float {
        /**
         * A
         * |\
         * | \
         * |  \
         * |___\
         * B    C
         *
         * tan<BAC = BC / AB
         * atan BC/AB = <BAC
         */
        val dx = neck[0] - hip[0]
        val dy = neck[1] - hip[1]
        return atan2(dy, dx) * 180 / PI.toFloat()
    }

    /**
     * 求人体纵向中心线角度
     */
    private fun humanCenterOrientation(neck: FloatArray, hip: FloatArray): Float =
        humanCenterOrientation(neck.toTypedArray(), hip.toTypedArray())

    /**
     * <del>求人体中心点坐标: 用于求下坠速度(已弃用, 精度劣于头部)</del>
     *
     * 求两点中点坐标
     */
    private fun humanCenter(neck: Array<Float>, hip: Array<Float>): Array<Float> {
        return arrayOf((neck[0] + hip[0]) / 2, (neck[1] + hip[1]) / 2)
    }

    /**
     * <del>求人体中心点坐标: 用于求下坠速度(已弃用, 精度劣于头部)</del>
     *
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
        // 取头部均值作为头部中心点
        val headXAve = Array(cacheSize) { i ->
            val array = fiveFrameCache[i].sliceArray(0..10)
            average(Array(11) { j -> array[j][0] })
        }
        val headYAve = Array(cacheSize) { i ->
            val array = fiveFrameCache[i].sliceArray(0..10)
            average(Array(11) { j -> array[j][1] })
        }
        val xVel = velocity(headXAve)
        val yVel = velocity(headYAve)
        // 取鼻子作为头部中心点
//        val xVel = velocity(Array(cacheSize) { fiveFrameCache[it][0][0] })
//        val yVel = velocity(Array(cacheSize) { fiveFrameCache[it][0][1] })

        // 对检测缓存区的每一帧都进行计算
        fiveFrameCache.forEach {
            // 中心线角度(以[脖子]和[髋部与膝盖的中点]确定中心线)
            val angle = humanCenterOrientation(
                neck = humanCenter(it[11], it[12]),
                hip = humanCenter(
                    humanCenter(it[23], it[24]),
                    humanCenter(it[25], it[26])
                )
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
                }|Time=$fallTime"
            )

            /**
             * (由于实际通过横屏检测, 相机旋转并未改变, 因此使用x作为纵向移动轴,
             * 角度: 从头向下指为 x 正向, 因此 人物站立时 角度基本为 180 度, 因此采用 0 ~ 180 - thr 的表示法)
             * <p></p>特性1: 头部下坠速度 大于 阈值
             * <p></p>特性2: 人体纵向中心角 大于 阈值
             * <p></p>特性3: 人体外接框宽高比 大于 阈值
             */
            if (
                angle in 0f..AngleCenterThr &&
                ratio <= RatioWHBoxThr
            ) {
                if (fallTime != 0L && fallTime < 1100L) fallTime++
                if (fallTime >= 1024L && !alertFlag) suspectedFall("长时间跌倒未恢复！")
                if (xVel >= VelXThr) {
                    // &&  可以将速度判定移至 foreach 之外，减少计算次数，提高检测效率。
                    Log.e(TAG, "?!?!?!?!?!?!: 跌倒了!")
                    if (!alertFlag) suspectedFall("跌倒了!")
                    fallTime++
                }
            } else if (
                angle !in 0f..AngleCenterThr ||
                ratio > RatioWHBoxThr
            ) {
                fallTime = 0L
            }
        }
    }
}