package me.yangle.dlnademo.upnp

import android.util.Log
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.support.connectionmanager.callback.GetProtocolInfo
import org.fourthline.cling.support.model.ProtocolInfos

object ConnectionManagerHelper {
    private val TAG = this::class.simpleName

    fun getProtocolInfo(service: Service<*, *>) = object : GetProtocolInfo(service) {
        override fun failure(
            invocation: ActionInvocation<out Service<*, *>>?, operation: UpnpResponse?, defaultMsg: String?
        ) {
            defaultMsg?.let {
                Log.w(TAG, "GetProtocolInfo: $it")
            }
        }

        override fun received(
            actionInvocation: ActionInvocation<out Service<*, *>>?,
            sinkProtocolInfos: ProtocolInfos?,
            sourceProtocolInfos: ProtocolInfos?
        ) {
            Log.i(TAG, "sinkProtocolInfos:")
            sinkProtocolInfos?.forEach {
                Log.i(TAG, it.toString())
            }
            Log.i(TAG, "sourceProtocolInfos:")
            sourceProtocolInfos?.forEach {
                Log.i(TAG, it.toString())
            }
        }
    }
}