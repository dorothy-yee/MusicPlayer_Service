package com.example.musicplayer_service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity(), ServiceConnection {

    companion object {
        const val Music_Broadcast = "com.example.musicplayerdemo.receiver"
    }
    var binder: MusicService.MusicBinder? = null
    val receiver = MusicReceiver()

    override fun onServiceConnected(p0: ComponentName?, _binder: IBinder?) {
        if(_binder == null) return
        binder = _binder as MusicService.MusicBinder
        binder?.apply {
            seekBar.max = duration
            textView_count.text = "${current}/${total}"
            textView_name.text = musicName

        }
        thread {
            while (true) {
                Thread.sleep(1000)
                runOnUiThread {
                    seekBar.progress = _binder.process ?: 0
                }
            }
        }

    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        binder = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intentFilter = IntentFilter()
        intentFilter.addAction(Music_Broadcast)
        registerReceiver(receiver,intentFilter)


        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1)
        } else {
        }

        val intent = Intent(this, MusicService::class.java)
        startService(intent)
        bindService(intent,this, Context.BIND_AUTO_CREATE)


        seekBar.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, position: Int, fromUser: Boolean) {
                if(fromUser){
                    binder?.process = position
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }

        })

    }


    fun onPlay(v: View){
        val intent = Intent(this, MusicService::class.java)
        intent.putExtra(MusicService.Operate,1)
        startService(intent)

    }

    fun onPause(v: View){
        val intent = Intent(this, MusicService::class.java)
        intent.putExtra(MusicService.Operate,2)
        startService(intent)
    }

    fun onStop(v: View){
        val intent = Intent(this, MusicService::class.java)
        intent.putExtra(MusicService.Operate,3)
        startService(intent) }

    fun onPrev(v: View){
        val intent = Intent(this, MusicService::class.java)
        intent.putExtra(MusicService.Operate,4)
        startService(intent)
    }

    fun onNext(v: View){
        val intent = Intent(this, MusicService::class.java)
        intent.putExtra(MusicService.Operate,5)
        startService(intent)}

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 1){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
            }
        }
    }

    inner class MusicReceiver: BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            binder?.apply {
                seekBar.max = duration
                textView_count.text = "${current}/${total}"
                textView_name.text = musicName

            }
        }

    }

}