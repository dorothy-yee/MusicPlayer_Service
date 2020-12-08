package com.example.musicplayer_service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MusicService : Service() {

    companion object {
        const val Operate = "operate"
    }

    val musicPathList = mutableListOf<String>()
    val musicNameList = mutableListOf<String>()
    var current = 0
    var isPausing = false
    val MyChannel1 = "music channel"

    val mediaPlayer = MediaPlayer()

    inner class MusicBinder : Binder() {
        val current
            get() = this@MusicService.current
        val total
            get() = musicNameList.size
        val musicName
            get() = musicNameList.get(current)
        var process
            get() = mediaPlayer.currentPosition
            set(value) = mediaPlayer.seekTo(value)
        val duration
            get() = mediaPlayer.duration
    }

    override fun onCreate() {
        super.onCreate()
        getMusicList()
        val intent2 = Intent(MainActivity.Music_Broadcast)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder: Notification.Builder
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel1 = NotificationChannel(
                MyChannel1,
                "this is my channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel1)
            builder = Notification.Builder(this, MyChannel1)
        } else {
            builder = Notification.Builder(this)
        }

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = builder.setContentTitle("music notification")
            .setContentText("this is music service!!!")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        startForeground(1,notification)


        mediaPlayer.setOnPreparedListener {
            it.start()
            sendBroadcast(intent2)
            val notification = builder.setContentTitle("${current}/${musicNameList.size}")
                .setContentText(musicNameList.get(current))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
            notificationManager.notify(1,notification)
        }
        mediaPlayer.setOnCompletionListener {
            next()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val operate = intent?.getIntExtra(Operate, 0) ?: 0
        when (operate) {
            1 -> play()
            2 -> pause()
            3 -> stop()
            4 -> prev()
            5 -> next()
        }


        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        mediaPlayer.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder {
        return MusicBinder()
    }


    fun pause() {
        if (isPausing) {
            mediaPlayer.start()
            isPausing = true
        } else {
            mediaPlayer.pause()
            isPausing = false
        }
    }

    fun stop() {
        mediaPlayer.stop()
        stopSelf()
    }


    fun prev() {
        current--
        if (current < 0) {
            current = musicPathList.size - 1
        }
        play()
    }

    private fun next() {
        current++
        if (current >= musicPathList.size) {
            current = 0
        }
        play()
    }

    fun play() {
        if (musicPathList.size == 0) return
        val musicPath = musicPathList.get(current)
        mediaPlayer.reset()
        mediaPlayer.setDataSource(musicPath)
        mediaPlayer.prepareAsync()
    }


    fun getMusicList() {
        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            null,
            null
        )
        cursor?.apply {
            while (moveToNext()) {
                val musicPath = getString(getColumnIndex(MediaStore.Audio.Media.DATA))
                val musicName = getString(getColumnIndex(MediaStore.Audio.Media.TITLE))
                musicPathList.add(musicPath)
                musicNameList.add(musicName)
                Log.d("Music", "$musicName: $musicPath")
            }
        }
    }

}