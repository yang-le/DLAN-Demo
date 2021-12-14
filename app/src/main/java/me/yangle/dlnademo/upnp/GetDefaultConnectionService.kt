package me.yangle.dlnademo.upnp

import org.fourthline.cling.controlpoint.ActionCallback
import org.fourthline.cling.model.action.ActionException
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.model.types.ErrorCode

abstract class GetDefaultConnectionService(service: Service<*, *>) :
    ActionCallback(ActionInvocation(service.getAction("GetDefaultConnectionService"))) {

    override fun success(invocation: ActionInvocation<out Service<*, *>>?) {
        try {
            val connectionService = invocation?.getOutput("NewDefaultConnectionService")
            received(invocation, connectionService.toString())
        } catch (ex: Exception) {
            invocation?.failure = ActionException(ErrorCode.ACTION_FAILED, "Can't parse ProtocolInfo response: $ex", ex)
            failure(invocation, null)
        }
    }

    abstract fun received(invocation: ActionInvocation<out Service<*, *>>?, connectionService: String)
}