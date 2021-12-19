package me.yangle.dlnademo

import android.app.*
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.pedro.rtsp.utils.ConnectCheckerRtsp
import com.pedro.rtspserver.RtspServerDisplay

class MirrorService : Service(), ConnectCheckerRtsp {
    private lateinit var rtspServer: RtspServerDisplay

    override fun onCreate() {
        super.onCreate()
        rtspServer = RtspServerDisplay(this, true, this, 8081)
    }

    override fun onBind(intent: Intent?): IBinder {
        return Binder(rtspServer)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundNotification()
        rtspServer.prepareVideo(1280, 720, 2048 * 2048)
        if (intent?.getBooleanExtra("audio", false) == true)
            rtspServer.prepareInternalAudio()

        rtspServer.startStream()
        return super.onStartCommand(intent, flags, startId)
    }

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
        private val TAG = this::class.java.simpleName
        private const val CHANNEL_DEFAULT_IMPORTANCE = "channel_default"
        private const val ONGOING_NOTIFICATION_ID = 1
    }
}