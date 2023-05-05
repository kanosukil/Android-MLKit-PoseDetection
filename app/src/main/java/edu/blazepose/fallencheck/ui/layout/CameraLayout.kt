package edu.blazepose.fallencheck.ui.layout

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.content.ContextCompat
import edu.blazepose.fallencheck.CameraActivity
import edu.blazepose.fallencheck.R
import edu.blazepose.fallencheck.SettingsActivity
import edu.blazepose.fallencheck.process.PoseAnalyzer
import edu.blazepose.fallencheck.util.shortToast
import edu.blazepose.fallencheck.view.PoseView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.tooling.preview.Preview as Pr

@RequiresApi(Build.VERSION_CODES.M)
@Composable
fun CameraShow(activity: CameraActivity) {
    val preference = activity.preferenceViewModel
    val lensFacing = preference.facing.observeAsState().value!!
    val realTimeOpt = preference.opt.observeAsState().value!!
    val showInFrameLikelihood = preference.isInFrameLikelihood.observeAsState().value!!
    val visualizeZ = preference.isVisualizeZ.observeAsState().value!!
    val rescaleZForVisualization = preference.isRescaleZForVisualization.observeAsState().value!!
    val isHideInfo = preference.isHideInfo.observeAsState().value!!
    val device = preference.deviceName.observeAsState().value!!
    val email = preference.emailAddress.observeAsState().value
    val sms = preference.smsAddress.observeAsState().value
    activity.cameraSelector = CameraSelector.Builder()
        .requireLensFacing(lensFacing).build()
//    val size = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
//        preference.frontSize.observeAsState().value!!
//    } else {
//        preference.rearSize.observeAsState().value!!
//    }
    val notice = activity.noticeViewModel
    val warn: Boolean = notice.isWarn.observeAsState().value!!
    val isFall: Boolean = notice.isFall.observeAsState().value!!
    val fall = activity.fallViewModel
    fall.shouldAlert.observeAsState().value?.run {
        if (this) {
            val time = SimpleDateFormat("yyyy-MM-dd|HH:mm:ss", Locale.CHINA).format(Date())
            if (!warn) {
                notice.addTime(time)
                notice.onAlert()
                fall.closeAlert()
            } else {
                notice.addTime(time)
                notice.fallHappen(
                    deviceName = device,
                    emailAddress = email,
                    smsAddress = sms
                )
            }
        }
    }
    val analyzer = remember {
        PoseAnalyzer(
            realTimeOpt = realTimeOpt,
            showInFrameLikelihood = showInFrameLikelihood,
            visualizeZ = visualizeZ,
            rescaleZForVisualization = rescaleZForVisualization,
            isHideInfo = isHideInfo,
            lensFacing = lensFacing,
            fallCheck = fall,
        )
    }
    val poseView = remember { PoseView(activity).apply { analyzer.view = this } }

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (camera, button) = createRefs()

        CameraScreen(
            modifier = Modifier.constrainAs(camera) {
                width = Dimension.fillToConstraints
                height = Dimension.fillToConstraints
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(button.top)
            },
            analyzer = analyzer,
            activity = activity,
            poseView = poseView
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.constrainAs(button) {
                width = Dimension.fillToConstraints
                height = Dimension.percent(.15f)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
            }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    modifier = Modifier.size(100.dp),
                    shape = MaterialTheme.shapes.medium,
                    onClick = {
//                        preference.setFacing(
//                            if (lensFacing == CameraSelector.LENS_FACING_BACK)
//                                CameraSelector.LENS_FACING_FRONT
//                            else
//                                CameraSelector.LENS_FACING_BACK
//                        )
//                        var change = true
//                        activity.cameraSelector =
//                            try {
//                                CameraSelector.Builder()
//                                    .requireLensFacing(lensFacing)
//                                    .build().also { cs ->
//                                        if (!activity.cameraProvider!!.hasCamera(cs)) {
//                                            shortToast(activity, "相机切换失败")
//                                            throw Exception("相机选择器创建异常")
//                                        } else {
//                                            // analysis 更新
//                                            activity.analysis = ImageAnalysis.Builder()
//                                                .setTargetResolution(size)
//                                                // 操作模式(阻塞/非阻塞)
//                                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                                                .build().also {
//                                                    it.setAnalyzer(
//                                                        ContextCompat.getMainExecutor(activity),
//                                                        analyzer
//                                                    )
//                                                }
//                                        }
//                                    }
//                            } catch (ex: Exception) {
//                                Log.e(
//                                    this::class.java.simpleName,
//                                    "创建相机选择器失败: ${ex.localizedMessage}",
//                                    ex
//                                )
//                                change = false
//                                activity.cameraSelector
//                            }
//                        if (change) activity.cameraBind()
                        val msg: String =
                            if (isFall) {
                                notice.lift()
                                "警戒状态已停止"
                            } else {
                                "警戒状态未启动"
                            }
                        shortToast(context = activity, msg = msg)
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_switch_camera_white_48dp_inset),
//                        contentDescription = "切换前后摄"
                        contentDescription = "关闭警告"
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    modifier = Modifier.size(100.dp),
                    shape = MaterialTheme.shapes.medium,
                    onClick = {
                        activity.startActivity(
                            Intent(
                                activity.applicationContext,
                                SettingsActivity::class.java
                            )
                        )
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings_white_24dp),
                        contentDescription = "跳转设置界面"
                    )
                }
            }
        }
    }
}

@Composable
fun CameraScreen(
    modifier: Modifier,
    analyzer: ImageAnalysis.Analyzer,
    activity: CameraActivity,
    poseView: PoseView
) {
    // 初始化
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(activity) }
    val cameraExecutor = remember { ContextCompat.getMainExecutor(activity) }
    val customView = remember { PreviewView(activity) }

    val preference = activity.preferenceViewModel
    val lensFacing = preference.facing.observeAsState().value!!
    val size = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
        preference.rearSize.observeAsState().value!!
    } else {
        preference.frontSize.observeAsState().value!!
    }

    // 预览显示
    Box(modifier = modifier.background(Color.DarkGray)) {
        AndroidView(modifier = Modifier.fillMaxSize(), factory = { customView }) { pre ->
            cameraProviderFuture.addListener({
                activity.cameraProvider = cameraProviderFuture.get()
                activity.preview = Preview.Builder()
                    .setTargetResolution(size)
                    .build().also {
                        it.setSurfaceProvider(pre.surfaceProvider)
                    }
                activity.analysis = ImageAnalysis.Builder()
                    .setTargetResolution(size)
                    // 操作模式(阻塞/非阻塞)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build().also { it.setAnalyzer(cameraExecutor, analyzer) }
                activity.cameraBind()
            }, cameraExecutor)
        }
        AndroidView(modifier = Modifier.fillMaxSize(), factory = { poseView })
    }
}

@Pr
@Composable
@RequiresApi(Build.VERSION_CODES.M)
private fun CameraShow() {
    CameraShow(activity = CameraActivity())
}