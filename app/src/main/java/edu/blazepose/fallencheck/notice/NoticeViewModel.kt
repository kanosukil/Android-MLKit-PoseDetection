package edu.blazepose.fallencheck.notice

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.blazepose.fallencheck.util.FileUtils
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

/**
 * 通知主体 ViewModel
 */
@RequiresApi(Build.VERSION_CODES.M)
class NoticeViewModel(
    private val alert: AlertViewModel,
    private val mail: MailViewModel,
    private val sms: SmsViewModel,
    deviceName: String,
    emailAddress: String?,
    smsAddress: String?
) : ViewModel() {
    companion object {
        private const val TAG = "Notice ViewModel"
    }

    // 跌倒事件标识
    private val _isFall: MutableLiveData<Boolean> = MutableLiveData(false)
    val isFall: LiveData<Boolean> get() = _isFall

    // 警戒标识
    private val _isWarn: MutableLiveData<Boolean> = MutableLiveData(false)
    val isWarn: LiveData<Boolean> get() = _isWarn

    // 警戒状态计时器
    private val timer: Timer = Timer()

    private var emailAd by mutableStateOf(emailAddress)
    private var smsAd by mutableStateOf(smsAddress)
    private var device by mutableStateOf(deviceName)

    // 警报处理流程执行标识
    private var notice by mutableStateOf(false)
    private var isPlay by mutableStateOf(false)
    private var isSentE by mutableStateOf(false)
    private var isSentS by mutableStateOf(false)

    // 疑似跌倒事件触发时间存储列表
    private val suspectedTimeList: MutableList<String> = ArrayList()
    fun addTime(time: String) {
        suspectedTimeList.add(time)
        // 在列表长度达到一定值时触发，通报疑似跌倒事件的发生
        if (suspectedTimeList.size >= 20) {
            viewModelScope.launch {
                emailAd?.run {
                    if (this != "") {
                        mail.sendMailSc(this, device, suspectedTimeList)
                        cleanSuspectedTime(false)
                    }
                }
            }
        }
    }

    private fun cleanSuspectedTime(isFall: Boolean = true) {
        var log = ""
        suspectedTimeList.withIndex().forEach { (i, v) ->
            log += if (isFall && i == suspectedTimeList.lastIndex) {
                "确定跌倒时间:$v\n"
            } else {
                "疑似跌倒时间:$v\n"
            }
        }
        FileUtils.saveLog(alert.getApplication(), log)
        suspectedTimeList.clear()
    }

    init {
        // 监听跌倒事件标识
        _isFall.observeForever {
            // 若出现跌倒事件标识置为 true, 则进行一次性警告
            viewModelScope.launch {
                if (it && !notice) {
                    notice = true
                    try {
                        // 闹铃警告
                        if (!isPlay) {
                            alert.play()
                            isPlay = true
                        }
                        // 电子邮件警告
                        emailAd?.run {
                            if (!isSentE && this != "") {
                                mail.sendMail(this, device, suspectedTimeList)
                                isSentE = true
                            }
                        }
                        // 短信警告
                        smsAd?.run {
                            if (!isSentS && this != "") {
                                sms.sendSms(this, device, suspectedTimeList.last())
                                isSentS = true
                            }
                        }
                        // 警告全部完成后, 清空时间记录
                        /*if (isSentE && isSentS)*/ cleanSuspectedTime()
                    } catch (ex: Exception) {
                        Log.e(TAG, "警报流程{ alert::$isPlay - email::$isSentE - sms::$isSentS }")
                        Log.e(TAG, "发出警报异常: ${ex.localizedMessage}", ex)
                        notice = false
                    }
                }
            }
        }
    }

    /**
     * 置警戒状态为 true, 并设置 Timer() 定时检测是否发生跌倒事件
     */
    fun onAlert() {
        _isWarn.postValue(true)
        // 5 分钟过后检测是否发生跌倒事件, 没有发生则退出警戒状态
        timer.schedule(object : TimerTask() {
            override fun run() {
                if (!isFall.value!!) {
                    _isWarn.postValue(false)
                }
            }
        }, 300 * 1000) // 5 分钟后检测
    }

    /**
     * 跌倒发生处理
     */
    fun fallHappen(
        emailAddress: String?,
        smsAddress: String?,
        deviceName: String
    ) {
        emailAd = emailAddress
        smsAd = smsAddress
        device = deviceName
        if (_isWarn.value!!) {
            _isFall.postValue(true)
        } else {
            Log.e(TAG, "跌倒发生状态设置异常: 还未处于警戒模式.")
        }
    }

    /**
     * 解除跌倒事件触发状态
     */
    fun lift() {
        if (_isFall.value!!) {
            notice = false
            if (isPlay) {
                alert.stop()
                isPlay = false
            }
            isSentE = false
            isSentS = false
            _isWarn.postValue(false)
            _isFall.postValue(false)
        }
    }

}