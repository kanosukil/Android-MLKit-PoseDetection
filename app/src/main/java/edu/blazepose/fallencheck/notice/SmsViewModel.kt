package edu.blazepose.fallencheck.notice

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import edu.blazepose.fallencheck.util.listToString


@RequiresApi(Build.VERSION_CODES.M)
class SmsViewModel(context: Context) : ViewModel() {
    companion object {
        private const val TAG = "Sms ViewModel"
    }

    private val smsManager: SmsManager
    private val sentPedingIntent: PendingIntent
    private val deliverIntent: PendingIntent

    init {
        // getSystemService(SmsManager::class.java) 需要 API Level >= 31
        smsManager = context.getSystemService(SmsManager::class.java) ?: SmsManager.getDefault()
        val sent = "SEND_SMS_ACTION"
        // flags = 0 不影响运行
        sentPedingIntent = PendingIntent.getBroadcast(context, 0, Intent(sent), 0)
        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (resultCode) {
                    Activity.RESULT_OK -> Log.w(TAG, "短信发送成功!")
                    SmsManager.RESULT_ERROR_GENERIC_FAILURE -> Log.e(TAG, "短信发送异常: 普通异常")
                    SmsManager.RESULT_ERROR_RADIO_OFF -> Log.e(TAG, "短信发送异常: 无线广播被关闭")
                    SmsManager.RESULT_ERROR_NULL_PDU -> Log.e(TAG, "短信发送异常: 未提供PDU")
                    SmsManager.RESULT_ERROR_NO_SERVICE -> Log.e(TAG, "短信发送异常: 当前服务不可用")
                    SmsManager.RESULT_ERROR_LIMIT_EXCEEDED -> Log.e(TAG, "短信发送异常: 发送队列已满")
                    SmsManager.RESULT_ERROR_FDN_CHECK_FAILURE -> Log.e(TAG, "短信发送异常: 错误检测失败")
                    SmsManager.RESULT_ERROR_SHORT_CODE_NOT_ALLOWED -> Log.e(TAG, "短信发送异常: 短码-不允许")
                    SmsManager.RESULT_ERROR_SHORT_CODE_NEVER_ALLOWED -> Log.e(TAG, "短信发送异常: 短码-从不允许")
                    SmsManager.RESULT_RADIO_NOT_AVAILABLE -> Log.e(TAG, "短信发送异常: 无线广播不可用")
                    SmsManager.RESULT_NETWORK_REJECT -> Log.e(TAG, "短信发送异常: 网络拒绝")
                    SmsManager.RESULT_INVALID_ARGUMENTS -> Log.e(TAG, "短信发送异常: 无效参数")
                    SmsManager.RESULT_INVALID_STATE -> Log.e(TAG, "短信发送异常: 无效状态")
                    SmsManager.RESULT_NO_MEMORY -> Log.e(TAG, "短信发送异常: 内存不足")
                    else -> Log.e(TAG, "短信发送异常: Code$resultCode")
                }
            }
        }, IntentFilter(sent))

        val delivered = "DELIVERED_SMS_ACTION"
        // flags = 0 不影响运行
        deliverIntent = PendingIntent.getBroadcast(context, 0, Intent(delivered), 0)
        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.w(TAG, "短信成功接收")
            }
        }, IntentFilter(delivered))
    }

    /**
     * 双卡存在时, 默认采用 SIM1 卡发送
     * 当 Message 长度超过 70 个汉字(貌似)时, 需要拆分成多条短信发送
     *
     * sendTextMessage() 的 sentIntent(监听发送状态) & deliveryIntent(监听接收状态) 在没有监听需求下可为 null
     */
    fun sendSms(toAddress: String, device: String, times: List<String>) {

        val message = "设备[$device]于\n${listToString(times)}\n检测到疑似跌倒事件(仅最后为确定事件)."
        if (message.length > 70) {
            val divideContents: List<String> =
                smsManager.divideMessage(message)
            for (msg in divideContents) {
                smsManager.sendTextMessage(toAddress, null, msg, sentPedingIntent, deliverIntent)
            }
        } else {
            smsManager.sendTextMessage(toAddress, null, message, sentPedingIntent, deliverIntent)
        }
    }
}