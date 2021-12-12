package me.yangle.dlnademo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import me.yangle.dlnademo.DlnaViewModel
import me.yangle.dlnademo.R
import org.fourthline.cling.transport.RouterException
import java.util.logging.Level
import java.util.logging.Logger


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
        val disablingDebugLogging = stringResource(R.string.disablingDebugLogging)
        val enablingDebugLogging = stringResource(R.string.enablingDebugLogging)
        val disablingRouter = stringResource(R.string.disablingRouter)
        val enablingRouter = stringResource(R.string.enablingRouter)
        val errorSwitchingRouter = stringResource(R.string.errorSwitchingRouter)
        val logger = Logger.getLogger("org.fourthline.cling")
        var debugState by remember { mutableStateOf(logger.level != Level.INFO) }
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
                        scope.launch { snackbarHostState.showSnackbar(
                            errorSwitchingRouter + ex.toString(),
                            duration = SnackbarDuration.Long
                        ) }
                    }
                    viewModel.refresh()
            }
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.switchRouter))
                Switch(routerState, null)
            }
        }
        DropdownMenuItem(onClick = {
            if (logger.level != Level.INFO) {
                logger.level = Level.INFO
                debugState = false
                scope.launch { snackbarHostState.showSnackbar(disablingDebugLogging) }
            } else {
                logger.level = Level.FINEST
                debugState = true
                scope.launch { snackbarHostState.showSnackbar(enablingDebugLogging) }
            }
        }) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.toggleDebugLogging))
                Switch(debugState, null)
            }
        }
    }
}
