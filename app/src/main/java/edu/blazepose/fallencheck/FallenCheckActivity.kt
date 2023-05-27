package edu.blazepose.fallencheck

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import edu.blazepose.fallencheck.ui.layout.HomeShow
import edu.blazepose.fallencheck.ui.layout.ScreenContainer
import edu.blazepose.fallencheck.util.FileUtils

/**
 * Main Activity
 */
class FallenCheckActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!allRuntimePermissionsGranted()) {
            getRuntimePermissions()
        }

        setContent {
            ScreenContainer(window = this.window) {
                HomeShow(
                    onCameraClick = {
                        startActivity(Intent(applicationContext, CameraActivity::class.java))
                    },
                    onSettingsClick = {
                        startActivity(Intent(applicationContext, SettingsActivity::class.java))
                    },
                    onLogsClick = {
                        FileUtils.openLog(applicationContext)
                    }
                    /*,
                    test1 = {
                        alertViewModel.play()
                    },
                    test2 = {
                        alertViewModel.stop()
                    }*/
                )
            }
        }
    }

    /* 布局 */
//    @Composable
//    private fun FallenCheckShow() {
//        val lifecycleOwner = LocalLifecycleOwner.current
//        val context = LocalContext.current
//        val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
//
//        Scaffold { padding ->
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(padding)
//            ) {
//                AndroidView(
//                    modifier = Modifier.fillMaxSize(),
//                    factory = { ctx ->
//                        val previewView = PreviewView(ctx)
//                        val executor = ContextCompat.getMainExecutor(ctx)
//
//                        cameraProviderFuture.addListener({
//                            val cameraProvider = cameraProviderFuture.get()
//                            val preview = Preview.Builder().build().also {
//                                it.setSurfaceProvider(previewView.surfaceProvider)
//                            }
//
//                            val cameraSelector = CameraSelector.Builder()
//                                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
//                                .build()
//
//                            val imageAnalysis = ImageAnalysis.Builder()
//                                .setTargetResolution(Size(previewView.width, previewView.height))
//                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                                .setImageQueueDepth(10)
//                                .build()
//                                .apply {
//                                    setAnalyzer(executor, PoseAnalyzer())
//                                }
//
//                            cameraProvider.unbindAll()
//                            cameraProvider.bindToLifecycle(
//                                lifecycleOwner,
//                                cameraSelector,
//                                preview,
//                                imageAnalysis
//                            )
//                        }, executor)
//                        previewView
//                    }
//                )
//            }
//        }
//    }

    /* 鉴权 */

    /**
     * 检查需要的权限
     */
    private fun allRuntimePermissionsGranted(): Boolean {
        for (permission in REQUIRED_RUNTIME_PERMISSIONS) {
            permission.let {
                if (!isPermissionGranted(this, it)) {
                    return false
                }
            }
        }
        return true
    }

    /**
     * 获取权限
     */
    private fun getRuntimePermissions() {
        val permissionsToRequest = ArrayList<String>()
        for (permission in REQUIRED_RUNTIME_PERMISSIONS) {
            permission.let {
                if (!isPermissionGranted(this, it)) {
                    Log.i(TAG, "尝试获取权限: $it")
                    permissionsToRequest.add(permission)
                }
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUESTS
            )
        }
    }

    /**
     * 检测指定权限
     */
    private fun isPermissionGranted(context: Context, permission: String): Boolean =
        if (ContextCompat.checkSelfPermission(context, permission)
            == PackageManager.PERMISSION_GRANTED
        ) {
            Log.i(TAG, "已获取权限: $permission")
            true
        } else {
            Log.i(TAG, "未获取权限: $permission")
            false
        }


    companion object {
        private const val TAG = "Fallen Check Activity"
        private const val PERMISSION_REQUESTS = 1

        private val REQUIRED_RUNTIME_PERMISSIONS =
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WAKE_LOCK,
                Manifest.permission.SEND_SMS,
                Manifest.permission.INTERNET,
                Manifest.permission.MODIFY_AUDIO_SETTINGS
            )
    }
}

