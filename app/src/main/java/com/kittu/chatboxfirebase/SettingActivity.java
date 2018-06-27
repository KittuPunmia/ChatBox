package com.kittu.chatboxfirebase;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingActivity extends AppCompatActivity {
private DatabaseReference mdatabaseReference;
private FirebaseUser mCurrentUser;
//storage
private  static final int GALLERY_PICK=20;
private StorageReference mImagestorage;
private ProgressDialog progressDialog;
    @BindView(R.id.settings_name)
    TextView settings_name;
    @BindView(R.id.settings_status)
    TextView settings_status;
    @BindView(R.id.settings_image_btn)
    Button settings_image_btn;
    @BindView(R.id.settings_status_btn)
    Button settings_status_btn;
    CircleImageView circleImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        circleImageView = findViewById(R.id.settings_image);
        //storage
        mImagestorage= FirebaseStorage.getInstance().getReference();
        //database
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String CurrentUid = mCurrentUser.getUid();
        mdatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(CurrentUid);
        mdatabaseReference.keepSynced(true);
        mdatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                // String value = dataSnapshot.getValue(String.class);
                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                // Log.d(TAG, "Value is: " + value);
                settings_name.setText(name);
                settings_status.setText(status);

//load image
                if(!image.equals("default")){
                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).fit().centerCrop().placeholder(R.drawable.profile).into(circleImageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(image).fit().centerCrop().placeholder(R.drawable.profile).into(circleImageView);

                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                //  Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
        settings_status_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String status = settings_status.getText().toString();
                Intent i = new Intent(SettingActivity.this, StatusActivity.class);

                i.putExtra("status", status);
                startActivity(i);
            }
        });
        settings_image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingActivity.this);

                */
                Intent i=new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(i,"SELECT IMAGE"),GALLERY_PICK);


            }
        });
    }
        @Override
        public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if(requestCode==GALLERY_PICK && resultCode==RESULT_OK)
        {
            progressDialog=new ProgressDialog(SettingActivity.this);
            progressDialog.setTitle("UPLOADING IMAGE");
            progressDialog.setMessage("PLEASE WAIT");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            Uri imageUri=data.getData();

            CropImage.activity(imageUri).setAspectRatio(1,1)
                    .setMinCropWindowSize(500,500)
                    .start(this);

        }

            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    Uri resultUri = result.getUri();
                    final File thumbPath=new File(resultUri.getPath());
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
                    String CurrentUserId=mCurrentUser.getUid();
                    StorageReference filePath=mImagestorage.child("profile_images")
                            .child(CurrentUserId+".jpg");
                    final StorageReference thumbfilepath=mImagestorage.child("profile_images").child("Thumb")
                            .child(mCurrentUser.getUid()+".jpg");
                    final byte[] finalThumb_byte = thumb_byte;
                    filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                          if(task.isSuccessful())
                          {
                              final String download_url=task.getResult().getDownloadUrl().toString();
                              UploadTask uploadTask = thumbfilepath.putBytes(finalThumb_byte);
                                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                            if(task.isSuccessful()){
                                                final String thumb_download_url=task.getResult().getDownloadUrl().toString();
                                                Map update_hash_map=new HashMap();
                                                update_hash_map.put("image",download_url);
                                                update_hash_map.put("thumb_image",thumb_download_url);
                                                mdatabaseReference.updateChildren(update_hash_map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful())

                                                        {

                                                            Toast.makeText(SettingActivity.this, "WORKINg", Toast.LENGTH_SHORT).show();
                                                            progressDialog.dismiss();
                                                            if(!download_url.equals("default"))
                                                            {
                                                                Picasso.get().load(download_url).placeholder(R.drawable.profile).fit().centerCrop().into(circleImageView);

                                                            }
                                                        }                                  }
                                                });

                                            }
                                            else
                                            {
                                                Toast.makeText(SettingActivity.this, " ERROR not WORKINg", Toast.LENGTH_SHORT).show();
                                                progressDialog.dismiss();

                                            }
                                        }
                                    });
                             // Toast.makeText(SettingActivity.this, "WORKINg", Toast.LENGTH_SHORT).show();
                          }
                          else
                          {
                              Toast.makeText(SettingActivity.this, " ERROR not WORKINg", Toast.LENGTH_SHORT).show();
                              //Log.i("jj", "onComplete: ");
                              progressDialog.dismiss();
                          }
                        }
                    });

                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
            }
        }
    public static String random() {

        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(10);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }


}
