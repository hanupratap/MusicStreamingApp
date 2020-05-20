package com.example.musicstreamer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
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
import androidx.core.app.ActivityCompat;
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

    FragmentTransaction transaction;
    Fragment selectedFragment = null;



    public ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) iBinder;
            mService = binder.getService();
            mBound = true;
            bottomNavigationView.getMenu().findItem(R.id.play).setEnabled(true);
            initializePlayer();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
            bottomNavigationView.getMenu().findItem(R.id.play).setEnabled(false);

        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);




        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();

        transaction.add(R.id.main_fragment,new TrackList(), TrackList.class.toString());
        transaction.addToBackStack(TrackList.class.toString());
        transaction.commit();
        Toast.makeText(this, "Double back to EXIT", Toast.LENGTH_SHORT).show();



        bottomNavigationView = findViewById(R.id.nav);



        selectedFragment = new TrackList();

        fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment fr = fragmentManager.findFragmentById(R.id.main_fragment);
                if(fr!=null){
                    Log.e("fragment=", fr.getClass().getSimpleName());
                    selectedFragment = fr;

                }
            }
        });

        App.player = new SimpleExoPlayer.Builder(this).setTrackSelector(new DefaultTrackSelector(this)).build();

        bottomNavigationView.getMenu().findItem(R.id.play).setEnabled(false);


        if(App.firstStart == false)
        {
            if(App.current_track.id == null)
            {
                Paper.init(this);
                App.current_track = Paper.book().read("current_track");

                if(App.current_track.id != null)
                {
                    bottomNavigationView.getMenu().findItem(R.id.play).setEnabled(true);
                }
                else
                {
                    bottomNavigationView.getMenu().findItem(R.id.play).setEnabled(false);
                }

            }
            else
            {
                bottomNavigationView.getMenu().findItem(R.id.play).setEnabled(true);
            }

        }





        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.explore:

                        if (!selectedFragment.getClass().toString().equals(TrackList.class.toString())) {
                            selectedFragment = new TrackList();
                            getSupportFragmentManager().popBackStack(Player.class.toString(), 0);
                            transaction = getSupportFragmentManager().beginTransaction();
                            transaction.setCustomAnimations(R.anim.fragment_open_enter, R.anim.fragment_close_exit);

                            transaction.add(R.id.main_fragment, selectedFragment, TrackList.class.toString());
                            transaction.addToBackStack(TrackList.class.toString());
                            transaction.commit();
                        }


                        return true;

                    case R.id.play:



                        Fragment temp_frag = getSupportFragmentManager().findFragmentByTag(Player.class.toString());
                        selectedFragment = new Player();
                        if(temp_frag == null)
                        {
                            transaction = getSupportFragmentManager().beginTransaction();
                            getSupportFragmentManager().popBackStack(Player.class.toString(),FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            transaction.add(R.id.main_fragment, selectedFragment, Player.class.toString());
                            transaction.addToBackStack(Player.class.toString());
                            transaction.commit();
                        }
                        else
                        {
                            getSupportFragmentManager().popBackStack(Player.class.toString(), 0);

                        }

                        return true;


                    case R.id.search:


                        if (!selectedFragment.getClass().toString().equals(Search.class.toString()))
                        {
                            selectedFragment = new Search();
                            getSupportFragmentManager().popBackStack(Player.class.toString(),0);
                            transaction = getSupportFragmentManager().beginTransaction();
                            transaction.setCustomAnimations(R.anim.fragment_open_enter, R.anim.fragment_close_exit);
                            transaction.add(R.id.main_fragment, selectedFragment, Search.class.toString());
                            transaction.addToBackStack(Search.class.toString());
                            transaction.commit();
                        }




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

    boolean doubleBackToExitPressedOnce = false;
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

            if(doubleBackToExitPressedOnce)
            {
                finishAffinity();
            }

        this.doubleBackToExitPressedOnce = true;


        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);


    }


    @Override
    protected void onDestroy() {

        if(App.current_track!=null)
        {
            Paper.book().write("current_track", App.current_track);

        }
        if(mBound)
        {
            this.unbindService(mConnection);
            mService.stopSelf();
        }

        mBound = false;

            releasePlayer();


        super.onDestroy();

    }




    @Override
    protected void onResume() {

        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
//            releasePlayer();

    }
    protected void releasePlayer() {


        if (App.player != null) {

            playWhenReady = App.player.getPlayWhenReady();
            playbackPosition = App.player.getCurrentPosition();
            currentWindow = App.player.getCurrentWindowIndex();
            App.player.release();
            App.player = null;
        }
    }


}





