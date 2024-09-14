package com.example.keyadministrator;

import android.content.Context;
import android.media.MediaPlayer;

public class MediaMp3Player {

    private static MediaPlayer mediaPlayer;

    public static void playMp3(Context context, int resId) {
        if (mediaPlayer != null) {
            mediaPlayer.release(); // 释放之前的 MediaPlayer 实例
            mediaPlayer = null;
        }

        mediaPlayer = MediaPlayer.create(context, resId);
        mediaPlayer.setOnCompletionListener(mp -> {
            mp.release(); // 播放完成后释放 MediaPlayer
            mediaPlayer = null;
        });
        mediaPlayer.start();
    }
}
