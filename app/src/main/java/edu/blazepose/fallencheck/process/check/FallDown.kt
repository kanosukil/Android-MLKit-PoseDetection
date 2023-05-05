package edu.blazepose.fallencheck.process.check
//
//import android.content.Context
//import android.util.Log
//import edu.blazepose.fallencheck.util.arrayToString
//import edu.blazepose.fallencheck.util.average
//import edu.blazepose.fallencheck.util.round
//import edu.blazepose.fallencheck.util.variance
//import edu.blazepose.fallencheck.view.PoseView
//import kotlin.math.PI
//import kotlin.math.absoluteValue
//import kotlin.math.atan2
//import kotlin.math.min
//import kotlin.math.pow
//import com.google.mlkit.vision.pose.PoseLandmark as PLM
//
///**
// * 跌倒检测
// */
//object FallDown {
//    private val TAG = this::class.java.simpleName
//
//    /**
//     * 数据处理
//     */
//    fun check(landmarks: List<PLM>, view: PoseView) {
//        val pose2D = Array(pointNumber) { FloatArray(2) }
//        for ((i, lm) in landmarks.withIndex()) {
//            pose2D[i][0] = view.translateX(lm.position3D.x)
//            pose2D[i][1] = view.translateY(lm.position3D.y)
//        }
//        if (pose2D[0][0] != 0f && pose2D[0][1] != 0f) {
//            // 1
////        // 存储到Cache中
////        cpToCache(pose2D)
////        SlideStrideFlag++
////        // 判定Cache是否已满, 满了则开始跌倒检测
////        if (SlideStrideFlag == SlideStride) {
////            cpToWin()
////            SlideStrideFlag = 0
////            check(view.context)
////        }
//            //2
////            check(
////                point2D = pose2D,
////                context = view.context
////            )
//            //3
//            zeroFrameNumber = 0
//            for ((index, pose) in pose2D.withIndex()) {
//                fiveFrame[frameFlag][index][0] = pose[0]
//                fiveFrame[frameFlag][index][1] = pose[1]
//            }
//            frameFlag++
//            if (frameFlag == cacheSize) {
//                frameFull = true
//                frameFlag = 0
//            }
//            if (frameFull) checkp(view.context)
//        } else {
//            zeroFrameNumber++
//            if (zeroFrameNumber == cacheSize) {
//                frameFlag = 0
//                frameFull = false
//            }
//        }
//    }
//
//    /**
//     * 还是需要窗口判断下坠速度!!
//     */
//    private fun checkp(context: Context) {
//        // 中心点(均值求速度)
////        val center: Array<FloatArray> = Array(cacheSize) { FloatArray(2) }
////        for ((index, frame) in fiveFrame.withIndex()) {
////            center[index] = humanCenter(
////                neck = arrayOf(
////                    (frame[11][0] + frame[12][0]) / 2f,
////                    (frame[11][1] + frame[12][1]) / 2f
////                ),
////                hip = arrayOf(
////                    (frame[23][0] + frame[24][0]) / 2f,
////                    (frame[23][1] + frame[24][1]) / 2f
////                )
////            ).toFloatArray()
////        }
////        val xVel = fallVelocity(Array(cacheSize) { center[it][0] }).absoluteValue
////        val yVel = fallVelocity(Array(cacheSize) { center[it][1] }).absoluteValue
//        val xVel = fallVelocity(Array(cacheSize) { fiveFrame[it][0][0] }).absoluteValue
//        val yVel = fallVelocity(Array(cacheSize) { fiveFrame[it][0][1] }).absoluteValue
//
//        fiveFrame.forEach {
//            // 中心线角度(以脖子和髋部确定中心线)
//            val angle = humanOrientation(
//                neck = arrayOf(
//                    (it[11][0] + it[12][0]) / 2f,
//                    (it[11][1] + it[12][1]) / 2f
//                ),
//                hip = arrayOf(
//                    (it[23][0] + it[24][0]) / 2f,
//                    (it[23][1] + it[24][1]) / 2f
//                )
//            ).absoluteValue
//            // 外接框宽高比
//            val ratio = humanBoxRatio(
//                x = Array(pointNumber) { i -> it[i][0] },
//                y = Array(pointNumber) { i -> it[i][1] }
//            )
//
//            Log.w(
//                TAG,
//                "?!?!?!?!?!?!: Vx=${
//                    String.format("%8.2f", round(xVel))
//                }|Vy=${
//                    String.format("%8.2f", round(yVel))
//                }|Angle=${
//                    String.format("%8.2f", round(angle))
//                }|Ratio=${
//                    String.format("%8.2f", round(ratio))
//                }"
//            )
//            if (xVel >= VelCenterThr &&
//                angle in 0f..(180 - AngleCenterThr) &&
//                ratio <= RatioBoxThr
//            ) {
//                Log.e(TAG, "?!?!?!?!?!?!: 跌倒了!")
//            }
//        }
//
//
//    }
//
//    /**
//     * 姿态检测1
//     */
//    private fun check(context: Context) {
//        var zeroFlag: Boolean // 全 0 标记
//        val zeroIndexes: MutableList<Int> = mutableListOf() // 全 0 行/列索引记录
//        var firstNotZeroIndex = SlideWindow // 首个非全 0 行/列
//
//        /**
//         * x相关定义
//         */
//        val x = Array(SlideWindow) { FloatArray(pointNumber) } // 全身
//        val xAve = FloatArray(SlideWindow) // 全身均值
//        val xRange = FloatArray(SlideWindow) // 范围
//        val xVar = FloatArray(SlideWindow) // 全身方差
//        val xHeadAve = FloatArray(SlideWindow) // 头部均值
//        val xLegAve = FloatArray(SlideWindow) // 腿部均值
//        val xLegVar = FloatArray(SlideWindow) // 腿部方差
//        val xLegRange = FloatArray(SlideWindow) // 腿部范围
//
//        /**
//         * y相关定义
//         */
//        val y = Array(SlideWindow) { FloatArray(pointNumber) } // 全身
//        val yAve = FloatArray(SlideWindow) // 全身均值
//        val yRange = FloatArray(SlideWindow) // 范围
//        val yVar = FloatArray(SlideWindow) // 全身方差
//        val yHeadAve = FloatArray(SlideWindow) // 头部均值
//        val yLegAve = FloatArray(SlideWindow) // 腿部均值
//        val yLegVar = FloatArray(SlideWindow) // 腿部方差
//        val yLegRange = FloatArray(SlideWindow) // 腿部范围
//
//        // 获取并计算数据
//        for (i in 0 until SlideWindow) {
//            // 每帧处理初始化
//            zeroFlag = true
//            // x,y分类
//            for (j in 0 until pointNumber) {
//                x[i][j] = window[i * pointNumber + j][0]
//                if (zeroFlag && x[i][j] != 0f) zeroFlag = false // 全零
//
//                y[i][j] = window[i * pointNumber + j][1]
//                if (zeroFlag && y[i][j] != 0f) zeroFlag = false // 全零
//            }
//
//            val xLeg = x[i].sliceArray(legStartIndex until pointNumber)
//            xRange[i] = x[i].max() - x[i].min()
//            xHeadAve[i] = average(x[i].sliceArray(0 until headPointNumber))
//            xLegAve[i] = average(xLeg)
//            xAve[i] = average(x[i])
//            xVar[i] = variance(x[i], xAve[i])
//            xLegVar[i] = variance(xLeg, xLegAve[i])
//            xLegRange[i] = xLeg.max() - xLeg.min()
//
//            val yLeg = y[i].sliceArray(legStartIndex until pointNumber)
//            yRange[i] = y[i].maxOrNull()!! - y[i].minOrNull()!!
//            yHeadAve[i] = average(y[i].sliceArray(0 until headPointNumber))
//            yLegAve[i] = average(yLeg)
//            yAve[i] = average(y[i])
//            yVar[i] = variance(y[i], yAve[i])
//            yLegVar[i] = variance(yLeg, yLegAve[i])
//            yLegRange[i] = yLeg.max() - yLeg.min()
//
//            if (zeroFlag) {
//                zeroIndexes.add(i)
//            } else {
//                firstNotZeroIndex = min(firstNotZeroIndex, i)
//            }
//        }
//
//        // 0值少时, 排除0值影响
//        if (zeroIndexes.size in 1..5) {
//            for (zIndex in zeroIndexes) {
//                try {
//                    xAve[zIndex] = xAve[firstNotZeroIndex]
//                    xVar[zIndex] = xVar[firstNotZeroIndex]
//                    xRange[zIndex] = xRange[firstNotZeroIndex]
//                    xHeadAve[zIndex] = xHeadAve[firstNotZeroIndex]
//
//                    yAve[zIndex] = yAve[firstNotZeroIndex]
//                    yVar[zIndex] = yVar[firstNotZeroIndex]
//                    yRange[zIndex] = yRange[firstNotZeroIndex]
//                    yHeadAve[zIndex] = yHeadAve[firstNotZeroIndex]
//                } catch (ioobe: IndexOutOfBoundsException) {
//                    Log.w(TAG, "check 头部均值数组替换异常!", ioobe)
//                    xHeadAve[zIndex] = xAve[firstNotZeroIndex]
//                    yHeadAve[zIndex] = yAve[firstNotZeroIndex]
//                }
//            }
//        }
//
//        // 最小二乘法求速率
//        val xV = fallVelocity(xHeadAve).absoluteValue
//        val yV = fallVelocity(yHeadAve).absoluteValue
//
////        Log.w(
////            TAG,
////            "!!!!!!!!!!!!: \n" +
////                    "xAve: ${arrayToString(xAve)}\n" +
////                    "xHeadAve: ${arrayToString(xHeadAve)}\n" +
////                    "xLegAve: ${arrayToString(xLegAve)}\n" +
////                    "xRange: ${arrayToString(xRange)}\n" +
////                    "xLegRange: ${arrayToString(xLegRange)}\n" +
////                    "xVar: ${arrayToString(xVar)}\n" +
////                    "xLegVar: ${arrayToString(xLegVar)}\n" +
////                    "xV: $xV\n" +
////                    "\n" +
////                    "yAve: ${arrayToString(yAve)}\n" +
////                    "yHeadAve: ${arrayToString(yHeadAve)}\n" +
////                    "yLegAve: ${arrayToString(yLegAve)}\n" +
////                    "yRange: ${arrayToString(yRange)}\n" +
////                    "yLegRange: ${arrayToString(yLegRange)}\n" +
////                    "yVar: ${arrayToString(yVar)}\n" +
////                    "yLegVar: ${arrayToString(yLegVar)}\n" +
////                    "yV: $yV"
////        )
//
//        Log.w(
//            TAG, "!!!!!!!!!!!!: $xV\n" +
//                    "xAve: ${arrayToString(xAve)}\n" +
//                    "yVar: ${arrayToString(yVar)}\n" +
//                    "yRan: ${arrayToString(yRange)}"
//        )
//
//        /**
//         * 1. 头部x速率 >= 阈值
//         * 2.1 下半身x均值 < 全身/头部x均值 (脚朝天)
//         * 2.2 |下半身x均值 - 头部x均值| <= 阈值 (常规 & 双膝跪地)
//         * 2.3 下半身x方差 <= 阈值 (坐倒式)
//         * 3.1 & 2 全身y范围 >= 阈值 (脚朝天 & 常规 & 双膝跪地)
//         * 3.3 |下半身y范围-全身y范围| <= 阈值 (坐倒式)
//         */
//        if (!yV.isNaN() && !xV.isNaN() &&
//            zeroIndexes.size in 0..5 /* &&
//            xV >= thr_xHeadVel */
//        ) {
//            if (!yV.isNaN() && !xV.isNaN() && zeroIndexes.size in 0..5) {
//                var countY = 0 // y大于阈值的数量
//                xAve.forEach {
//                    if (it > xAveThr) countY++
//                }
//                var countXVar = 0 // x方差大于阈值的数量
//                yVar.forEach {
//                    if (it > yVarThr) countXVar++
//                }
//                var countXRange = 0 // x范围大于阈值的数量
//                yRange.forEach {
//                    if (it > yRangeThr) countXRange++
//                }
//                Log.e(TAG, "!!!!!!!!!!!!: $countY - $countXVar - $countXRange")
//                // 是否符合三个特征的判断
//                if (countY >= threholdFlameNumber &&
//                    xV >= xHeadVThr &&
//                    countXVar >= threholdFlameNumber &&
//                    countXRange >= threholdFlameNumber
//                ) {
////                    shortToast(context, "摔倒了!")
//                    Log.e(TAG, "!!!!!!!!!!!!: 跌倒了!")
//                }
//            }
////            // 1
////            var count1 = 0
////            var count2 = 0
////            for (i in 0 until SlideWindow) {
////                if (xLegAve[i] < min(xHeadAve[i], xAve[i])) {
////                    count1++
////                }
////                if (abs(xLegAve[i] - xHeadAve[i]) <= thr_xHeadToLegAve) {
////                    count2++
////                }
////            }
////
////            // 2
////            var count3 = 0
////            if (count1 >= threholdFlameNumber || count2 >= threholdFlameNumber) {
////                yRange.forEach { if (it > thr_yRange) count3++ }
////            } else {
////                xLegVar.forEach { if (it <= thr_xLegVar) count3++ }
////                count3 = if (count3 >= threholdFlameNumber) {
////                    var count4 = 0
////                    for (i in 0 until SlideWindow) {
////                        if (abs(yLegRange[i] - yRange[i]) <= thr_yAllToLegRange) count4++
////                    }
////                    count4
////                } else {
////                    0
////                }
////            }
////
////            Log.e(TAG, "!!!!!!!!!!!!: $count1 - $count2 - $count3")
////
////            if (count3 >= threholdFlameNumber) {
//////                shortToast(context, "摔倒了!")
////                Log.e(TAG, "!!!!!!!!!!!!: 跌倒了!")
////            }
//        }
//    }
//
//    /**
//     * 跌倒的头部速度
//     * 最小二乘法
//     */
//    private fun fallVelocity(arr: FloatArray): Float {
//        try {
//            val n = arr.size
//            var sumX = 0f
//            var sumX2 = 0f
//            var sumY = 0f
//            var sumY2 = 0f
//
//            for ((i, yy) in arr.withIndex()) {
//                sumX += i.toFloat()
//                sumY += yy
//                sumX2 += i.toFloat().pow(2)
//                sumY2 += yy * i
//            }
//
//            /**
//             * n * X1 + sumX * x2 = sumY
//             * sumX * x1 + sumX2 * x2 = sumY2
//             *
//             * x2 = (sumY2 * n - sumX * sumY) / (sumX2 * n - sumX * sumX) = v
//             */
//
//            return (sumY2 * n - sumX * sumY) / (sumX2 * n - sumX * sumX)
//        } catch (ex: Exception) {
//            Log.w(TAG, "头部跌倒速度计算异常!", ex)
//            return Float.NaN
//        }
//    }
//
//    private fun fallVelocity(arr: Array<Float>): Float =
//        fallVelocity(arr.toFloatArray())
//
//    /**
//     * 人体纵向中心线角度
//     */
//    private fun humanOrientation(
//        neck: Array<Float>,
//        hip: Array<Float>
//    ): Float {
//        val dx = neck[0] - hip[0]
//        val dy = neck[1] - hip[1]
////        return round(((atan2(dy, dx) * 180) / PI)).toFloat() - 90
//        return atan2(dy, dx) * 180 / PI.toFloat()
//    }
//
//    /**
//     * 人体中心点坐标
//     */
//    private fun humanCenter(
//        neck: Array<Float>,
//        hip: Array<Float>
//    ): Array<Float> {
//        return arrayOf((neck[0] + hip[0]) / 2, (neck[1] + hip[1]) / 2)
//    }
//
//    /**
//     * 人体外接框宽高比
//     */
//    private fun humanBoxRatio(x: Array<Float>, y: Array<Float>): Float {
//        val x1 = x.min()
//        val y1 = y.min()
//        val x2 = x.max()
//        val y2 = y.max()
//        return (x2 - x1).absoluteValue / (y2 - y1).absoluteValue
//    }
//}
