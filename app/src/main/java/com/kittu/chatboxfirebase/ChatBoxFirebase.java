package com.kittu.chatboxfirebase;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

public class ChatBoxFirebase extends Application {
    FirebaseUser user;
    DatabaseReference databaseReference;
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

            user= FirebaseAuth.getInstance().getCurrentUser();
            if(user!=null) {
                databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null) {
                            databaseReference.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        }
    }
}
