package com.kittu.chatboxfirebase;


import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    RecyclerView recyclerView;
    private FirebaseUser user;

    private List<Friends> friends=new ArrayList<>();
    private DatabaseReference mdatabaseReference;
private DatabaseReference databaseReference;
    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        recyclerView= view.findViewById(R.id.friends_list);

        user = FirebaseAuth.getInstance().getCurrentUser();
        mdatabaseReference = FirebaseDatabase.getInstance().getReference().child("Friends");
databaseReference=FirebaseDatabase.getInstance().getReference().child("Users");


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        final FriendsFragment.FriendsAdapter friendsAdapter = new FriendsFragment.FriendsAdapter(getContext(), friends);
        recyclerView.setAdapter(friendsAdapter);


        mdatabaseReference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot childSnapshot) {
                for(DataSnapshot dataSnapshot : childSnapshot.getChildren()) {
                    final String a=dataSnapshot.getKey();
                  //   String b=dataSnapshot.child("date").getValue().toString();
                    final String b= dataSnapshot.child("date").getValue().toString();
                    Log.i("data", "onDataChange: "+b);
                    Log.i("data", "onDataChange: "+a);
                    databaseReference.child(a).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                        String name=dataSnapshot.child("name").getValue().toString();
                        String image=dataSnapshot.child("thumb_image").getValue().toString();
                        Friends f=new Friends(b,a,name,image);
                        friends.add(f);
                            friendsAdapter.notifyDataSetChanged();

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(getContext(), "error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "error", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
    public static class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendsViewHolder>
    {
        private Context context;
        private List<Friends> friends;
        FriendsAdapter(Context context, List<Friends> friends) {
            this.context = context;
            this.friends = friends;
        }
        @NonNull
        @Override
        public FriendsAdapter.FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View v = inflater.inflate(R.layout.userslist, parent, false);
            return new FriendsAdapter.FriendsViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull FriendsAdapter.FriendsViewHolder holder, int position) {
            Friends c=friends.get(position);

            holder.user_single_name.setText(c.getName());
            holder.user_single_status.setText(c.getDate());
            if(!c.getImage().equals("default")) {
                Picasso.get().load(c.getImage()).placeholder(R.drawable.profile).fit().centerCrop().into(holder.user_single_image);
            }

         /*   holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent i=new Intent(this,ProfileActivity.class);
                    Log.i("keyvalue", "onClick: "+user_id.get(position));
                    i.putExtra("user_key",user_id.get(position));
                    startActivity(i);

                }
            });
            */


        }

        @Override
        public int getItemCount() {
            return friends.size();
        }

        class FriendsViewHolder extends RecyclerView.ViewHolder {
            TextView user_single_name;
            CircleImageView user_single_image;
            TextView user_single_status;

            FriendsViewHolder(View itemView) {
                super(itemView);
                user_single_name=itemView.findViewById(R.id.user_single_name);
                user_single_status=itemView.findViewById(R.id.user_single_status);
                user_single_image=itemView.findViewById(R.id.user_single_image);
            }
        }
    }
    public void setAdapterAndPassData(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        FriendsFragment.FriendsAdapter friendsAdapter = new FriendsFragment.FriendsAdapter(getActivity(), friends);
        recyclerView.setAdapter(friendsAdapter);
    }
}


