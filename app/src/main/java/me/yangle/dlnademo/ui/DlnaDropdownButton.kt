package me.yangle.dlnademo.ui

import android.content.Intent
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import me.yangle.dlnademo.DlnaViewModel
import me.yangle.dlnademo.R
import me.yangle.dlnademo.SettingsActivity
import org.fourthline.cling.transport.RouterException


@Composable
fun DlnaDropdownButton(
    viewModel: DlnaViewModel,
    snackbarHostState: SnackbarHostState
) {
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }

    IconButton(onClick = { expanded = true }) {
        Icon(Icons.Rounded.MoreVert, "more")
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        val context = LocalContext.current
        val disablingRouter = stringResource(R.string.disablingRouter)
        val enablingRouter = stringResource(R.string.enablingRouter)
        val errorSwitchingRouter = stringResource(R.string.errorSwitchingRouter)
        val router = viewModel.service.get().router
        var routerState by remember { mutableStateOf(router.isEnabled) }

        DropdownMenuItem(
            onClick = {
                try {
                    if (router.isEnabled) {
                        router.disable()
                        routerState = false
                        scope.launch { snackbarHostState.showSnackbar(disablingRouter) }
                    } else {
                        router.enable()
                        routerState = true
                        scope.launch { snackbarHostState.showSnackbar(enablingRouter) }
                    }
                } catch (ex: RouterException) {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            errorSwitchingRouter + ex.toString(),
                            duration = SnackbarDuration.Long
                        )
                    }
                }
                viewModel.refresh()
            }
        ) {
            Text(stringResource(R.string.switchRouter))
            Switch(routerState, null)
        }
        DropdownMenuItem(onClick = {
            expanded = false
            context.startActivity(Intent(context, SettingsActivity::class.java))
        }) {
            Text("Settings")
        }
    }
}
