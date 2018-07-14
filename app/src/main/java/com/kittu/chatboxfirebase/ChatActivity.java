package com.kittu.chatboxfirebase;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class ChatActivity extends AppCompatActivity {
    private android.support.v7.widget.Toolbar toolbar;
ViewPager viewPager;
DatabaseReference mUserDatabase;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

private SectionsPagerAdapter msectionspageradapter;
private TabLayout tablayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        toolbar=findViewById(R.id.toolbarid);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("CHAT BOX");
        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("Users");

        viewPager=findViewById(R.id.tabpager);
        msectionspageradapter=new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(msectionspageradapter);
        tablayout=findViewById(R.id.main_tabs);
        tablayout.setupWithViewPager(viewPager);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.m1,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);
         if(R.id.logoutbtn==item.getItemId())
         {
             FirebaseAuth.getInstance().signOut();
             Intent i=new Intent(ChatActivity.this,MainActivity.class);
             startActivity(i);
             finish();
         }
         if(R.id.setting==item.getItemId())
         {
             Intent i=new Intent(ChatActivity.this,SettingActivity.class);
             startActivity(i);

         }
        if(R.id.users==item.getItemId())
        {
            Intent i=new Intent(ChatActivity.this,UsersActivity.class);
            startActivity(i);

        }

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user!=null)
        {
            mUserDatabase.child(user.getUid()).child("online").setValue("true");
        }
        else
        {
            Intent i=new Intent(ChatActivity.this,MainActivity.class);
            startActivity(i);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(user!=null) {
            mUserDatabase.child(user.getUid()).child("online").setValue(ServerValue.TIMESTAMP);
        }
    }
}
