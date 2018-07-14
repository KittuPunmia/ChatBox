package com.kittu.chatboxfirebase;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class MessageActivity extends AppCompatActivity {
//data from friends activity
    private String Chatuser;
    private String Chatuser_name;
    private String ChatImage;
    //Firebase
    FirebaseUser user;
    private DatabaseReference Rootref;
    String current_user ;
    private StorageReference mImagestorage;

    private android.support.v7.widget.Toolbar toolbar;
    //chat_bar.xml

    TextView mtitle;
TextView mLastseen;
 CircleImageView mProfileImage;
 //mesaage.xml
 EditText chat_message_view;
 ImageButton send_button;
 ImageButton add_button;
 RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    SwipeRefreshLayout message_swipe_layout;
    private final List<messages> messagelist=new ArrayList<>();
private MessageAdapter messageAdapter;

//pagination
private static final int TOTAL_ITEMS_TO_LOAD=10;
private int mCurrentPage=1;
//new Soln
private int itempos=0;
String mLastKey="";
String mPrevKey="";

    private  static final int GALLERY_PICK=20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Chatuser = getIntent().getStringExtra("user_key");
        Chatuser_name = getIntent().getStringExtra("user_name");
        Log.i("name", "onCreate: "+Chatuser_name);
        ChatImage = getIntent().getStringExtra("user_image");

        user=FirebaseAuth.getInstance().getCurrentUser();
        current_user=user.getUid();
        Rootref = FirebaseDatabase.getInstance().getReference();
        mImagestorage= FirebaseStorage.getInstance().getReference();

        toolbar = findViewById(R.id.appbarid);





        //set custom toolbar
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View actionbarview = inflater.inflate(R.layout.chat_bar, null);
        actionBar.setCustomView(actionbarview);

        mtitle = findViewById(R.id.custom_bar_title);
        mLastseen = findViewById(R.id.custom_bar_seen);
        mProfileImage = findViewById(R.id.custom_bar_image);
        add_button=(ImageButton)findViewById(R.id.add_button);
        send_button=(ImageButton)findViewById(R.id.send_button);
        chat_message_view=findViewById(R.id.chat_message_view);

        mtitle.setText(Chatuser_name);
        Picasso.get().load(ChatImage).fit().centerCrop().into(mProfileImage);

        recyclerView=findViewById(R.id.messages_list);
        message_swipe_layout=findViewById(R.id.message_swipe_layout);
         linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        messageAdapter=new MessageAdapter(getApplicationContext(),messagelist);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(messageAdapter);

        loadMessages();

//to check if person is online or last seen
        Rootref.child("Users").child(Chatuser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                if (online.equals("true")) {
                    mLastseen.setText("ONLINE");
                } else {
                    GetTimeAgo getTimeAgo=new GetTimeAgo();
                    long lastTime=Long.parseLong(online);
                    String lastseentime=getTimeAgo.getTimeAgo(lastTime,getApplicationContext());
                    mLastseen.setText(lastseentime);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MessageActivity.this, "ERROR", Toast.LENGTH_SHORT).show();
            }
        });
//to add chat details when person opens a csend messaage from dialog
        Rootref.child("Chat").child(current_user).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
            if(!dataSnapshot.hasChild(Chatuser))
            {
             Map chataddmap=new HashMap();
             chataddmap.put("seen",false);
             chataddmap.put("timestamp",ServerValue.TIMESTAMP);
             Map chatusermap=new HashMap();
            chatusermap.put("Chat/"+current_user+"/"+Chatuser,chataddmap);
            chatusermap.put("Chat/"+Chatuser+"/"+current_user,chataddmap);
            Rootref.updateChildren(chatusermap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError!=null)
                    {
                        Log.i("DATABASE ERROR", "onComplete: "+ databaseError.getMessage());
                    }
                }
            });
            }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
            message_swipe_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    mCurrentPage++;
                    itempos=0; //recycler view first item pos should start from 1 everytime so we set it to 0 when it loads
                   // messagelist.clear(); becuase we aare loading onlu new message so we dont do this
                    loadMoreMessages();

                }
            });
         add_button.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent i=new Intent();
                 i.setType("image/*");
                 i.setAction(Intent.ACTION_GET_CONTENT);
                 startActivityForResult(Intent.createChooser(i,"SELECT IMAGE"),GALLERY_PICK);

             }
         });

    }
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();

            CropImage.activity(imageUri).setAspectRatio(1,1)
                    .setMinCropWindowSize(500,500)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                final File thumbPath = new File(resultUri.getPath());
                byte[] thumb_byte = new byte[0];
                try {
                    Bitmap compressedImageBitmap = new Compressor(this).setMaxHeight(200).setMaxWidth(200)
                            .setQuality(75)
                            .compressToBitmap(thumbPath);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    thumb_byte = baos.toByteArray();

                } catch (IOException e) {
                    e.printStackTrace();
                }


            final String current_user_ref = "messages/" + current_user + "/" + Chatuser;
            final String chat_user_ref = "messages/" + Chatuser + "/" + current_user;
            DatabaseReference push = Rootref.child("messages").child(current_user).child(Chatuser).push();
            final String push_id = push.getKey();
            StorageReference filePath = mImagestorage.child("message_images")
                    .child(push_id + ".jpg");
                final byte[] finalThumb_byte = thumb_byte;

                UploadTask uploadTask = filePath.putBytes(finalThumb_byte);
                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            final String thumb_download_url = task.getResult().getDownloadUrl().toString();
                            Log.i("image", "onComplete: "+thumb_download_url);

                            Map details = new HashMap();
                            details.put("message",thumb_download_url);
                            details.put("seen", false);
                            details.put("time", ServerValue.TIMESTAMP);
                            details.put("type", "image");
                            details.put("from", current_user);

                            Map messageusermap = new HashMap();
                            messageusermap.put(current_user_ref + "/" + push_id, details);
                            messageusermap.put(chat_user_ref + "/" + push_id, details);
                            chat_message_view.setText("");

                            Rootref.updateChildren(messageusermap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    if (databaseError != null) {
                                        Log.i("DATABASE ERROR", "onComplete: " + databaseError.getMessage());
                                    }
                                }
                            });
                        }
                    };
                });
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
            Exception error = result.getError();
            }
            }
        }

        private void loadMoreMessages(){
    DatabaseReference messageref=Rootref.child("messages").child(current_user).child(Chatuser);
    //order by to order end at specifies the key before which you want to load the data startat tehe key after which you want to load data
    Query messagequery=messageref.orderByKey().endAt(mLastKey).limitToLast(10);
    messagequery.addChildEventListener(new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            if(dataSnapshot.exists()) {
                messages message = dataSnapshot.getValue(messages.class);
                String messageKey=dataSnapshot.getKey();

                if(!mPrevKey.equals(messageKey))
                {
                    messagelist.add(itempos++,message);

                }
                else
                {
                    mPrevKey=mLastKey;
                }
                if(itempos==1)
                {
                    mLastKey=messageKey;
                }
                messageAdapter.notifyDataSetChanged();
                message_swipe_layout.setRefreshing(false);
                linearLayoutManager.scrollToPositionWithOffset(mCurrentPage*TOTAL_ITEMS_TO_LOAD,0);
            }
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

}
    private void loadMessages() {
        DatabaseReference messageref=Rootref.child("messages").child(current_user).child(Chatuser);
        Query messagequery=messageref.limitToLast(mCurrentPage*TOTAL_ITEMS_TO_LOAD);
            messagequery.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if(dataSnapshot.exists()) {
                        messages message = dataSnapshot.getValue(messages.class);
                        itempos++;
                        if(itempos==1)
                        {
                            String messageKey=dataSnapshot.getKey();
                            mLastKey=messageKey;
                            mPrevKey=messageKey;
                        }
                        messagelist.add(message);
                        messageAdapter.notifyDataSetChanged();
                        recyclerView.scrollToPosition(messagelist.size()-1); //loads page to last
                        message_swipe_layout.setRefreshing(false);
                    }
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
        }


    private void sendMessage(){
        String message=chat_message_view.getText().toString();
       if(!TextUtils.isEmpty(message)){

           String current_user_ref="messages/"+current_user+"/"+Chatuser;
           String chat_user_ref="messages/"+Chatuser+"/"+current_user;
           DatabaseReference push=Rootref.child("messages").child(current_user).child(Chatuser).push();
           String push_id=push.getKey();
           Map details=new HashMap();
           details.put("message",message);
           details.put("seen",false);
           details.put("time",ServerValue.TIMESTAMP);
           details.put("type","text");
          details.put("from",current_user);

           Map messageusermap=new HashMap();
           messageusermap.put(current_user_ref+"/"+push_id,details);
           messageusermap.put(chat_user_ref+"/"+push_id,details);
           chat_message_view.setText("");
           Rootref.updateChildren(messageusermap, new DatabaseReference.CompletionListener() {
               @Override
               public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                   if(databaseError!=null)
                   {
                       Log.i("DATABASE ERROR", "onComplete: "+ databaseError.getMessage());
                   }
               }
           });
       }
    }
        @Override
        protected void onStop () {
            super.onStop();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                Rootref.child("Users").child(user.getUid()).child("online").setValue(ServerValue.TIMESTAMP);
            }
        }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            Rootref.child("Users").child(user.getUid()).child("online").setValue("true");
        }
    }
    }