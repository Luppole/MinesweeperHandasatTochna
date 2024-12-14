package com.example.minesweeperhansasattochna;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

public class MusicService extends Service {

    private MediaPlayer mediaPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music); // Place the audio file in res/raw
        mediaPlayer.setLooping(true); // Loop the music
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start(); // Start playing music
        }
        return START_STICKY; // Keep the service running
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop(); // Stop the music
            mediaPlayer.release(); // Release resources
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // No binding needed
    }
}
