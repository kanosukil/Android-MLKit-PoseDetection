package edu.blazepose.fallencheck

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import edu.blazepose.fallencheck.notice.AlertViewModel
import edu.blazepose.fallencheck.notice.MailViewModel
import edu.blazepose.fallencheck.notice.NoticeViewModel
import edu.blazepose.fallencheck.notice.SmsViewModel
import edu.blazepose.fallencheck.process.check.FallViewModel
import edu.blazepose.fallencheck.ui.layout.CameraShow
import edu.blazepose.fallencheck.ui.layout.ScreenContainer
import edu.blazepose.fallencheck.util.PreferenceUtils
import edu.blazepose.fallencheck.util.PreferenceViewModel

/**
 * 显示相机预览的 Activity
 */
class CameraActivity : ComponentActivity() {
    // 相机配置 ViewModel
    lateinit var noticeViewModel: NoticeViewModel
    private lateinit var alertViewModel: AlertViewModel
    private lateinit var mailViewModel: MailViewModel
    private lateinit var smsViewModel: SmsViewModel
    lateinit var preferenceViewModel: PreferenceViewModel
    lateinit var fallViewModel: FallViewModel
    var cameraProvider: ProcessCameraProvider? = null
    var cameraSelector: CameraSelector? = null
    var preview: Preview? = null
    var analysis: ImageAnalysis? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        alertViewModel = AlertViewModel(application = application)
        mailViewModel = MailViewModel()
        smsViewModel = SmsViewModel(context = applicationContext)
        preferenceViewModel = PreferenceViewModel(application = application)
        fallViewModel = FallViewModel(application = application)

        noticeViewModel = NoticeViewModel(
            alert = alertViewModel,
            mail = mailViewModel,
            sms = smsViewModel,
            deviceName = PreferenceUtils.getDeviceName(applicationContext),
            emailAddress = PreferenceUtils.getEmailAddress(applicationContext),
            smsAddress = PreferenceUtils.getSmsAddress(applicationContext)
        )

        setContent {
            ScreenContainer(showAppBar = false, window = this.window) {
                CameraShow(activity = this)
            }
        }
    }

    /**
     * 从 Setting Activity 返回时, 更新 PreferenceView Model 中的内容
     */
    override fun onResume() {
        super.onResume()
        // 更新相机配置
        preferenceViewModel.update()
    }

    /**
     * Activity Destory 的同时将 AlertViewModel 的 MediaPlayer 释放, 并将 NoticeViewModel 的疑似跌倒时间列表全部持久化
     */
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onDestroy() {
        super.onDestroy()
        noticeViewModel.cleanSuspectedTime(false)
        alertViewModel.destroy()
    }

    /**
     * CameraX 绑定 Preview 以及 analysis preview
     */
    @Synchronized
    fun cameraBind() {
        cameraProvider?.run {
            unbindAll()
            bindToLifecycle(this@CameraActivity, cameraSelector!!, preview!!, analysis!!)
        }
    }
}