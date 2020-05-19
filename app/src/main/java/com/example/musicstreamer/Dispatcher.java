package com.example.musicstreamer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class Dispatcher extends Activity {

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.login);

        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Main", "signInAnonymously:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            startActivity(new Intent(Dispatcher.this, Main.class));

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Main", "signInAnonymously:failure", task.getException());
                            Toast.makeText(Dispatcher.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                    }
                });


    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null)
        {
            startActivity(new Intent(Dispatcher.this, Main.class));

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        finishAffinity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}