package com.kittu.chatboxfirebase;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class RegisterActivity extends AppCompatActivity {
    @BindView(R.id.etEmail)
    EditText etEmail;
    @BindView(R.id.etPassword)
    EditText etPassword;
    @BindView(R.id.etName)
    EditText etName;
    @BindView(R.id.btnRegister)
    Button btnRegister;

    @BindView(R.id.btnLogin)
    Button btnLogin;
    private FirebaseAuth mAuth;
    String TAG = "MainActivity";
    private ProgressDialog progressDialog;

     private android.support.v7.widget.Toolbar toolbar;

     //storage

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

       // backbutton on title bar

        toolbar=findViewById(R.id.toolbarid);
      setSupportActionBar(toolbar);
      getSupportActionBar().setTitle("CHAT BOX");
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }
    private void sendEmailVerification(final String name) {
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            String uid=user.getUid();
                            Log.d(TAG, "MainActivityEmail:success" + user.getEmail());
                            Toast.makeText(RegisterActivity.this,
                                    "Verification email sent to " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                            //storage
                            DatabaseReference myRef = FirebaseDatabase.getInstance()
                                    .getReference().
                                            child("Users").child(uid);
                            HashMap<String,String> usermap=new HashMap<>();
                            usermap.put("name",name);
                            usermap.put("status","Hey There!");
                            usermap.put("image","default");
                            usermap.put("thumb_image","default");
                            myRef.setValue(usermap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText(RegisterActivity.this, "Data saved", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                   e.printStackTrace();                                }
                            });
                            FirebaseAuth.getInstance().signOut();
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText(RegisterActivity.this,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });

    }


    @OnClick({R.id.btnRegister, R.id.btnLogin})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnRegister:
                final String name= etName.getText().toString().trim();

                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    etName.setError("Please enter email here");
                    etName.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    etEmail.setError("Please enter email here");
                    etEmail.requestFocus();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    etPassword.setError("Please enter Password here");
                    etPassword.requestFocus();
                    return;
                }
                progressDialog.setMessage("Registering the user..");
                progressDialog.show();


                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(RegisterActivity.this, "You are registered", Toast.LENGTH_SHORT).show();
                                    sendEmailVerification(name);
                                    progressDialog.cancel();


                                    mAuth.signOut();
                                } else {
                                    progressDialog.cancel();
                                    Toast.makeText(RegisterActivity.this, "Could not register please try again", Toast.LENGTH_SHORT).show();


                                }

                            }
                        });
                break;
            case R.id.btnLogin:finish();
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
