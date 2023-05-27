package edu.blazepose.fallencheck.util

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object FileUtils {
    private const val TAG = "FileUtils"
    private const val FileName = "FCFall.log"

    /**
     * 存储日志到文件
     * @param context 应用上下文
     * @param log 日志文本
     */
    fun saveLog(context: Context, log: String) {
        var fileOutput: FileOutputStream? = null
        try {
            fileOutput = context.openFileOutput(FileName, Context.MODE_APPEND)
            fileOutput?.write(log.toByteArray(Charsets.UTF_8))
        } catch (e: Exception) {
            Log.e(TAG, "saveLog->写入异常:${e.localizedMessage}", e.cause)
        } finally {
            try {
                fileOutput?.close()
            } catch (e: IOException) {
                Log.e(TAG, "saveLog->关闭异常:${e.localizedMessage}", e.cause)
            }
        }
        Log.w(TAG, "saveLog->${File(context.filesDir, FileName).absolutePath}")
    }

    /**
     * 调用第三方文本读取器读取日志文件
     */
    fun openLog(context: Context) {
        val file = File(context.filesDir, FileName)
        if (!file.exists()) {
            shortToast(context, "日志文件还未创建！")
            return
        } else {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setDataAndType(
                    FileProvider.getUriForFile(
                        context,
                        "edu.blazepose.fallencheck.textprovider",
                        file
                    ),
                    "text/plain"
                )
            }
            context.startActivity(intent)
        }
    }

}