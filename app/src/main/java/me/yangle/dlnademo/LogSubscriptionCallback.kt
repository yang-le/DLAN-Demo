package me.yangle.dlnademo

import android.util.Log
import org.fourthline.cling.controlpoint.SubscriptionCallback
import org.fourthline.cling.model.gena.CancelReason
import org.fourthline.cling.model.gena.GENASubscription
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.meta.Service

class LogSubscriptionCallback(currentService: Service<*, *>) : SubscriptionCallback(currentService) {
    private val TAG = this::class.simpleName

    override fun failed(
        subscription: GENASubscription<out Service<*, *>>?,
        responseStatus: UpnpResponse?,
        exception: Exception?,
        defaultMsg: String?
    ) {
        Log.w(TAG, "$defaultMsg")
    }

    override fun established(subscription: GENASubscription<out Service<*, *>>?) {
        Log.i(TAG, "Established: ${subscription?.subscriptionId}")
    }

    override fun ended(
        subscription: GENASubscription<out Service<*, *>>?, reason: CancelReason?, responseStatus: UpnpResponse?
    ) {
        Log.i(TAG, createDefaultFailureMessage(responseStatus, null))
    }

    override fun eventReceived(subscription: GENASubscription<out Service<*, *>>?) {
        Log.i(TAG, "Event: ${subscription?.currentSequence?.value}")
        Log.i(TAG, "Status is: ${subscription?.currentValues?.get("Status")}")
        Log.i(TAG, "LastChange is: ${subscription?.currentValues?.get("LastChange")}")
    }

    override fun eventsMissed(
        subscription: GENASubscription<out Service<*, *>>?, numberOfMissedEvents: Int
    ) {
        Log.w(TAG, "Missed events: $numberOfMissedEvents")
    }
}