package edu.blazepose.fallencheck.process.check
//
//import java.util.concurrent.atomic.AtomicBoolean
//
///**
// * 纵向坐标均值阈值
// */
//const val xAveThr: Float = 720f
//
///**
// * 头部纵向速度阈值
// */
//const val xHeadVThr: Float = 1.5f
//
///**
// * 横向坐标方差阈值
// */
//const val yVarThr: Float = 17000f
//
///**
// * 横向坐标范围阈值
// */
//const val yRangeThr: Float = 360f
//
//// 滑窗1
//const val SlideStride: Int = 5
//const val SlideWindow: Int = 30
//var SlideStrideFlag: Int = 0
//const val threholdFlameNumber: Int = 10
//
//// 坐标数
//const val pointNumber: Int = 33 // 总数
//const val headPointNumber: Int = 11 // 头部数
//const val legPointNumber: Int = 10 // 腿部 + 脚部数
//const val legStartIndex = pointNumber - legPointNumber
//
//
///**
// * 窗口数组1
// */
//internal var window =
//    Array(SlideWindow * pointNumber) { FloatArray(2) }
//
///**
// * 步长缓存数组1
// */
//internal var cache =
//    Array(SlideStride * pointNumber) { FloatArray(2) }
//
///**
// * 每一帧的坐标数据存入Cache
// */
//fun cpToCache(pose2D: Array<FloatArray>) {
//    val tmp = SlideStrideFlag * pointNumber
//    for ((i, fa) in pose2D.withIndex()) {
//        cache[tmp + i][0] = fa[0]
//        cache[tmp + i][1] = fa[1]
//    }
//}
//
///**
// * Cache满了时, 存入Window
// */
//fun cpToWin() {
//    backward()
//    cpCacheToWin()
//}
//
///**
// * Window内数据后移
// */
//private fun backward() {
//    for (i in window.size - 1 downTo cache.size) {
//        window[i] = window[i - cache.size]
//    }
//}
//
///**
// * Cache中数据移入Window
// */
//private fun cpCacheToWin() {
//    for ((i, fa) in cache.withIndex()) {
//        window[i] = fa
//    }
//}
//
//
//// 2
///**
// * 摔倒检测新标准
// *
// * Android 横屏 -> 坐标系:
// *
// * y -> 0
// *      ^
// *      |
// *      x
// */
//
///**
// * 1.
// * |x 下半身均值 - threshold1 <= 全身均值
// * or
// * |x |头部均值 - 下半身均值| <= threshold2
// *
// * threshold1 -> 预防双膝跪式跌倒
// * threshold2 -> 头部与下半身之间的 x 值差
// *      1. 常规跌倒 -> 平躺 时, 差距不大
// *      2. 双膝跪式跌倒时, 差距中等(阈值极限)
// *      3. 坐倒式跌倒时, 差距大(超过阈值, 无法鉴别)
// * !: 坐倒式跌倒 -> leg x 方差小 / y 范围大(但不一定大于阈值)
// * !: legAve 比全身以及头部均值都小时, 可以确定为一个跌倒条件(不排除躺下抬腿的情况)
// */
//const val thr_xLegAve: Float = 80f
//const val thr_xHeadToLegAve: Float = 140f
//const val thr_xLegVar: Float = 4000f
//const val thr_yAllToLegRange: Float = 50f
//
///**
// * 2. 必须 x 头部速度 >= threshold
// *
// * 跌倒时头部向下速率突增
// */
//const val thr_xHeadVel: Float = 1.1f
//
///**
// * 3.分情况 y 范围 >= threshold
// *
// * 跌倒时范围大
// * !: 坐倒式 y 范围不一定大于阈值
// */
//const val thr_yRange: Float = 550f
//
//// 3
///**
// * 人体中心线 + 人体外边框 + 中心点/头部中心点速度
// */
//const val cacheSize = 5
//var zeroFrameNumber = 0
//var frameFlag = 0
//var frameFull = false
//val fiveFrame: Array<Array<FloatArray>> =
//    Array(cacheSize) { Array(pointNumber) { FloatArray(2) } }
//
//// 中心点下坠速率阈值
//const val VelCenterThr = 20f
//
//// 中心线角度阈值
//const val AngleCenterThr = 45f
//
//// 外接框宽高比阈值
//const val RatioBoxThr = 1f
//
//// Alert Flag
//val alert: AtomicBoolean = AtomicBoolean(false)