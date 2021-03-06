package com.kittu.chatboxfirebase;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

import butterknife.BindAnim;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileActivity extends AppCompatActivity {
    private DatabaseReference mdatabaseReference;
    private FirebaseUser user;
    private DatabaseReference FriendRequestReference;
    private  DatabaseReference FriendsDatabase;
    private String mCurrentState;
private ProgressDialog progressDialog;
    @BindView(R.id.profile_name)
    TextView profile_name;
    @BindView(R.id.profile_status)
    TextView profile_status;
    @BindView(R.id.profile_friends)
    TextView profile_friends;

    @BindView(R.id.send_request)
    Button send_request;
    @BindView(R.id.decline_request)
    Button decline_request;
    @BindView(R.id.profile_image)
    ImageView profile_image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        final String user_key=getIntent().getStringExtra("user_key");
mCurrentState="not_friends";

        user= FirebaseAuth.getInstance().getCurrentUser();


        progressDialog=new ProgressDialog(ProfileActivity.this);
        progressDialog.show();
        progressDialog.setTitle("LOADING  USER DATA");
        progressDialog.setMessage("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        FriendsDatabase=FirebaseDatabase.getInstance().getReference().child("Friends");
        mdatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(user_key);
        mdatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                // String value = dataSnapshot.getValue(String.class);
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                // Log.d(TAG, "Value is: " + value);
                profile_name.setText(name);
                profile_status.setText(status);
                Picasso.get().load(image).fit().centerCrop().placeholder(R.drawable.prof).into(profile_image);

                FriendRequestReference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(user_key))
                        {
                            String req_type=dataSnapshot.child(user_key).child("REQUEST_TYPE").getValue().toString();
                            if(req_type.equals("sent"))
                            {
                                mCurrentState="req_sent";
                                send_request.setText("CANCEL FRIEND REQUEST");
                            }
                            else if(req_type.equals("recieved"))
                            {
                                mCurrentState="req_recieved";
                                send_request.setText("ACCEPT FRIEND REQUEST");

                            }
                        }
                        else
                        {
                            FriendsDatabase.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user_key))
                                    {
                                        mCurrentState="friends";
                                        send_request.setText(" UNFRIEND");

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                progressDialog.dismiss();


            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                //  Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
FriendRequestReference =FirebaseDatabase.getInstance().getReference().child("Friend_request");
send_request.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        send_request.setEnabled(false);
        //////NOT FRIENDS STATE
        if (mCurrentState.equals("not_friends")) {
            FriendRequestReference.child(user.getUid()).child(user_key).child("REQUEST_TYPE").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        FriendRequestReference.child(user_key).child(user.getUid()).child("REQUEST_TYPE").setValue("recieved").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                send_request.setEnabled(true);
                                mCurrentState="req_sent";
                                send_request.setText("CANCEL FRIEND REQUEST");
                                Toast.makeText(ProfileActivity.this, "Request sent", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        send_request.setEnabled(true);

                        Toast.makeText(ProfileActivity.this, "Seending Request Failed", Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }


        ///CANCEL REQUEST STATE
        if(mCurrentState.equals("req_sent"))
        {
            FriendRequestReference.child(user.getUid()).child(user_key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    FriendRequestReference.child(user_key).child(user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            send_request.setEnabled(true);
                            mCurrentState="not_friends";
                            send_request.setText("SEND FRIEND REQUEST");
                            Toast.makeText(ProfileActivity.this, "Request cancelled", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }

        //REQUEST RECIEVED
        if(mCurrentState.equals("req_recieved"))
        {
            final String currentdate=DateFormat.getDateTimeInstance().format(new Date());
            FriendsDatabase.child(user.getUid()).child(user_key).setValue(currentdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                  FriendsDatabase.child(user_key).child(user.getUid()).setValue(currentdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                      @Override
                      public void onComplete(@NonNull Task<Void> task) {
                          FriendRequestReference.child(user.getUid()).child(user_key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                              @Override
                              public void onSuccess(Void aVoid) {
                                  FriendRequestReference.child(user_key).child(user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                      @Override
                                      public void onSuccess(Void aVoid) {
                                          send_request.setEnabled(true);
                                          mCurrentState="friends";
                                          send_request.setText("UNFRIEND");
                                          Toast.makeText(ProfileActivity.this, "ADDED AS FRIEND", Toast.LENGTH_SHORT).show(); }
                                  });
                              }
                          });

                      }
                  });
                }
            });
        }
        if(mCurrentState.equals("friends"))
        {
            FriendsDatabase.child(user.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        FriendsDatabase.child(user_key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                send_request.setEnabled(true);

                                mCurrentState = "not_friends";
                                send_request.setText("SEND FRIEND REQUEST");
                                Toast.makeText(ProfileActivity.this, "UNFRIENDED SUCCESSFULlY", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else
                    {
                        Toast.makeText(ProfileActivity.this, "erorr", Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }
    }
});
    }
}
