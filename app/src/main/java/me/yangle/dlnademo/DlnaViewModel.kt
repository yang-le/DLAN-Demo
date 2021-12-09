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

class DlnaViewModel(context: Context) : ViewModel() {
    val devices = mutableStateListOf<DeviceDisplay>()
    private var connection = UpnpServiceConnection(context)

    class DeviceDisplay(private var device: Device<*, *, *>) {
        val detailsMessage: String
            get() {
                val sb = StringBuilder()
                if (device.isFullyHydrated) {
                    sb.append(device.displayString)
                    sb.append("\n\n")
                    for (service in device.services) {
                        sb.append(service.serviceType).append("\n")
                    }
                } else {
                    sb.append("Device details are being discovered, please wait.")
                }
                return sb.toString()
            }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || javaClass != other.javaClass) return false
            val that = other as DeviceDisplay
            return device == that.device
        }

        override fun hashCode() = device.hashCode()

        override fun toString(): String =
            if (device.details != null && device.details.friendlyName != null) device.details.friendlyName else device.displayString
    }

    private class UpnpServiceConnection(
        val context: Context
    ) : ServiceConnection {
        private lateinit var upnpService: AndroidUpnpService
        private var connected = false

        var listener: DefaultRegistryListener? = null

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

        private fun search() {
            if (connected) {
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
    private fun getDeviceData(): Flow<Pair<DeviceDisplay, Boolean>> =
        callbackFlow {
            connection.listener = object : DefaultRegistryListener() {
                override fun deviceAdded(registry: Registry, device: Device<*, *, *>) {
                    trySend(DeviceDisplay(device) to true)
                }

                override fun deviceRemoved(registry: Registry, device: Device<*, *, *>) {
                    trySend(DeviceDisplay(device) to false)
                }
            }

            connection.connect()

            awaitClose { connection.disconnect() }
        }

    override fun onCleared() {
        connection.disconnect()
        super.onCleared()
    }

    init {
        viewModelScope.launch {
            getDeviceData().collect { (device, add) ->
                if (add) devices.add(device) else devices.remove(device)
            }
        }
    }
}