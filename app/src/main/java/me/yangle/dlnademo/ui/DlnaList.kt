package me.yangle.dlnademo.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import me.yangle.dlnademo.DlnaViewModel
import me.yangle.dlnademo.R
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.model.meta.Service

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalMaterialApi::class
)
@Composable
fun DlnaList(viewModel: DlnaViewModel) {
    var showService by remember { mutableStateOf(false) }
    var showAction by remember { mutableStateOf(false) }
    var currentDevice: Device<*, *, *>? by remember { mutableStateOf(null) }
    var currentService: Service<*, *>? by remember { mutableStateOf(null) }

    LazyColumn {
        if (showService) {
            currentDevice?.let {
                items(it.services) {
                    ListItem(Modifier.clickable {
                        currentService = it
                        showAction = true
                    }) {
                        Text(it.serviceType.toFriendlyString())
                    }
                }
            }
        } else {
            items(viewModel.devices) {
                ListItem(Modifier.clickable {
                    currentDevice = it
                    showService = true
                }) {
                    Text(it.details.friendlyName)
                }
            }
        }
    }

    AnimatedVisibility(showAction) {
        AlertDialog(
            title = { Text(stringResource(R.string.deviceDetails)) },
            text = {
                LazyColumn {
                    currentService?.let {
                        items(it.actions) {
                            Text(it.name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showAction = false }
                ) {
                    Text(stringResource(R.string.OK))
                }
            },
            onDismissRequest = { showAction = false },
        )
    }

    if (showService) {
        BackHandler {
            showService = false
        }
    }
}
