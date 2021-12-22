package me.yangle.dlnademo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

class DlnaViewModel(private val connection: UpnpServiceConnection) : ViewModel() {
    val devices = mutableStateListOf<Device<*, *, *>>()
    var refreshing by mutableStateOf(true)
    val service: AndroidUpnpService
        get() = connection.upnpService

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getDeviceData(): Flow<Pair<Device<*, *, *>, Boolean>> = callbackFlow {
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

    fun destroy() {
        connection.disconnect()
    }

    init {
        viewModelScope.launch {
            getDeviceData().collect { (device, add) ->
                if (add) devices.add(device) else devices.remove(device)
                refreshing = devices.isEmpty()
            }
        }
    }
}