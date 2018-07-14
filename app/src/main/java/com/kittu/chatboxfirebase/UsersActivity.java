package com.kittu.chatboxfirebase;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Optional;
import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {
    private android.support.v7.widget.Toolbar toolbar;
    RecyclerView recycler_view;
    private DatabaseReference mdatabaseReference;
    int orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
     List<users> userlist=new ArrayList<>();
     List<String> user_id=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        toolbar = findViewById(R.id.toolbarid);
        recycler_view=  findViewById(R.id.users_list);

        setSupportActionBar(toolbar);
        ///  getSupportActionBar().setTitle("CHAT BOX");
        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);


//        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recycler_view.getContext(),
//                linearLayoutManager.getOrientation());

//        recycler_view.addItemDecoration(dividerItemDecoration);
        setRequestedOrientation(orientation);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recycler_view.setLayoutManager(linearLayoutManager);
        final UserAdapter userAdapter = new UserAdapter(UsersActivity.this, userlist);
        recycler_view.setAdapter(userAdapter);

        mdatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mdatabaseReference.keepSynced(true);

        Log.i("Values", "befiore recycle");
       /* mdatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               String a= dataSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/
                mdatabaseReference.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        users userval=dataSnapshot.getValue(users.class);
                      String a=  dataSnapshot.getKey();
                      //  Log.i("keyvalue", "onChildAdded: keyvalue"+dataSnapshot.getKey());
                        user_id.add(dataSnapshot.getKey());
                      //  Log.i("keyvalue", "onChildAdded: "+user_id);
                        userlist.add(userval);
                        userAdapter.notifyDataSetChanged();


                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        users userval=dataSnapshot.getValue(users.class);
                        userlist.add(userval);
                        userAdapter.notifyDataSetChanged();
                recycler_view.setAdapter(userAdapter);
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        users userval=dataSnapshot.getValue(users.class);
                        userlist.add(userval);
                        userAdapter.notifyDataSetChanged();
                        recycler_view.setAdapter(userAdapter);
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

       /* mdatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot childSnapshot) {
                for(DataSnapshot dataSnapshot : childSnapshot.getChildren()) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String status = dataSnapshot.child("status").getValue().toString();
                    String image = dataSnapshot.child("image").getValue().toString();
                    users userval = new users(name, image, status);
                    userlist.add(userval);
                    Log.i("value", "onChildAdded: " + name);
                }
//                userAdapter.notifyDataSetChanged();
                setAdapterAndPassData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/


    }
    public void setAdapterAndPassData(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recycler_view.setLayoutManager(linearLayoutManager);
        UserAdapter userAdapter = new UserAdapter(UsersActivity.this, userlist);
        recycler_view.setAdapter(userAdapter);
    }
    public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
        private Context context;
        private List<users> user;

        public UserAdapter(Context context, List<users> user) {
            this.context = context;
            this.user = user;
        }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View v = inflater.inflate(R.layout.userslist, parent, false);
            return new UserAdapter.UserViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final UserViewHolder holder, final int position) {
            users c=user.get(position);

            holder.user_single_name.setText(c.getName());
            holder.user_single_status.setText(c.getStatus());
            if(!c.getImage().equals("default")) {
                Picasso.get().load(c.getThumb_image()).placeholder(R.drawable.profile).fit().centerCrop().into(holder.user_single_image);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent i=new Intent(UsersActivity.this,ProfileActivity.class);
                    Log.i("keyvalue", "onClick: "+user_id.get(position));
                    i.putExtra("user_key",user_id.get(position));
                   startActivity(i);

                }
            });
        }

        @Override
        public int getItemCount() {
            return user.size();
        }

        public class UserViewHolder extends RecyclerView.ViewHolder {
            TextView user_single_name;
            CircleImageView user_single_image;
            TextView user_single_status;

            public UserViewHolder(View itemView) {
                super(itemView);
                user_single_name=itemView.findViewById(R.id.user_single_name);
                user_single_status=itemView.findViewById(R.id.user_single_status);
                user_single_image=itemView.findViewById(R.id.user_single_image);

            }
        }
    }


}
