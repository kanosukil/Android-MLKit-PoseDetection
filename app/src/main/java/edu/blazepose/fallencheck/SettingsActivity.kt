package edu.blazepose.fallencheck

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.text.InputType
import android.util.Size
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import edu.blazepose.fallencheck.util.PreferenceUtils
import edu.blazepose.fallencheck.util.shortToast

/**
 * Preference Setting Activity
 */
class SettingsActivity : AppCompatActivity() {
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.xml.setting) // 绑定占位符

        // 配置 PreferenceFragmentCompat
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, CameraXSettingFragment())
            .commit()
        supportActionBar?.title = getString(R.string.pref_screen_title_camerax_source)
    }

    class CameraXSettingFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preference_live_settings, rootKey)
            setUpPreference() // 分辨率设置
            setUpEditText() // 文本
            setUpSwitch() // 切换按钮

            // 检测模式
            findPreference<ListPreference>(getString(R.string.pref_key_performance_mode))?.let {
                it.setOnPreferenceChangeListener { _, newValue ->
                    PreferenceUtils.saveString(
                        requireContext(),
                        R.string.pref_key_performance_mode,
                        newValue.toString()
                    )
                    true
                }
            }
        }

        /**
         * SwitchPreference 配置
         */
        private fun setUpSwitch() {
            // 隐藏检测信息 pref_key_info_hide
            switchListen(R.string.pref_key_info_hide)
            // 开启 GPU 加速 pref_key_prefer_gpu
            switchListen(R.string.pref_key_prefer_gpu)
            // 显示 InFrame Likelihood 值 pref_key_show_in_frame_likelihood
            switchListen(R.string.pref_key_show_in_frame_likelihood)
            // Z 值可视化 pref_key_visualize_z
            switchListen(R.string.pref_key_visualize_z)
            // 缩放 Z 值以进行可视化 pref_key_rescale_z
            switchListen(R.string.pref_key_rescale_z)
        }

        /**
         * 统一配置 SwitchPreference 变化监听器
         */
        private fun switchListen(@StringRes prefkey: Int) {
            findPreference<SwitchPreference>(getString(prefkey))?.let {
                it.setOnPreferenceChangeListener { _, newValue ->
                    PreferenceUtils.saveBoolean(
                        requireContext(),
                        prefkey,
                        newValue as Boolean
                    )
                    true
                }
            }
        }

        /**
         * 配置 EditTextPreference(Summary / 变化监听 / defaultValue / 输入类型)
         */
        private fun setUpEditText() {
            findPreference<EditTextPreference>(getString(R.string.pref_key_email_address))?.let {
                it.setOnBindEditTextListener { et ->
                    et.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                }
                it.setOnPreferenceChangeListener { _, newValue ->
                    newValue.toString().run {
                        it.summary = this
                        PreferenceUtils.saveString(
                            requireContext(),
                            R.string.pref_key_email_address,
                            this
                        )
                    }
                    true
                }
                it.summary = PreferenceUtils.getEmailAddress(requireContext())?.ifBlank { "未设置" } ?: "未设置"
                it.setDefaultValue(it.summary)
            }
            findPreference<EditTextPreference>(getString(R.string.pref_key_sms_address))?.let {
                it.setOnBindEditTextListener { et -> et.inputType = InputType.TYPE_CLASS_PHONE }
                it.setOnPreferenceChangeListener { _, newValue ->
                    newValue.toString().run {
                        it.summary = this
                        PreferenceUtils.saveString(
                            requireContext(),
                            R.string.pref_key_sms_address,
                            this
                        )
                    }
                    true
                }
                it.summary = PreferenceUtils.getSmsAddress(requireContext())?.ifBlank { "未设置" } ?: "未设置"
                it.setDefaultValue(it.summary)
            }
            findPreference<EditTextPreference>(getString(R.string.pref_key_device_name))?.let {
                it.setOnBindEditTextListener { et -> et.inputType = InputType.TYPE_CLASS_TEXT }
                it.setOnPreferenceChangeListener { _, newValue ->
                    newValue.toString().run {
                        it.summary = this
                        PreferenceUtils.saveString(
                            requireContext(),
                            R.string.pref_key_device_name,
                            this
                        )
                    }
                    true
                }
                it.summary = PreferenceUtils.getDeviceName(requireContext())
                it.setDefaultValue(it.summary)
            }
        }

        /**
         * 获得相机属性 ID
         */
        private fun getCameraCharacteristics(lensFacing: Int): CameraCharacteristics? {
            val cManager =
                requireContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager
            try {
                // 遍历可用的 Camera ID
                for (availableID in cManager.cameraIdList) {
                    cManager.getCameraCharacteristics(availableID).let {
                        it.get(CameraCharacteristics.LENS_FACING)?.run {
                            // 返回朝向正确的 Camera 信息
                            if (equals(lensFacing)) {
                                return@getCameraCharacteristics it
                            }
                        }
                    }
                }
            } catch (ex: CameraAccessException) {
                shortToast(requireContext(), "获取 CameraID 信息异常")
            }
            return null
        }

        /**
         * 获得设备支持的分辨率并进行配置
         */
        private fun setUpCameraXSize(
            @StringRes previewSizePrefKeyId: Int,
            lensFacing: Int
        ) {
            findPreference<ListPreference>(getString(previewSizePrefKeyId))?.let {
                // 获取 Size 信息
                val characteristics = getCameraCharacteristics(lensFacing)
                val entries: Array<String> =
                    if (characteristics == null) {
                        arrayOf(
                            "2000x2000", "1600x1600",
                            "1200x1200", "1000x1000",
                            "800x800", "600x600",
                            "400x400", "200x200",
                            "100x100",
                        )
                    } else {
                        characteristics
                            .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
                            .getOutputSizes(SurfaceTexture::class.java)
                            .run { Array(size) { i -> this[i].toString() } }
                    }
                // 设置 Size 显示内容
                it.entries = entries
                it.entryValues = entries
                if (PreferenceUtils.getCameraXTargetResolution(
                        context = requireContext(),
                        lensFacing = lensFacing
                    ) == null
                ) {
                    Size(1080, 1080).toString().let { sz ->
                        if (sz in entries) {
                            it.value = sz
                        }
                    }
                }
                it.summary = it.entry?.toString() ?: "Defaults"
                // 设置 Size 选择处理
                it.setOnPreferenceChangeListener { _, newValue ->
                    newValue.toString().run {
                        it.summary = this
                        PreferenceUtils.saveString(requireContext(), previewSizePrefKeyId, this)
                    }
                    true
                }
            }
        }

        /**
         * 配置设备分辨率
         */
        private fun setUpPreference() {
            findPreference<PreferenceCategory>(
                getString(R.string.pref_category_key_camera)
            )?.run {
                // 后置摄像头配置
                setUpCameraXSize(
                    R.string.pref_key_camerax_rear_target_resolution,
                    CameraSelector.LENS_FACING_BACK
                )
                // 前置摄像头配置
                setUpCameraXSize(
                    R.string.pref_key_camerax_front_target_resolution,
                    CameraSelector.LENS_FACING_FRONT
                )
            }
        }
    }
}