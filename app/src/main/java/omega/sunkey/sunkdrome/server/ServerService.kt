package omega.sunkey.sunkdrome.server

import android.R
import android.app.Service
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import io.javalin.Javalin

class ServerService : Service() {
    private var server: Javalin? = null
    private val channelId = "sunkdrome_server_channel"
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (server == null) start()
        val notification = notify()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(1, notification)
        }

        return START_STICKY
    }
    private fun start() {
        server = Javalin.create() { config ->
            config.showJavalinBanner = false
        }.apply {
            setBeforeHandlers()
            setPaths()
        }.start("0.0.0.0",4040)

        Log.i("ServerService", "started")
    }
    private fun notify(): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Sunkdrome")
            .setContentText("Server running: 4040")
            .setSmallIcon(R.drawable.ic_media_play)
            .setOngoing(true)
            .build()
    }
    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Sunkdrome Status",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        server?.stop()
        server = null
        Log.i("ServerService", "stopped")
    }

    override fun onBind(p0: Intent?): IBinder? = null
}