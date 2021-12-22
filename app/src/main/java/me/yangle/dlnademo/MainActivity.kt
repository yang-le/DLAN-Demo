package me.yangle.dlnademo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import me.yangle.dlnademo.ui.DlnaList
import me.yangle.dlnademo.ui.theme.DLNADemoTheme
import me.yangle.dlnademo.upnp.AVTransportHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DLNADemoTheme {
                val viewModel =
                    remember { DlnaViewModel(UpnpServiceConnection(applicationContext)) }
                val scaffoldState = rememberScaffoldState()

                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        TopAppBar(
                            title = { Text(stringResource(R.string.app_name)) },
                            actions = {
                                IconButton(onClick = {
                                    startActivity(
                                        Intent(
                                            this@MainActivity,
                                            SettingsActivity::class.java
                                        )
                                    )
                                }) {
                                    Icon(Icons.Rounded.Settings, "settings")
                                }
                            }
                        )
                    }
                ) {
                    DlnaList(viewModel)
                }
                BackHandler {
                    AVTransportHelper.destroy()
                    viewModel.destroy()
                    finish()
                }
            }
        }
    }
}
