package me.yangle.dlnademo.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import me.yangle.dlnademo.DlnaViewModel
import me.yangle.dlnademo.upnp.AVTransportHelper
import me.yangle.dlnademo.upnp.ConnectionManagerHelper
import me.yangle.dlnademo.upnp.Layer3ForwardingHelper
import me.yangle.dlnademo.upnp.LogSubscriptionCallback
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.model.types.ServiceType
import org.fourthline.cling.support.model.PlayMode


enum class ShowState {
    DEVICE, SERVICE, ACTION
}

@OptIn(
    ExperimentalAnimationApi::class, ExperimentalMaterialApi::class
)
@Composable
fun DlnaList(viewModel: DlnaViewModel) {
    var showState by remember { mutableStateOf(ShowState.DEVICE) }
    var currentDevice: Device<*, *, *>? by remember { mutableStateOf(null) }
    var currentService: Service<*, *>? by remember { mutableStateOf(null) }

    val context = LocalContext.current
    val getContentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            currentService?.let { service ->
                viewModel.service.controlPoint.execute(
                    AVTransportHelper.setAVTransportURI(
                        service,
                        context,
                        it
                    )
                )
            }
        }
    }
    val requestPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        getContentLauncher.launch("*/*")
    }

    when (showState) {
        ShowState.DEVICE -> {
            SwipeRefresh(
                state = rememberSwipeRefreshState(viewModel.refreshing),
                onRefresh = {
                    viewModel.refresh()
                }
            ) {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(viewModel.devices) {
                        val avTransport = it.findService(
                            ServiceType(
                                "schemas-upnp-org",
                                "AVTransport"
                            )
                        )
                        ListItem(
                            Modifier.clickable {
                                currentDevice = it
                                showState = ShowState.SERVICE
                            }, secondaryText = {
                                Text(it.type.displayString)
                            }, trailing = if (avTransport != null) {
                                {
                                    ProjectionButton(context, avTransport, viewModel)
                                }
                            } else null) {
                            Text(it.details.friendlyName)
                        }
                    }
                }
            }
        }
        ShowState.SERVICE -> {
            LazyColumn {
                currentDevice?.let { device ->
                    items(device.services) {
                        ListItem(Modifier.clickable {
                            currentService = it
                            showState = ShowState.ACTION
                            viewModel.service.controlPoint.execute(LogSubscriptionCallback(it))
                        }) {
                            Text((it as Service<*, *>).serviceType.toFriendlyString())
                        }
                    }
                }
            }
        }
        ShowState.ACTION -> {
            LazyColumn {
                currentService?.let { service ->
                    items(service.actions) {
                        ListItem(Modifier.clickable {
                            when (it.name) {
                                "SetAVTransportURI" -> {
                                    requestPermission.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                                }
                                "Play" -> {
                                    viewModel.service.controlPoint.execute(
                                        AVTransportHelper.play(
                                            service
                                        )
                                    )
                                }
                                "GetDeviceCapabilities" -> {
                                    viewModel.service.controlPoint.execute(
                                        AVTransportHelper.getDeviceCapabilities(
                                            service
                                        )
                                    )
                                }
                                "SetPlayMode" -> {
                                    viewModel.service.controlPoint.execute(
                                        AVTransportHelper.setPlayMode(
                                            service, PlayMode.DIRECT_1
                                        )
                                    )
                                }
                                "GetProtocolInfo" -> {
                                    viewModel.service.controlPoint.execute(
                                        ConnectionManagerHelper.getProtocolInfo(
                                            service
                                        )
                                    )
                                }
                                "GetDefaultConnectionService" -> {
                                    viewModel.service.controlPoint.execute(
                                        Layer3ForwardingHelper.getDefaultConnectionService(
                                            service
                                        )
                                    )
                                }
                            }
                        }) {
                            Text(it.name)
                        }
                    }
                }
            }
        }
    }
    when (showState) {
        ShowState.ACTION -> {
            BackHandler {
                showState = ShowState.SERVICE
            }
        }
        ShowState.SERVICE -> {
            BackHandler {
                showState = ShowState.DEVICE
            }
        }
        ShowState.DEVICE -> {
            // NOOP
        }
    }
}
