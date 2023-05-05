package edu.blazepose.fallencheck.util

import android.app.Application
import android.util.Log
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions

/**
 * SharedPreference 值管理
 */
class PreferenceViewModel(
    application: Application,
    lensFacing: Int = CameraSelector.LENS_FACING_BACK
) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "Preference ViewModel"
    }

    // 数据锁
    private val lock = Any()

    // 相机朝向
    private val _lensFacing: MutableLiveData<Int> = MutableLiveData<Int>(lensFacing)
    val facing: LiveData<Int> get() = _lensFacing

    // 后置分辨率
    private val _rearSize: MutableLiveData<Size> = MutableLiveData<Size>(Size(1080, 1080))
    val rearSize: LiveData<Size> get() = _rearSize

    // 前置分辨率
    private val _frontSize: MutableLiveData<Size> = MutableLiveData<Size>(Size(1080, 1080))
    val frontSize: LiveData<Size> get() = _frontSize

    // 隐藏分析信息
    private val _isHideInfo: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    val isHideInfo: LiveData<Boolean> = _isHideInfo

    // 姿态检测处理器配置
    private val _opts: MutableLiveData<PoseDetectorOptionsBase> =
        MutableLiveData<PoseDetectorOptionsBase>(
            PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                .setPreferredHardwareConfigs(PoseDetectorOptions.CPU_GPU)
                .build()
        )
    val opt: LiveData<PoseDetectorOptionsBase> get() = _opts

    // 帧相似
    private val _isInFrameLikelihood: MutableLiveData<Boolean> = MutableLiveData<Boolean>(true)
    val isInFrameLikelihood: LiveData<Boolean> get() = _isInFrameLikelihood

    // Z 值可视化
    private val _isVisualizeZ: MutableLiveData<Boolean> = MutableLiveData<Boolean>(true)
    val isVisualizeZ: LiveData<Boolean> get() = _isVisualizeZ

    // 缩放 Z 值可视化
    private val _isRescaleZForVisualization: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>(true)
    val isRescaleZForVisualization: LiveData<Boolean> get() = _isRescaleZForVisualization

    /**
     * 用户信息
     */
    private val _deviceName: MutableLiveData<String> // 设备名
    val deviceName: LiveData<String> get() = _deviceName
    private val _emailAddress: MutableLiveData<String> // 电子邮件地址
    val emailAddress: LiveData<String> get() = _emailAddress
    private val _smsAddress: MutableLiveData<String> // 电话号码
    val smsAddress: LiveData<String> get() = _smsAddress

    init {
        _deviceName = MutableLiveData(PreferenceUtils.getDeviceName(application))
        _emailAddress = MutableLiveData(PreferenceUtils.getEmailAddress(application))
        _smsAddress = MutableLiveData(PreferenceUtils.getSmsAddress(application))
        update()

        Log.e(
            TAG,
            "!!!!!!!!!!!!!!!!:\n" +
                    " ${facing.value} - ${rearSize.value}" +
                    " - ${frontSize.value} - ${isHideInfo.value} - ${opt.value} - " +
                    "${isInFrameLikelihood.value} - ${isVisualizeZ.value} - " +
                    "${isRescaleZForVisualization.value}\n" +
                    "UserInfo{ " +
                    "Device::${deviceName.value} - " +
                    "Email::${emailAddress.value} - " +
                    "Sms::${smsAddress.value}}"
        )
    }

    fun setFacing(lensFacing: Int) {
        synchronized(lock = lock) {
            _lensFacing.postValue(lensFacing)
        }
    }

    fun update() {
        synchronized(lock = lock) {
            val context = getApplication<Application>().applicationContext
//            _rearSize.postValue(
//                PreferenceUtils.getCameraXTargetResolution(
//                    context,
//                    CameraSelector.LENS_FACING_BACK
//                )
//            )
            _rearSize.value = PreferenceUtils.getCameraXTargetResolution(
                context,
                CameraSelector.LENS_FACING_BACK
            ) ?: rearSize.value
//            _frontSize.postValue(
//                PreferenceUtils.getCameraXTargetResolution(
//                    context,
//                    CameraSelector.LENS_FACING_FRONT
//                )
//            )
            _frontSize.value = PreferenceUtils.getCameraXTargetResolution(
                context,
                CameraSelector.LENS_FACING_FRONT
            ) ?: frontSize.value
//            _isHideInfo.postValue(
//                PreferenceUtils.shouldHideDetectionInfo(context)
//            )
            _isHideInfo.value =
                PreferenceUtils.shouldHideDetectionInfo(context)
//            _opts.postValue(
//                PreferenceUtils.getPoseDetectorOptions(context)
//            )
            _opts.value =
                PreferenceUtils.getPoseDetectorOptions(context)
//            _isInFrameLikelihood.postValue(
//                PreferenceUtils.shouldShowInFrameLikelihood(context)
//            )
            _isInFrameLikelihood.value =
                PreferenceUtils.shouldShowInFrameLikelihood(context)
//            _isVisualizeZ.postValue(
//                PreferenceUtils.shouldVisualizeZ(context)
//            )
            _isVisualizeZ.value =
                PreferenceUtils.shouldVisualizeZ(context)
//            _isRescaleZForVisualization.postValue(
//                PreferenceUtils.shouldRescaleZForVisualization(context)
//            )
            _isRescaleZForVisualization.value =
                PreferenceUtils.shouldRescaleZForVisualization(context)
//            _deviceName.postValue(PreferenceUtils.getDeviceName(context))
            _deviceName.value = PreferenceUtils.getSmsAddress(context)
//            _emailAddress.postValue(PreferenceUtils.getEmailAddress(context))
            _emailAddress.value = PreferenceUtils.getEmailAddress(context)
//            _smsAddress.postValue(PreferenceUtils.getSmsAddress(context))
            _smsAddress.value = PreferenceUtils.getSmsAddress(context)
        }
    }
}