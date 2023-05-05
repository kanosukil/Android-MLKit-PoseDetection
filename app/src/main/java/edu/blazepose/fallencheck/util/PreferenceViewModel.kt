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

class PreferenceViewModel(
    application: Application,
    lensFacing: Int = CameraSelector.LENS_FACING_BACK
) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "Preference ViewModel"
    }

    private val lock = Any()
    private val _lensFacing: MutableLiveData<Int> = MutableLiveData<Int>(lensFacing)
    val facing: LiveData<Int> get() = _lensFacing
    private val _rearSize: MutableLiveData<Size> = MutableLiveData<Size>(Size(1280, 720))
    val rearSize: LiveData<Size> get() = _rearSize
    private val _frontSize: MutableLiveData<Size> = MutableLiveData<Size>(Size(1280, 720))
    val frontSize: LiveData<Size> get() = _frontSize
    private val _isHideInfo: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    val isHideInfo: LiveData<Boolean> = _isHideInfo
    private val _opts: MutableLiveData<PoseDetectorOptionsBase> =
        MutableLiveData<PoseDetectorOptionsBase>(
            PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                .setPreferredHardwareConfigs(PoseDetectorOptions.CPU_GPU)
                .build()
        )
    val opt: LiveData<PoseDetectorOptionsBase> get() = _opts
    private val _isInFrameLikelihood: MutableLiveData<Boolean> = MutableLiveData<Boolean>(true)
    val isInFrameLikelihood: LiveData<Boolean> get() = _isInFrameLikelihood
    private val _isVisualizeZ: MutableLiveData<Boolean> = MutableLiveData<Boolean>(true)
    val isVisualizeZ: LiveData<Boolean> get() = _isVisualizeZ
    private val _isRescaleZForVisualization: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>(true)
    val isRescaleZForVisualization: LiveData<Boolean> get() = _isRescaleZForVisualization

    /**
     * 用户信息
     */
    private val _deviceName: MutableLiveData<String>
    val deviceName: LiveData<String> get() = _deviceName
    private val _emailAddress: MutableLiveData<String>
    val emailAddress: LiveData<String> get() = _emailAddress
    private val _smsAddress: MutableLiveData<String>
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
        }
    }
}