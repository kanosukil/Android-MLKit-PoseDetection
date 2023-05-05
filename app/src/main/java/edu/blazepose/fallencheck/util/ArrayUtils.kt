package edu.blazepose.fallencheck.util

import kotlin.math.abs
import kotlin.math.pow

fun round(number: Float, scale: Int = 2): Float {
    val pow = 10f.pow(scale)
    return (number * pow).toInt() / pow
}

fun round(number: Double, scale: Int = 2): Double {
    val pow = 10.0.pow(scale)
    return (number * pow).toInt() / pow
}

fun arrayToString(arr: FloatArray): String {
    var str = "["
    arr.forEach { str = "$str$it," }
    return "${str.removeSuffix(",")}]"
}

fun arrayToString(arr: Array<Float>): String =
    arrayToString(arr.toFloatArray())


fun average(arr: Array<Float>): Float =
    average(arr.toFloatArray())

fun average(arr: FloatArray): Float {
    var tmp = 0f
    arr.forEach { tmp += it }
    return tmp / arr.size
}

fun variance(arr: Array<Float>, ave: Float): Float =
    variance(arr.toFloatArray(), ave)

fun variance(arr: Array<Float>): Float =
    variance(arr.toFloatArray())

fun variance(arr: FloatArray, ave: Float): Float {
    var tmp = 0f
    arr.forEach { tmp += abs(it - ave).pow(2) }
    return tmp / arr.size
}

fun variance(arr: FloatArray): Float =
    variance(arr, average(arr))

fun listToString(times: List<String>): String {
    var tmp = ""
    times.forEach {
        tmp += "$it\n"
    }
    return tmp
}