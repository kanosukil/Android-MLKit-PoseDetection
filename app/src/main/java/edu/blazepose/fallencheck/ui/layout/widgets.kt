package edu.blazepose.fallencheck.ui.layout

import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import edu.blazepose.fallencheck.ui.theme.FallenCheckTheme

/**
 * 主页预设按钮
 *
 * @see <a href="https://github.com/sameermore412/CameraApp/blob/main/app/src/main/java/com/more/camerapp/widgets/Buttons.kt">参考 sameermore412 CameraApp Buttons.kt</a>
 */
@Composable
fun NavButton(buttonText: String, onClick: () -> Unit) {
    Button(
        shape = MaterialTheme.shapes.medium,
        onClick = onClick
    ) {
        Text(text = buttonText)
    }
}

@Preview
@Composable
private fun NavButton() {
    FallenCheckTheme {
        NavButton(buttonText = "Test", onClick = {})
    }
}