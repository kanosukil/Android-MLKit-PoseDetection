package edu.blazepose.fallencheck.util

import kotlin.math.abs
import kotlin.math.pow

/**
 * 主要是数组处理方法(也包含一些数据处理方法)
 */

/**
 * Float 保留小数
 */
fun round(number: Float, scale: Int = 2): Float {
    val pow = 10f.pow(scale)
    return (number * pow).toInt() / pow
}

/**
 * Double 保留小数
 */
fun round(number: Double, scale: Int = 2): Double {
    val pow = 10.0.pow(scale)
    return (number * pow).toInt() / pow
}

/**
 * FloatArray 转换为 String
 */
fun arrayToString(arr: FloatArray): String {
    var str = "["
    arr.forEach { str = "$str$it," }
    return "${str.removeSuffix(",")}]"
}

/**
 * Array<Float> 转换为 String
 */
fun arrayToString(arr: Array<Float>): String =
    arrayToString(arr.toFloatArray())

/**
 * Array<Float> 的均值
 */
fun average(arr: Array<Float>): Float =
    average(arr.toFloatArray())

/**
 * FloatArray 的均值
 */
fun average(arr: FloatArray): Float {
    var tmp = 0f
    arr.forEach { tmp += it }
    return tmp / arr.size
}

/**
 * 带有均值的 Array<Float> 求方差
 */
fun variance(arr: Array<Float>, ave: Float): Float =
    variance(arr.toFloatArray(), ave)

/**
 * Array<Float> 求方差
 */
fun variance(arr: Array<Float>): Float =
    variance(arr.toFloatArray())

/**
 * 带有均值的 FloatArray 求方差
 */
fun variance(arr: FloatArray, ave: Float): Float {
    var tmp = 0f
    arr.forEach { tmp += abs(it - ave).pow(2) }
    return tmp / arr.size
}

/**
 * FloatArray 求方差
 */
fun variance(arr: FloatArray): Float =
    variance(arr, average(arr))

/**
 * List<String> 转换为 String 调整
 */
fun listToString(times: List<String>): String {
    var tmp = "\n"
    times.forEach {
        tmp += "$it\n"
    }
    return tmp
}