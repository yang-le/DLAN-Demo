package me.yangle.dlnademo

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import org.fourthline.cling.android.AndroidUpnpService
import org.fourthline.cling.registry.DefaultRegistryListener

class UpnpServiceConnection(
    private val context: Context
) : ServiceConnection {
    lateinit var upnpService: AndroidUpnpService
        private set

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
