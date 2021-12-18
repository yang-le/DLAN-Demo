package me.yangle.dlnademo.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.pedro.rtspserver.RtspServerDisplay
import me.yangle.dlnademo.DlnaViewModel
import me.yangle.dlnademo.MirrorService
import me.yangle.dlnademo.getCursor
import me.yangle.dlnademo.upnp.*
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
    val getContentLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                currentService?.let { service ->
                    val metadata = getCursor(context, uri).use {
                        DIDLLiteHelper.metaData(it)
                    }
                    viewModel.service.controlPoint.execute(
                        AVTransportHelper.setAVTransportURI(
                            service,
                            context,
                            it,
                            metadata
                        )
                    )
                }
            }
        }

    LazyColumn {
        when (showState) {
            ShowState.DEVICE -> {
                items(viewModel.devices) {
                    val avTransport = it.findService(
                        ServiceType(
                            "schemas-upnp-org",
                            "AVTransport"
                        )
                    )
                    ListItem(Modifier.clickable {
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
            ShowState.SERVICE -> {
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
            ShowState.ACTION -> {
                currentService?.let { service ->
                    items(service.actions) {
                        ListItem(Modifier.clickable {
                            when (it.name) {
                                "SetAVTransportURI" -> {
                                    getContentLauncher.launch("*/*")
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
                                else -> {
                                    getContentLauncher.launch("*/*")
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

@Composable
private fun ProjectionButton(
    context: Context,
    avTransport: Service<*, *>,
    viewModel: DlnaViewModel
) {
    lateinit var rtspServer: RtspServerDisplay
    val requestPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) rtspServer.prepareInternalAudio()
    }
    val getProjection = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == ComponentActivity.RESULT_OK) {
            val service = Intent(context, MirrorService::class.java)
            service.putExtra("code", it.resultCode)
            service.putExtra("data", it.data)
            ContextCompat.startForegroundService(context, service)
            requestPermission.launch(android.Manifest.permission.RECORD_AUDIO)
            viewModel.service.controlPoint.execute(
                AVTransportHelper.play(avTransport)
            )
        }
    }

    IconButton(onClick = {
        context.bindService(
            Intent(context, MirrorService::class.java),
            object : ServiceConnection {
                override fun onServiceConnected(
                    name: ComponentName?,
                    service: IBinder?
                ) {
                    rtspServer = (service as MirrorService.Binder).server
                    viewModel.service.controlPoint.execute(
                        AVTransportHelper.setAVTransportURI(
                            avTransport,
                            rtspServer.getEndPointConnection()
                        )
                    )
                    getProjection.launch(rtspServer.sendIntent())
                }

                override fun onServiceDisconnected(name: ComponentName?) {}
            },
            Context.BIND_AUTO_CREATE
        )
    }) {
        Icon(Icons.Rounded.Cast, "projection")
    }
}
