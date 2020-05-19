package com.example.musicstreamer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.widget.Switch;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.danikula.videocache.HttpProxyCacheServer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.DownloadNotificationUtil;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.util.Util;


import io.paperdb.Paper;

public class PlayerService extends Service {


    Track track;
    private final IBinder mBinder = new LocalBinder();
    Context context;
    private SimpleExoPlayer player;
    private PlayerNotificationManager playerNotificationManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }




    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Paper.init(context);

        player = App.player;

        playerNotificationManager = App.playerNotificationManager;

        if(App.prev_track == App.current_track)
        {

        }
        else
        {
            if(playerNotificationManager!=null)
            {

                playerNotificationManager.setPlayer(null);
                if(player!=null)
                {
                    player.release();
                    player = null;
                }


            }


            if(App.current_track != null)
            {
                startPlayer();
            }


            App.prev_track = App.current_track;

        }

        return Service.START_STICKY;


    }

    @Override
    public void onDestroy() {
        playerNotificationManager.setPlayer(null);
        player.release();
        player = null;
        super.onDestroy();
    }




    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        player = new SimpleExoPlayer.Builder(this).build();
    }


    public void startPlayer()
    {


        track = App.current_track;



        String proxyURL = App.proxyServer.getProxyUrl(track.url);



        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, context.getApplicationContext().getPackageName()));

        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(proxyURL));

        player.prepare(mediaSource);
        player.setPlayWhenReady(true);


        playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(
                context,
                App.CHANNEL_ID,
                R.string.app_name,
                App.NOTIFICATION_ID,
                new PlayerNotificationManager.MediaDescriptionAdapter() {
                    @Override
                    public String getCurrentContentTitle(Player player) {
                        return track.name;

                    }

                    @Nullable
                    @Override
                    public PendingIntent createCurrentContentIntent(Player player) {

                        Intent intent = new Intent(context, Main.class);

                        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    }

                    @Nullable
                    @Override
                    public String getCurrentContentText(Player player) {

                        return track.artist;


                    }

                    @Nullable
                    @Override
                    public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {

                        final Bitmap[] r = new Bitmap[1];
                        Glide.with(context).asBitmap()
                                .load(track.album_art)
                                .into(new CustomTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                        r[0] =  resource;
                                    }

                                    @Override
                                    public void onLoadCleared(@Nullable Drawable placeholder) {
                                    }
                                });
                        return r[0];
                    }
                }

        );




        playerNotificationManager.setNotificationListener(
                new PlayerNotificationManager.NotificationListener() {

                    @Override
                    public void onNotificationStarted(int notificationId, Notification notification) {
                        startForeground(notificationId,notification);

                    }

                    @Override
                    public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
                        stopSelf();
                    }
                }
        );



        playerNotificationManager.setPlayer(player);




    }


    public SimpleExoPlayer getplayerInstance() {
        return player;
    }

    public class LocalBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }


}










