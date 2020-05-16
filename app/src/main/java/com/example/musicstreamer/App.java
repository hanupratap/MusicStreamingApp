package com.example.musicstreamer;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import java.util.List;

public class App extends Application {
    public static final String CHANNEL_ID = "ServiceChannel";
    public static final int NOTIFICATION_ID = 1234;



    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
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
