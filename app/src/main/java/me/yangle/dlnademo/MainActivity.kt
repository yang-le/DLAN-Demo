package me.yangle.dlnademo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import me.yangle.dlnademo.ui.DlnaDropdownButton
import me.yangle.dlnademo.ui.DlnaList
import me.yangle.dlnademo.ui.theme.DLNADemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DLNADemoTheme {
                val viewModel = remember { DlnaViewModel(applicationContext) }
                val scaffoldState = rememberScaffoldState()
                val scope = rememberCoroutineScope()
                val searchingLAN = stringResource(id = R.string.searchingLAN)

                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        TopAppBar(
                            title = { Text(stringResource(R.string.app_name)) },
                            actions = {
                                IconButton(onClick = {
                                    viewModel.refresh()
                                    scope.launch {
                                        scaffoldState.snackbarHostState.showSnackbar(searchingLAN)
                                    }
                                }) {
                                    Icon(Icons.Rounded.Refresh, stringResource(R.string.searchLAN))
                                }
                                DlnaDropdownButton(viewModel, scaffoldState.snackbarHostState)
                            }
                        )
                    }
                ) {
                    DlnaList(viewModel)
                }
                BackHandler {
                    viewModel.destroy()
                    finish()
                }
            }
        }
    }
}
