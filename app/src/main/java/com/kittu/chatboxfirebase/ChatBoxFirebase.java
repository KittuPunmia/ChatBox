package com.kittu.chatboxfirebase;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

public class ChatBoxFirebase extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if(!FirebaseApp.getApps(this).isEmpty())
        {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);

            //Piccaso
            Picasso.Builder builder=new Picasso.Builder(this);
            builder.downloader(new OkHttp3Downloader(this,Integer.MAX_VALUE));
            Picasso built=builder.build();
            built.setIndicatorsEnabled(true);
            built.setLoggingEnabled(true);
            Picasso.setSingletonInstance(built);
        }
    }
}
