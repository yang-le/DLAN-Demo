package me.yangle.dlnademo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import me.yangle.dlnademo.ui.theme.DLNADemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DLNADemoTheme {
                DlnaList(DlnaViewModel(applicationContext))
            }
        }
    }
}
