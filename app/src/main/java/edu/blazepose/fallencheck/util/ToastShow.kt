package edu.blazepose.fallencheck.util

import android.content.Context
import android.os.Looper
import android.widget.Toast

fun shortToast(context: Context, msg: String) {
    try {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    } catch (ne: NullPointerException) {
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