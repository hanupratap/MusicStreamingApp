package com.example.musicstreamer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.airbnb.lottie.LottieAnimationView;
import com.danikula.videocache.HttpProxyCacheServer;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;


public class Dispatcher extends Activity {

    SignInButton signInButton;
    Button AnonymSignIn;
    private static final int RC_SIGN_IN = 1234;
    FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    LottieAnimationView animationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.login);

        AnonymSignIn = findViewById(R.id.signInAnonym);

        animationView = findViewById(R.id.animationView);
        animationView.setVisibility(View.GONE);
        signInButton = findViewById(R.id.GsignIn);
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        //Initializing Cache
        App.proxyServer =  new HttpProxyCacheServer.Builder(this).maxCacheSize(1024 * 1024 * 1024).build();



        AnonymSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anonymSignIn();
            }
        });

        mGoogleSignInClient = GoogleSignIn.getClient(Dispatcher.this, gso);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });



    }


    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(Dispatcher.class.toString(), "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(Dispatcher.class.toString(), "Google sign in failed", e);
                // ...
            }
        }
    }

    protected void startApp()
    {
        signInButton.setVisibility(View.GONE);
        AnonymSignIn.setVisibility(View.GONE);
        animationView.setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(Dispatcher.this, Main.class));
            }
        }, 2500);
    }


    protected void anonymSignIn()
    {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Main", "signInAnonymously:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            startApp();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Main", "signInAnonymously:failure", task.getException());
                            Toast.makeText(Dispatcher.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                    }
                });

    }


    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(Dispatcher.class.toString(), "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            startApp();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(Dispatcher.class.toString(), "signInWithCredential:failure", task.getException());
                            Toast.makeText(Dispatcher.this, "Login Failed", Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }




    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null)
        {
            startApp();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        finishAffinity();
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}