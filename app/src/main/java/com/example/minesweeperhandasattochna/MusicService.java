package com.example.minesweeperhandasattochna;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;

import java.io.IOException;

public class MusicService extends Service {

    private MediaPlayer mediaPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String musicUriString = prefs.getString("CustomMusicUri", null);

        if (musicUriString != null) {
            Uri musicUri = Uri.parse(musicUriString);
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(this, musicUri);
                mediaPlayer.setLooping(true);
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Default background music
            mediaPlayer = MediaPlayer.create(this, R.raw.background_music); // Place the default music in res/raw
            mediaPlayer.setLooping(true);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
