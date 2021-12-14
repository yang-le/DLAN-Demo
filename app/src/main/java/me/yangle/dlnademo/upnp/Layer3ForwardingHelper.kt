package me.yangle.dlnademo.upnp

import android.util.Log
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.meta.Service

object Layer3ForwardingHelper {
    private val TAG = this::class.simpleName

    fun getDefaultConnectionService(service: Service<*, *>) = object : GetDefaultConnectionService(service) {
        override fun received(invocation: ActionInvocation<out Service<*, *>>?, connectionService: String) {
            Log.i(TAG, "GetDefaultConnectionService: $connectionService")
        }

        override fun failure(
            invocation: ActionInvocation<out Service<*, *>>?, operation: UpnpResponse?, defaultMsg: String?
        ) {
            defaultMsg?.let {
                Log.w(TAG, "GetDefaultConnectionService: $it")
            }
        }
    }
}