package com.example.musicstreamer;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.telecom.Call;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

public class Main extends AppCompatActivity  {

    FragmentManager fragmentManager ;
    BottomNavigationView bottomNavigationView;
    public PlayerService mService;
    public boolean mBound = false;
    private boolean playWhenReady = false;
    private int currentWindow = 0;

    private long playbackPosition = 0;

    Fragment selectedFragment = null;



    public ServiceConnection mConnection = new ServiceConnection() {
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
    protected void onCreate(Bundle savedInstanceState) {
        fragmentManager = getSupportFragmentManager();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);




        App.player = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector());






        bottomNavigationView = findViewById(R.id.nav);


        if(App.current_track == null)
        {
            bottomNavigationView.getMenu().findItem(R.id.play).setEnabled(false);
        }


        Paper.init(this);
       final Track track = Paper.book().read("current_track");

       if(track == null)
       {
           bottomNavigationView.getMenu().findItem(R.id.play).setEnabled(true);
       }
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            Fragment selectedFragment = null;

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentTransaction transaction;
                switch (item.getItemId()) {
                    case R.id.explore:
                        selectedFragment = new TrackList();
                        getSupportFragmentManager().popBackStack(Player.class.toString(),0);
                        transaction = getSupportFragmentManager().beginTransaction();
                        transaction.add(R.id.main_fragment, selectedFragment, TrackList.class.toString());
                        transaction.addToBackStack(TrackList.class.toString());
                        transaction.commit();
                        return true;

                    case R.id.play:

                        selectedFragment = new Player();

                        getSupportFragmentManager().popBackStack(Player.class.toString(),0);

                        return true;



                    case R.id.search:

                        selectedFragment = new Search();
                        getSupportFragmentManager().popBackStack(Player.class.toString(),0);
                        transaction = getSupportFragmentManager().beginTransaction();
                        transaction.add(R.id.main_fragment, selectedFragment, Search.class.toString());
                        transaction.addToBackStack(Search.class.toString());
                        transaction.commit();
                        return true;

                }
                return false;

            }

        });
    }




    void initializePlayer() {

        if (mBound) {
            App.player = mService.getplayerInstance();
            App.playerControlView.setPlayer(App.player);
        }
    }



    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed()
    {

        int fragmentCount = getSupportFragmentManager().getBackStackEntryCount();
        if (fragmentCount == 1) {
            finishAffinity();
        }
        String a = selectedFragment.getClass().toString();
        String b = Player.class.toString();
        if(a.equals(b))
        {
            FragmentTransaction transaction;
            selectedFragment = new TrackList();
            transaction = getSupportFragmentManager().beginTransaction();
            getSupportFragmentManager().popBackStack(Player.class.toString(),0);
            transaction.add(R.id.main_fragment, selectedFragment, TrackList.class.toString());
            transaction.addToBackStack(TrackList.class.toString());
            transaction.commit();

            bottomNavigationView.getMenu().findItem(R.id.explore).setChecked(true);
        }
        else
        {
            super.onBackPressed();
        }

    }

    @Override
    protected void onDestroy() {
        if(mBound==true)
        {
            this.unbindService(mConnection);
            mService.stopSelf();
        }

        mBound = false;
        if (Util.SDK_INT >= 24) {
            releasePlayer();
        }

        super.onDestroy();

    }




    @Override
    protected void onResume() {

        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Util.SDK_INT < 24) {
            releasePlayer();
        }
    }
    private void releasePlayer() {


        if (App.player != null) {

            playWhenReady = App.player.getPlayWhenReady();
            playbackPosition = App.player.getCurrentPosition();
            currentWindow = App.player.getCurrentWindowIndex();
            App.player.release();
            App.player = null;
        }
    }


}





