package com.example.musicstreamer;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;


import com.danikula.videocache.HttpProxyCacheServer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;

import java.util.List;

import io.paperdb.Paper;

public class App extends Application {
    public static final String CHANNEL_ID = "ServiceChannel";
    public static final int NOTIFICATION_ID = 1234;
    public static SimpleExoPlayer player;
    public static PlayerControlView playerControlView;
    public static PlayerNotificationManager playerNotificationManager;
    public static Track current_track, prev_track;
    public static HttpProxyCacheServer proxyServer;





    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        Paper.init(this);

        playerControlView = null;
        current_track =  Paper.book().read("current_track");
    }
    private void createNotificationChannel()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID,
                    "Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
                    );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }











}
