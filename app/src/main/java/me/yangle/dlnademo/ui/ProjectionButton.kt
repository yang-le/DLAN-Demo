package me.yangle.dlnademo.ui

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import me.yangle.dlnademo.DlnaViewModel
import me.yangle.dlnademo.MirrorService
import me.yangle.dlnademo.upnp.AVTransportHelper
import org.fourthline.cling.model.meta.Service


@Composable
fun ProjectionButton(
    context: Context,
    avTransport: Service<*, *>,
    viewModel: DlnaViewModel
) {
    val service = Intent(context, MirrorService::class.java)
    val connection = remember {
        object : ServiceConnection {
            var connected = false
            override fun onServiceConnected(
                name: ComponentName?,
                service: IBinder?
            ) {
                connected = true

                val rtspServer = (service as MirrorService.Binder).server
                viewModel.service.controlPoint.execute(
                    AVTransportHelper.setAVTransportURI(
                        avTransport,
                        rtspServer.getEndPointConnection()
                    )
                )
                viewModel.service.controlPoint.execute(
                    AVTransportHelper.play(avTransport)
                )
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                connected = false
            }
        }
    }

    val requestPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        service.putExtra("audio", it)
        ContextCompat.startForegroundService(context, service)

        context.bindService(
            Intent(context, MirrorService::class.java),
            connection,
            Context.BIND_AUTO_CREATE
        )
    }

    val getProjection = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == ComponentActivity.RESULT_OK) {
            service.putExtra("data", it.data)
            requestPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val projectionManager =
        context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

    IconButton(onClick = {
        if (connection.connected) {
            context.unbindService(connection)
            connection.connected = false
        } else {
            getProjection.launch(projectionManager.createScreenCaptureIntent())
        }
    }) {
        Icon(Icons.Rounded.Cast, "projection")
    }
}
