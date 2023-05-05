package edu.blazepose.fallencheck.util

import android.content.Context
import android.os.Looper
import android.widget.Toast

fun shortToast(context: Context, msg: String) {
    try {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    } catch (ne: NullPointerException) {
        // 非同一线程使用时(如通过 Timer() 定时/延时使用)将会出现 NullPointerException
        // 如想强行使用则如下
        Looper.prepare()
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        Looper.loop()
    }
}

fun longToast(context: Context, msg: String) {
    try {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    } catch (ne: NullPointerException) {
        Looper.prepare()
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        Looper.loop()
    }
}