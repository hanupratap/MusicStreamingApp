package com.example.musicstreamer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
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
import androidx.appcompat.widget.SearchView;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.varunest.sparkbutton.SparkButton;
import com.varunest.sparkbutton.SparkEventListener;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.paperdb.Paper;


import static com.bumptech.glide.request.RequestOptions.bitmapTransform;
import static com.bumptech.glide.request.RequestOptions.diskCacheStrategyOf;
import static com.google.android.exoplayer2.offline.DownloadService.startForeground;

public class Player extends Fragment {

    Toolbar toolbar;
    TextView tv;
    private boolean playWhenReady = false;
    private int currentWindow = 0;

    private long playbackPosition = 0;
    ImageView img;
    FloatingActionButton floatingActionButton;
    Track item = new Track();
    TextView lyrics;
    DocumentReference dr;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ImageView imageView;
    CollapsingToolbarLayout collapsingToolbarLayout;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();


    private Intent intent;

    private static Track prev_track;

    SparkButton sparkButton;
    Main main;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        intent = new Intent(getActivity(), PlayerService.class);
        Util.startForegroundService(getActivity(),intent);
        main = (Main) getActivity();


        main.bindService(intent, main.mConnection, Context.BIND_AUTO_CREATE);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_player, container, false);



        App.playerControlView = view.findViewById(R.id.player_control_view);

        main = (Main) getActivity();


        main.bottomNavigationView.getMenu().findItem(R.id.play).setChecked(true);
        main.selectedFragment = new Player();

        item = App.current_track;
        prev_track = item;




        Paper.init(getActivity());

        sparkButton = view.findViewById(R.id.spark_button);



        if(!user.isAnonymous())
        {
            FirebaseFirestore.getInstance().collection("Users").document(user.getUid()).collection("Favourites").document(App.current_track.id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(documentSnapshot.exists())
                    {
                        if(documentSnapshot.getBoolean("isfav"))
                        {
                            sparkButton.setChecked(true);
                        }
                        else {
                            sparkButton.setChecked(false);
                        }
                    }

                }
            });
        }




        if(mAuth.getCurrentUser().isAnonymous())
        {
            sparkButton.setVisibility(View.GONE);
        }
        else {
            sparkButton.setVisibility(View.VISIBLE);

        }


        sparkButton.setEventListener(new SparkEventListener(){

            @Override
            public void onEvent(ImageView button, boolean buttonState) {
                if(buttonState)
                {
                    Toast.makeText(main, "Added to favourites", Toast.LENGTH_SHORT).show();
                    Map map = new HashMap<>();
                    map.put("isfav",true);
                    map.put("path", App.current_track.path);
                    db.collection("Users").document(mAuth.getCurrentUser().getUid()).collection("Favourites").document(App.current_track.id).set(map, SetOptions.merge());
                }
                else
                {
                    Toast.makeText(main, "Removed to favourites", Toast.LENGTH_SHORT).show();
                    Map map = new HashMap<>();
                    map.put("isfav",false);
                    map.put("path", App.current_track.path);
                    db.collection("Users").document(mAuth.getCurrentUser().getUid()).collection("Favourites").document(App.current_track.id).set(map, SetOptions.merge());


                }
            }

            @Override
            public void onEventAnimationEnd(ImageView button, boolean buttonState) {

            }

            @Override
            public void onEventAnimationStart(ImageView button, boolean buttonState) {

            }
        });
        imageView = view.findViewById(R.id.album_img);
        img = view.findViewById(R.id.image_main);


        tv = view.findViewById(R.id.track_title);

        lyrics = view.findViewById(R.id.lyrics);

        toolbar = view.findViewById(R.id.toolbar1);
        collapsingToolbarLayout = view.findViewById(R.id.coll_bar);
        floatingActionButton = view.findViewById(R.id.fab_note);
        collapsingToolbarLayout.setTitleEnabled(false);


        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
//        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                (getActivity()).onBackPressed();
//            }
//        });






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
                loadLyrics();
            }
        }).start();

        Glide.with(getActivity()).load(item.artist).centerCrop().into(img);


        return view;
    }



    void loadLyrics()
    {

        final List<String> urls = new ArrayList<String>(); //to read each line
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


    void initializePlayer() {
        main.initializePlayer();
    }


    @Override
    public void onStart() {
        Glide.with(getActivity()).load(item.artist_art).centerCrop().into(img);
        Glide.with(getActivity()).load(item.album_art).centerCrop().into(imageView);
        if (Util.SDK_INT >= 24) {
            initializePlayer();
        }
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();

        if ((Util.SDK_INT < 24 || App.player == null)) {
            initializePlayer();
        }
    }



    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onStop() {

        super.onStop();


    }

    @Override
    public void onDestroy() {
//        getActivity().unbindService(main.mConnection);
//        main.mService.stopSelf();
//        main.mBound = false;
//        if (Util.SDK_INT >= 24) {
//            releasePlayer();
//        }
        super.onDestroy();
    }

//    private void releasePlayer() {
//
//
//        if (main.player != null) {
//
//            playWhenReady = main.player.getPlayWhenReady();
//            playbackPosition = main.player.getCurrentPosition();
//            currentWindow = main.player.getCurrentWindowIndex();
//            main.player.release();
//            main.player = null;
//        }
//    }




}