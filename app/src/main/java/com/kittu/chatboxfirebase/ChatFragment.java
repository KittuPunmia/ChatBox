package com.kittu.chatboxfirebase;


import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    RecyclerView recyclerView;
    private FirebaseUser user;
    String user_id;
private DatabaseReference mConvDatabase;
private DatabaseReference mUsersDatabase;
private DatabaseReference mMessageDatabase;
    private List<Conv> conv=new ArrayList<>();
private View view;
    FirebaseRecyclerAdapter adapter;
    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =inflater.inflate(R.layout.fragment_chat, container, false);
        recyclerView= view.findViewById(R.id.chat_list);
        user = FirebaseAuth.getInstance().getCurrentUser();
        user_id=user.getUid();
        mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(user_id);
mConvDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(user_id);
        mUsersDatabase.keepSynced(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        Query convers=mConvDatabase.orderByChild("timestamp");
        FirebaseRecyclerOptions<Conv> options =
                new FirebaseRecyclerOptions.Builder<Conv>()
                        .setQuery(convers, Conv.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<Conv, ChatViewHolder>(options) {
            @Override
            public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.message for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.userslist, parent, false);

                return new ChatViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(final ChatViewHolder holder, int position, final Conv model) {
                // Bind the Chat object to the ChatHolder
                // ...
                final String chat_user_id= getRef(position).getKey();
                Log.i("TAG", "onBindViewHolder: "+chat_user_id);
                Query messagequery=mMessageDatabase.child(chat_user_id).limitToLast(1);
                messagequery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String data=dataSnapshot.child("message").getValue().toString();
                        holder.setMessage(data, model.isSeen());

/*                       if(dataSnapshot.child("type").getValue().toString().equals("text")) {
                            holder.setMessage(data, model.isSeen());
                        }
                        else
                        {
                            holder.setMessage("Photo",model.isSeen());
                        }*/
                        }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                mUsersDatabase.child(chat_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String name=dataSnapshot.child("name").getValue().toString();
                        final String userthumb=dataSnapshot.child("thumb_image").getValue().toString();
                        holder.setName(name);
                        holder.setImage(userthumb);
                        holder.mview.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {


                                Intent chatintent=new Intent(getContext(),MessageActivity.class);
                                //  Log.i("keyvalue", "onClick: "+user_id.get(position));
                                chatintent.putExtra("user_key", chat_user_id);
                                chatintent.putExtra("user_name",name);
                                chatintent.putExtra("user_image",userthumb);
                                startActivity(chatintent);

                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };


        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
        }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
         View mview;
            ChatViewHolder(View itemView) {
                super(itemView);
                mview=itemView;

            }

        public void setMessage(String data, boolean seen) {
            TextView user_single_status=mview.findViewById(R.id.user_single_status);
user_single_status.setText(data);
            if(seen)
            {
                user_single_status.setTypeface(user_single_status.getTypeface(), Typeface.BOLD);
            }
            else
            {
                user_single_status.setTypeface(user_single_status.getTypeface(), Typeface.NORMAL);

            }
        }
        public void setName(String name) {
            TextView user_single_name=mview.findViewById(R.id.user_single_name);
            user_single_name.setText(name);

        }

        public void setImage(String userthumb) {
            CircleImageView user_single_image=mview.findViewById(R.id.user_single_image);
            Picasso.get().load(userthumb).placeholder(R.drawable.profile).fit().centerCrop().into(user_single_image);
        }
    }


}
