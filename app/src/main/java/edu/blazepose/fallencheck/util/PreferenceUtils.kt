package edu.blazepose.fallencheck.util

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import android.util.Size
import androidx.annotation.StringRes
import androidx.camera.core.CameraSelector
import com.google.common.base.Preconditions
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import edu.blazepose.fallencheck.R

/**
 * SharedPreference 工具
 *
 * @see <a href="https://github.com/googlesamples/mlkit/blob/master/android/vision-quickstart/app/src/main/java/com/google/mlkit/vision/demo/preference/PreferenceUtils.java">参考 ML kit QuickStart PreferenceUtils.java</a>
 */
object PreferenceUtils {
    private const val POSE_DETECTOR_PERFORMANCE_MODE_FAST = 1
    private const val TAG = "PreferenceUtils"

    /**
     * 获取 SharedPreferences 对象
     */
    private fun getSharedPreferences(context: Context): SharedPreferences =
        context.getSharedPreferences(TAG, Context.MODE_PRIVATE)

    /**
     * 从 SharedPreferences 当中获取 Boolean 值相关的 Preference
     */
    private fun getSimpleBooleanValue(
        context: Context,
        @StringRes prefKey: Int,
        defVal: Boolean = true
    ): Boolean = getSharedPreferences(context).getBoolean(context.getString(prefKey), defVal)

    /**
     * 获取模式类型优先级
     */
    private fun getModeTypePreferenceValue(
        context: Context,
        @StringRes prefKeyResId: Int,
    ): Int = getSharedPreferences(context).getString(
        context.getString(prefKeyResId),
        POSE_DETECTOR_PERFORMANCE_MODE_FAST.toString()
    )!!.toInt()

    /**
     * 存储 String 到 SharedPreferences 当中
     */
    fun saveString(
        context: Context,
        @StringRes prefKey: Int,
        value: String?
    ) {
        getSharedPreferences(context).edit()
            .putString(context.getString(prefKey), value)
            .apply()
    }

    /**
     * 存储 Boolean 到 SharedPreference 当中
     */
    fun saveBoolean(
        context: Context,
        @StringRes prefKey: Int,
        value: Boolean
    ) {
        getSharedPreferences(context).edit()
            .putBoolean(context.getString(prefKey), value)
            .apply()
    }

    /**
     * 获取姿态检测的 GPU 支持状态
     */
    private fun preferGPU(context: Context): Boolean =
        getSimpleBooleanValue(context, R.string.pref_key_prefer_gpu)

    /**
     * 获取在摄像中的相似帧
     */
    fun shouldShowInFrameLikelihood(context: Context): Boolean =
        getSimpleBooleanValue(context, R.string.pref_key_show_in_frame_likelihood)

    /**
     * 是否显现姿态检测的 Z 值
     */
    fun shouldVisualizeZ(context: Context): Boolean =
        getSimpleBooleanValue(context, R.string.pref_key_visualize_z)

    /**
     * 是否展现姿态检测的重缩放的 Z 值
     */
    fun shouldRescaleZForVisualization(context: Context): Boolean =
        getSimpleBooleanValue(context, R.string.pref_key_rescale_z)

    /**
     * 摄像姿态检测配置
     */
    fun getPoseDetectorOptions(context: Context): PoseDetectorOptionsBase {
        val performanceMode: Int =
            getModeTypePreferenceValue(context, R.string.pref_key_performance_mode)
        val preferGPU: Boolean = preferGPU(context)
        return if (performanceMode == POSE_DETECTOR_PERFORMANCE_MODE_FAST) {
            val builder = PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            if (preferGPU) {
                builder.setPreferredHardwareConfigs(PoseDetectorOptions.CPU_GPU)
            }
            builder.build()
        } else {
            val builder = AccuratePoseDetectorOptions.Builder()
                .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
            if (preferGPU) {
                builder.setPreferredHardwareConfigs(AccuratePoseDetectorOptions.CPU_GPU)
            }
            builder.build()
        }
    }

    /**
     * 从 Shared 中获取 是否隐藏检测信息 的默认值
     */
    fun shouldHideDetectionInfo(context: Context): Boolean = getSharedPreferences(context)
        .getBoolean(context.getString(R.string.pref_key_info_hide), false)

    /**
     * 获取 Shared 中的 CameraX API 中的分辨率
     */
    fun getCameraXTargetResolution(context: Context, lensFacing: Int): Size? {
        Preconditions.checkArgument(
            lensFacing == CameraSelector.LENS_FACING_BACK
                    || lensFacing == CameraSelector.LENS_FACING_FRONT
        )
        val prefKey =
            if (lensFacing == CameraSelector.LENS_FACING_BACK) context.getString(
                R.string.pref_key_camerax_rear_target_resolution
            ) else context.getString(
                R.string.pref_key_camerax_front_target_resolution
            )
        return try {
            Size.parseSize(getSharedPreferences(context).getString(prefKey, null))
        } catch (e: Exception) {
            Log.w(TAG, "获取分辨率列表异常: ${e.localizedMessage}", e)
            null
        }
    }

    /**
     * 设备名称
     */
    fun getDeviceName(context: Context): String = getSharedPreferences(context)
        .getString(
            context.getString(R.string.pref_key_device_name),
            "Device-${Build.BRAND}-${Build.ID}"
        ) ?: "Device-${Build.BRAND}-${Build.ID}"

    /**
     * 电子邮件地址
     */
    fun getEmailAddress(context: Context): String? = getSharedPreferences(context)
        .getString(context.getString(R.string.pref_key_email_address), null)

    /**
     * 电话号码
     */
    fun getSmsAddress(context: Context): String? = getSharedPreferences(context)
        .getString(context.getString(R.string.pref_key_sms_address), null)
}