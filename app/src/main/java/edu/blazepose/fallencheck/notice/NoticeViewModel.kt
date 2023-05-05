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
import java.util.Timer
import java.util.TimerTask

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

    private val _isFall: MutableLiveData<Boolean> = MutableLiveData(false)
    val isFall: LiveData<Boolean> get() = _isFall
    private val _isWarn: MutableLiveData<Boolean> = MutableLiveData(false)
    val isWarn: LiveData<Boolean> get() = _isWarn
    private val timer: Timer = Timer()

    private var emailAd by mutableStateOf(emailAddress)
    private var smsAd by mutableStateOf(smsAddress)
    private var device by mutableStateOf(deviceName)

    private var notice by mutableStateOf(false)
    private var isPlay by mutableStateOf(false)
    private var isSentE by mutableStateOf(false)
    private var isSentS by mutableStateOf(false)

    private val suspectedTimeList: MutableList<String> = ArrayList()
    fun addTime(time: String) {
        suspectedTimeList.add(time)
    }

    init {
        _isFall.observeForever {
            if (it && !notice) {
                notice = true
                try {
                    if (!isPlay) {
                        alert.play()
                        isPlay = true
                    }
                    emailAd?.run {
                        if (!isSentE) {
                            mail.sendMail(this, device, suspectedTimeList)
                            isSentE = true
                        }
                    }
                    smsAd?.run {
                        if (!isSentS) {
                            sms.sendSms(this, device, suspectedTimeList)
                            isSentS = true
                        }
                    }
                    suspectedTimeList.clear()
                } catch (ex: Exception) {
                    Log.e(TAG, "警报流程{ alert::$isPlay - email::$isSentE - sms::$isSentS }")
                    Log.e(TAG, "发出警报异常: ${ex.localizedMessage}", ex)
                    notice = false
                }
            }
        }
    }

    fun onAlert() {
        _isWarn.postValue(true)
        timer.schedule(object : TimerTask() {
            override fun run() {
                if (!isFall.value!!) {
                    _isWarn.postValue(false)
                }
            }
        }, 300 * 1000) // 5 分钟后检测
    }

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