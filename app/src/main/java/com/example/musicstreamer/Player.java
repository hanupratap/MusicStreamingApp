package com.example.musicstreamer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.Image;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.telecom.ConnectionService;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

import io.paperdb.Paper;


import static com.bumptech.glide.request.RequestOptions.bitmapTransform;
import static com.bumptech.glide.request.RequestOptions.diskCacheStrategyOf;
import static com.google.android.exoplayer2.offline.DownloadService.startForeground;

public class Player extends Fragment {

    Toolbar toolbar;
    TextView tv;
    SimpleExoPlayer player;
    PlayerControlView playerControlView;
    private boolean playWhenReady = false;
    private int currentWindow = 0;
    FloatingActionButton floatingActionButton;
    private long playbackPosition = 0;
    String url;
    ImageView img;

    Track item = new Track();
    TextView lyrics;
    DocumentReference dr;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ImageView imageView;
    CollapsingToolbarLayout collapsingToolbarLayout;
    private PlayerService mService;
    private boolean mBound = false;
    private Intent intent;




    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) iBinder;
            mService = binder.getService();
            mBound = true;
            initializePlayer();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        item = Paper.book().read("current_track");

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        intent = new Intent(getActivity(), PlayerService.class);
        Util.startForegroundService(getActivity(),intent);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        initializePlayer();


        Paper.init(getActivity());





        imageView = view.findViewById(R.id.album_img);
        img = view.findViewById(R.id.image_main);


        tv = view.findViewById(R.id.track_title);
        playerControlView = view.findViewById(R.id.player_control_view);
        lyrics = view.findViewById(R.id.lyrics);

        toolbar = view.findViewById(R.id.toolbar1);
        collapsingToolbarLayout = view.findViewById(R.id.coll_bar);
        floatingActionButton = view.findViewById(R.id.fab_note);
        collapsingToolbarLayout.setTitleEnabled(false);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                (getActivity()).onBackPressed();
            }
        });





        lyrics.setVisibility(View.GONE);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lyrics.getVisibility() == View.GONE) {
                    lyrics.setVisibility(View.VISIBLE);
                } else {
                    lyrics.setVisibility(View.GONE);
                }
            }
        });








        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(item.artist);



        tv.setText(item.name);

        new Thread(new Runnable() {

            public void run() {



                final ArrayList<String> urls = new ArrayList<String>(); //to read each line
                //TextView t; //to show the result, please declare and find it inside onCreate()


                try {
                    // Create a URL for the desired page
                    URL url = new URL(item.lyrics); //My text file location
                    //First open the connection
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(60000); // timing out in a minute

                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    //t=(TextView)findViewById(R.id.TextView1); // ideally do this in onCreate()
                    String str;
                    while ((str = in.readLine()) != null) {
                        urls.add(str);
                    }
                    in.close();
                } catch (Exception e) {
                    Log.d("MyTag", e.toString());
                }

                //since we are in background thread, to post results we have to go back to ui thread. do the following for that

                try {
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {

                            if(urls!=null){
                                lyrics.setText("\n");
                                for (int i = 0; i < urls.size(); i++) {
                                    lyrics.append(urls.get(i) + '\n');
                                }
                            }

                        }
                    });
                }
                catch (Exception e)
                {
                    Log.d("TAG", e.toString());
                }




            }
        }).start();

        Glide.with(getActivity()).load(item.artist).centerCrop().into(img);


        if (Util.SDK_INT >= 24) {
            initializePlayer();
        }


        return view;
    }

    private void initializePlayer() {


        if (mBound) {
            player = mService.getplayerInstance();
            playerControlView.setPlayer(player);
        }
    }


    @Override
    public void onStart() {
        Glide.with(getActivity()).load(item.artist_art).centerCrop().into(img);
        Glide.with(getActivity()).load(item.album_art).centerCrop().into(imageView);
        super.onStart();



    }

    @Override
    public void onResume() {
        super.onResume();
        hideSystemUi();
        if ((Util.SDK_INT < 24 || player == null)) {
            initializePlayer();
        }
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        playerControlView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT < 24) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {

        super.onStop();


    }

    @Override
    public void onDestroy() {
        getActivity().unbindService(mConnection);
        mService.stopSelf();
        mBound = false;
        if (Util.SDK_INT >= 24) {
            releasePlayer();
        }
        super.onDestroy();
    }

    private void releasePlayer() {


        if (player != null) {

            playWhenReady = player.getPlayWhenReady();
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            player.release();
            player = null;
        }
    }


}