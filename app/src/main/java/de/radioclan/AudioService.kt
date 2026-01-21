package de.radioclan

import android.app.*
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player

class AudioService : Service() {

    private var player: ExoPlayer? = null
    private val channelId = "RadioClanChannel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        player = ExoPlayer.Builder(this).build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val streamUrl = intent?.getStringExtra("STREAM_URL")

        if (streamUrl != null) {
            playStream(streamUrl)
        }

        // Benachrichtigung anzeigen, damit Android den Dienst nicht killt
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Radio Clan")
            .setContentText("Stream läuft...")
            .setSmallIcon(R.mipmap.ic_launcher) // Nutzt dein Icon
            .build()

        startForeground(1, notification)

        return START_STICKY
    }

    private fun playStream(url: String) {
        player?.let {
            val mediaItem = MediaItem.fromUri(Uri.parse(url))
            it.setMediaItem(mediaItem)
            it.prepare()
            it.playWhenReady = true
        }
    }

    override fun onDestroy() {
        player?.release()
        player = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            channelId, "Radio Clan Stream",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }
}