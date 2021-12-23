package me.yangle.dlnademo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.ui.res.stringResource
import me.yangle.dlnademo.ui.About
import me.yangle.dlnademo.ui.theme.DLNADemoTheme

class AboutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DLNADemoTheme {
                Scaffold(
                    topBar = {
                        TopAppBar({ Text(stringResource(R.string.title_activity_about)) })
                    }) {
                    About()
                }
            }
        }
    }
}
