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
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.ResourceHandler
import org.fourthline.cling.android.AndroidUpnpService
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.registry.DefaultRegistryListener
import org.fourthline.cling.registry.Registry

class DlnaViewModel(context: Context) : ViewModel() {
    val devices = mutableStateListOf<Device<*, *, *>>()
    val service: AndroidUpnpService
        get() = connection.upnpService

    private var connection = UpnpServiceConnection(context)
    private var server = Server(8080)

    private class UpnpServiceConnection(
        private val context: Context
    ) : ServiceConnection {
        lateinit var upnpService: AndroidUpnpService

        private var connected = false
        private lateinit var listener: DefaultRegistryListener

        fun connect(listener: DefaultRegistryListener) {
            if (!connected) {
                this.listener = listener
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

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            upnpService = service as AndroidUpnpService
            upnpService.registry.devices.forEach { device ->
                listener.deviceAdded(upnpService.registry, device)
            }
            upnpService.registry.addListener(listener)
            connected = true

            upnpService.controlPoint.search()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            upnpService.registry.removeListener(listener)
            upnpService.registry.devices.forEach { device ->
                listener.deviceRemoved(upnpService.registry, device)
            }
            connected = false
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getDeviceData(): Flow<Pair<Device<*, *, *>, Boolean>> =
        callbackFlow {
            connection.connect(object : DefaultRegistryListener() {
                override fun deviceAdded(registry: Registry, device: Device<*, *, *>) {
                    trySend(device to true)
                }

                override fun deviceRemoved(registry: Registry, device: Device<*, *, *>) {
                    trySend(device to false)
                }
            })
            awaitClose { connection.disconnect() }
        }

    fun refresh() {
        service.registry.removeAllRemoteDevices()
        service.controlPoint.search()
    }

    fun startServer(path: String) {
        if (server.isStarted) server.stop()
        val staticResourceHandler = ResourceHandler()
        staticResourceHandler.resourceBase = path
        server.handler = staticResourceHandler
        server.start()
    }

    fun destroy() {
        if (server.isStarted) server.stop()
        connection.disconnect()
    }

    init {
        viewModelScope.launch {
            getDeviceData().collect { (device, add) ->
                if (add) devices.add(device) else devices.remove(device)
            }
        }
    }
}