package com.example.musicstreamer;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class Main extends AppCompatActivity  {

    FragmentManager fragmentManager ;
    private AppBarConfiguration mAppBarConfiguration;
    private FirebaseAuth mAuth;
    private SimpleExoPlayer player;
    private PlayerNotificationManager playerNotificationManager;
    String currentFragment;
    boolean exited = false;
    PlayerService playerService = new PlayerService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        fragmentManager = getSupportFragmentManager();
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();



        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Main", "signInAnonymously:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Main", "signInAnonymously:failure", task.getException());
                            Toast.makeText(Main.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                    }
                });



        setContentView(R.layout.activity_main2);


        fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                List<Fragment> f = fragmentManager.getFragments();
                Fragment frag = f.get(0);
                currentFragment = frag.getClass().getSimpleName();
            }
        });

    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    @Override
    public void onBackPressed()
    {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        List<Fragment> fragments = fragmentManager.getFragments();
        String fragment_name = fragments.get(fragments.size()-1).getClass().toString();
        String name_pack = Player.class.toString();
        if(fragment_name.equals(name_pack))
        {
            fragmentTransaction.replace(R.id.main_fragment,new TrackList()).commit();

        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {

        if(playerNotificationManager!=null && player!=null)
        {
            this.playerNotificationManager.setPlayer(null);
            this.player.release();
            this.player = null;
        }

        super.onDestroy();

    }


    public void func(SimpleExoPlayer player, PlayerNotificationManager playerNotificationManager)
    {
        this.player = player;
        this.playerNotificationManager = playerNotificationManager;
    }

}





