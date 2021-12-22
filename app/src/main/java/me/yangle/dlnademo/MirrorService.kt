package me.yangle.dlnademo

import android.app.*
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.preference.PreferenceManager
import com.pedro.rtsp.utils.ConnectCheckerRtsp
import com.pedro.rtspserver.RtspServerDisplay

class MirrorService : Service(), ConnectCheckerRtsp {
    private lateinit var rtspServer: RtspServerDisplay

    private fun startForegroundNotification() {
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, FLAG_IMMUTABLE)
            }

        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_DEFAULT_IMPORTANCE,
                "Mirror Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )

        val notification: Notification = Notification.Builder(this, CHANNEL_DEFAULT_IMPORTANCE)
            .setContentTitle("MirrorService")
            .setContentText("Tap to stop")
            .setContentIntent(pendingIntent)
            .build()

        startForeground(ONGOING_NOTIFICATION_ID, notification)
    }

    override fun onCreate() {
        super.onCreate()
        val preference = PreferenceManager.getDefaultSharedPreferences(this)
        val opengl = preference.getBoolean("opengl", false)
        val port = preference.getString("port", null) ?: "8081"
        rtspServer = RtspServerDisplay(this, opengl, this, port.toInt())
    }

    override fun onBind(intent: Intent?): IBinder {
        return Binder(rtspServer)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundNotification()

        val preference = PreferenceManager.getDefaultSharedPreferences(this)
        val size = (preference.getString("resolution", null) ?: "1280x720").split('x')
        val bitrate = preference.getString("bitrate", null) ?: "4"
        Log.i(TAG, "start ${size[0].toInt()} x ${size[1].toInt()} @ ${bitrate.toInt()} Mbps")

        rtspServer.setIntentResult(
            ComponentActivity.RESULT_OK,
            intent?.getParcelableExtra("data")
        )
        rtspServer.prepareVideo(size[0].toInt(), size[1].toInt(), bitrate.toInt() * 1024 * 1024)
        if (intent?.getBooleanExtra("audio", false) == true)
            rtspServer.prepareInternalAudio()

        rtspServer.startStream()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        stopSelf()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        rtspServer.stopStream()
        super.onDestroy()
    }

    override fun onAuthErrorRtsp() {
        Log.e(TAG, "onAuthErrorRtsp")
        rtspServer.stopStream()
    }

    override fun onAuthSuccessRtsp() {
        Log.i(TAG, "onAuthSuccessRtsp")
    }

    override fun onConnectionFailedRtsp(reason: String) {
        Log.e(TAG, "onConnectionFailedRtsp: $reason")
        rtspServer.stopStream()
    }

    override fun onConnectionStartedRtsp(rtspUrl: String) {
        Log.i(TAG, "onConnectionStartedRtsp: $rtspUrl")
    }

    override fun onConnectionSuccessRtsp() {
        Log.i(TAG, "onConnectionSuccessRtsp")
    }

    override fun onDisconnectRtsp() {
        Log.i(TAG, "onDisconnectRtsp")
    }

    override fun onNewBitrateRtsp(bitrate: Long) {
        Log.i(TAG, "onNewBitrateRtsp: $bitrate")
    }

    class Binder(val server: RtspServerDisplay) : android.os.Binder()

    companion object {
        private val TAG = MirrorService::class.java.simpleName
        private const val CHANNEL_DEFAULT_IMPORTANCE = "channel_default"
        private const val ONGOING_NOTIFICATION_ID = 1
    }
}