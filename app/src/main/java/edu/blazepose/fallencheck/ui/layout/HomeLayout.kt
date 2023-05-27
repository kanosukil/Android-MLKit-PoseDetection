package edu.blazepose.fallencheck.ui.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.blazepose.fallencheck.ui.theme.FallenCheckTheme

/**
 * 进入主页面
 *
 * @see <a href="https://github.com/sameermore412/CameraApp/blob/main/app/src/main/java/com/more/camerapp/screens/HomeScreen.kt">参考 sameermore412 CameraApp HomeScreen.kt</a>
 */
@Composable
fun HomeShow(
    onCameraClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogsClick: () -> Unit,
//    test1: () -> Unit = {},
//    test2: () -> Unit = {},
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            NavButton(onClick = onCameraClick, buttonText = "打开相机")
            Spacer(modifier = Modifier.height(16.dp))
            NavButton(onClick = onSettingsClick, buttonText = "打开设置")
            Spacer(modifier = Modifier.height(16.dp))
            NavButton(onClick = onLogsClick, buttonText = "查看日志")
//            Spacer(modifier = Modifier.height(16.dp))
//            NavButton(onClick = test1, buttonText = "功能测试1")
//            Spacer(modifier = Modifier.height(16.dp))
//            NavButton(onClick = test2, buttonText = "功能测试2")
        }
    }
}

@Preview
@Composable
private fun HomeShow() {
    FallenCheckTheme {
        HomeShow(onCameraClick = {}, onSettingsClick = {}, onLogsClick = {})
    }
}