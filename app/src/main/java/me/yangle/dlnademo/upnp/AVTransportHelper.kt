package me.yangle.dlnademo.upnp

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkAddress
import android.net.Uri
import android.util.Log
import me.yangle.dlnademo.getCursor
import me.yangle.dlnademo.getPath
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.ResourceHandler
import org.eclipse.jetty.server.nio.SelectChannelConnector
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.support.avtransport.callback.GetDeviceCapabilities
import org.fourthline.cling.support.avtransport.callback.Play
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI
import org.fourthline.cling.support.avtransport.callback.SetPlayMode
import org.fourthline.cling.support.model.DeviceCapabilities
import org.fourthline.cling.support.model.PlayMode
import java.net.Inet4Address
import java.net.URLEncoder

object AVTransportHelper {
    private val TAG = this::class.simpleName

    private val server = Server()

    private fun startServer(path: String, port: Int = 8080) {
        if (server.isStarted) server.stop()
        val staticResourceHandler = ResourceHandler()
        staticResourceHandler.resourceBase = path
        server.handler = staticResourceHandler
        if (server.connectors?.isEmpty() != false) {
            server.connectors = arrayOf(SelectChannelConnector())
        }
        server.connectors[0].port = port
        server.start()
    }

    private fun getActiveIpv4(context: Context): LinkAddress? {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.getLinkProperties(connectivityManager.activeNetwork)?.linkAddresses?.find { it.address is Inet4Address }
    }

    fun play(service: Service<*, *>) = object : Play(service) {
        override fun failure(
            invocation: ActionInvocation<out Service<*, *>>?,
            operation: UpnpResponse?,
            defaultMsg: String?
        ) {
            defaultMsg?.let {
                Log.e(TAG, "Play: $it")
            }
        }
    }

    fun setAVTransportURI(service: Service<*, *>, url: String, metadata: String? = null) =
        object : SetAVTransportURI(service, url, metadata) {
            override fun failure(
                invocation: ActionInvocation<out Service<*, *>>?,
                operation: UpnpResponse?,
                defaultMsg: String?
            ) {
                defaultMsg?.let {
                    Log.e(TAG, "SetAVTransportURI: $it")
                }
            }
        }

    fun setAVTransportURI(
        service: Service<*, *>,
        context: Context,
        uri: Uri
    ): SetAVTransportURI {
        val path = getPath(context, uri)

        startServer(path?.dropLastWhile { it != '/' } ?: "")
        val url =
            "http://${getActiveIpv4(context)?.address?.hostAddress}:${server.connectors[0].port}/${
                URLEncoder.encode(
                    path?.split(
                        '/'
                    )?.last(), "utf-8"
                )
            }"
        Log.i(TAG, "start server at $url")

        val metadata = getCursor(context, uri).use {
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>${DIDLLiteHelper.getMetadata(it, url)}"
        }
        Log.i(TAG, "metadata: $metadata")

        return setAVTransportURI(service, url, metadata)
    }

    fun getDeviceCapabilities(service: Service<*, *>) = object : GetDeviceCapabilities(service) {
        override fun failure(
            invocation: ActionInvocation<out Service<*, *>>?,
            operation: UpnpResponse?,
            defaultMsg: String?
        ) {
            defaultMsg?.let {
                Log.e(TAG, "GetDeviceCapabilities: $it")
            }
        }

        override fun received(
            actionInvocation: ActionInvocation<out Service<*, *>>?,
            caps: DeviceCapabilities?
        ) {
            Log.i(TAG, "PlayMedia: ${caps?.playMediaString}")
            Log.i(TAG, "RecMedia: ${caps?.recMediaString}")
            Log.i(TAG, "RecQualityModes: ${caps?.recQualityModesString}")
        }
    }

    fun setPlayMode(service: Service<*, *>, playMode: PlayMode) =
        object : SetPlayMode(service, playMode) {
            override fun failure(
                invocation: ActionInvocation<out Service<*, *>>?,
                operation: UpnpResponse?,
                defaultMsg: String?
            ) {
                defaultMsg?.let {
                    Log.e(TAG, "SetPlayMode: $playMode $it")
                }
            }
        }

    fun destroy() {
        if (server.isStarted) server.stop()
    }
}