package edu.blazepose.fallencheck.ui.layout

import android.view.Window
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import edu.blazepose.fallencheck.ui.theme.FallenCheckTheme

/**
 * 进入主页面整体布局
 *
 * @see <a href="https://github.com/sameermore412/CameraApp/blob/main/app/src/main/java/com/more/camerapp/screens/AppScreen.kt">参考 sameermore412 CameraApp AppScreen.kt</a>
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenContainer(
    showAppBar: Boolean = true,
    window: Window? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    FallenCheckTheme(window) {
        Scaffold(
            topBar = {
                if (showAppBar) {
                    TopAppBar(
                        title = { Text(text = "跌倒姿态监测APP") },
                    )
                }
            },
            content = content
        )
    }
}

@Preview
@Composable
private fun ScreenContainer() {
    FallenCheckTheme {
        ScreenContainer(content = {})
    }
}