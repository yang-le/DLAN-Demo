package me.yangle.dlnademo.ui

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import me.yangle.dlnademo.DlnaViewModel
import me.yangle.dlnademo.copyFile
import org.fourthline.cling.controlpoint.SubscriptionCallback
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.gena.CancelReason
import org.fourthline.cling.model.gena.GENASubscription
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.support.avtransport.callback.Play
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI
import java.io.File
import java.net.Inet4Address


enum class ShowState {
    DEVICE, SERVICE, ACTION
}

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalMaterialApi::class
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
                val file = File(context.cacheDir, "dlna_demo_temp")
                context.contentResolver.openInputStream(uri)?.let {
                    copyFile(it, file.outputStream())
                }
                viewModel.startServer(file.path)
                val connectivityManager =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val ipv4Address =
                    connectivityManager.getLinkProperties(connectivityManager.activeNetwork)?.linkAddresses?.find { it.address is Inet4Address }
                val url = "http://${ipv4Address?.address?.hostAddress}:8080"
                Log.i("dlnaDemo", "start server at $url")
                viewModel.service.controlPoint.execute(object :
                    SetAVTransportURI(currentService, url) {
                    override fun failure(
                        invocation: ActionInvocation<out Service<*, *>>?,
                        operation: UpnpResponse?,
                        defaultMsg: String?
                    ) {
                        defaultMsg?.let {
                            Log.w("dlnaDemo", it)
                        }
                    }
                })
            }
        }

    LazyColumn {
        when (showState) {
            ShowState.DEVICE -> {
                items(viewModel.devices) {
                    ListItem(Modifier.clickable {
                        currentDevice = it
                        showState = ShowState.SERVICE
                    }) {
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
                            viewModel.service.controlPoint.execute(MySubscriptionCallback(it))
                        }) {
                            Text(it.serviceType.toFriendlyString())
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
                                    viewModel.service.controlPoint.execute(object : Play(service) {
                                        override fun failure(
                                            invocation: ActionInvocation<out Service<*, *>>?,
                                            operation: UpnpResponse?,
                                            defaultMsg: String?
                                        ) {
                                            defaultMsg?.let {
                                                Log.w("dlnaDemo", it)
                                            }
                                        }
                                    })
                                }
                                else -> {
//                                    getContentLauncher.launch("*/*")
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

class MySubscriptionCallback(currentService: Service<*, *>) : SubscriptionCallback(currentService) {
    override fun failed(
        subscription: GENASubscription<out Service<*, *>>?,
        responseStatus: UpnpResponse?,
        exception: Exception?,
        defaultMsg: String?
    ) {
        Log.w("SubscriptionCallback", "$defaultMsg")
    }

    override fun established(subscription: GENASubscription<out Service<*, *>>?) {
        Log.i("SubscriptionCallback", "Established: ${subscription?.subscriptionId}")
    }

    override fun ended(
        subscription: GENASubscription<out Service<*, *>>?,
        reason: CancelReason?,
        responseStatus: UpnpResponse?
    ) {
        Log.i("SubscriptionCallback", createDefaultFailureMessage(responseStatus, null))
    }

    override fun eventReceived(subscription: GENASubscription<out Service<*, *>>?) {
        Log.i("SubscriptionCallback", "Event: ${subscription?.currentSequence?.value}")
        Log.i("SubscriptionCallback", "Status is: ${subscription?.currentValues?.get("Status")}")
        Log.i(
            "SubscriptionCallback",
            "LastChange is: ${subscription?.currentValues?.get("LastChange")}"
        )
    }

    override fun eventsMissed(
        subscription: GENASubscription<out Service<*, *>>?,
        numberOfMissedEvents: Int
    ) {
        Log.w("SubscriptionCallback", "Missed events: $numberOfMissedEvents")
    }
}
