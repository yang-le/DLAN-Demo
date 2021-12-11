package me.yangle.dlnademo

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.fourthline.cling.android.AndroidUpnpService
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.registry.DefaultRegistryListener
import org.fourthline.cling.registry.Registry
import org.fourthline.cling.transport.Router

class DlnaViewModel(context: Context) : ViewModel() {
    val devices = mutableStateListOf<Device<*, *, *>>()
    val router: Router?
        get() = connection.router

    private var connection = UpnpServiceConnection(context)

    private class UpnpServiceConnection(
        private val context: Context
    ) : ServiceConnection {
        private lateinit var upnpService: AndroidUpnpService
        private var connected = false

        var listener: DefaultRegistryListener? = null
        val router: Router?
            get() = if (connected) upnpService.get().router else null

        fun connect() {
            if (!connected) {
                context.bindService(
                    Intent(context, DlnaUpnpService::class.java),
                    this,
                    Context.BIND_AUTO_CREATE
                )
            }
        }

        fun disconnect() {
            if (connected) {
                context.unbindService(this)
            }
        }

        fun search() {
            if (connected) {
                upnpService.registry.removeAllRemoteDevices()
                upnpService.controlPoint.search()
            }
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            upnpService = service as AndroidUpnpService
            upnpService.registry.devices.forEach { device ->
                listener?.deviceAdded(upnpService.registry, device)
            }
            upnpService.registry.addListener(listener)
            connected = true
            search()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            upnpService.registry.removeListener(listener)
            upnpService.registry.devices.forEach { device ->
                listener?.deviceRemoved(upnpService.registry, device)
            }
            connected = false
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getDeviceData(): Flow<Pair<Device<*, *, *>, Boolean>> =
        callbackFlow {
            connection.listener = object : DefaultRegistryListener() {
                override fun deviceAdded(registry: Registry, device: Device<*, *, *>) {
                    trySend(device to true)
                }

                override fun deviceRemoved(registry: Registry, device: Device<*, *, *>) {
                    trySend(device to false)
                }
            }

            connection.connect()

            awaitClose { connection.disconnect() }
        }

    fun search() = connection.search()
    fun disconnect() = connection.disconnect()

    init {
        viewModelScope.launch {
            getDeviceData().collect { (device, add) ->
                if (add) devices.add(device) else devices.remove(device)
            }
        }
    }
}