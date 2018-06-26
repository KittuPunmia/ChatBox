package com.kittu.chatboxfirebase;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StatusActivity extends AppCompatActivity {
    private android.support.v7.widget.Toolbar toolbar;
    private DatabaseReference mdatabaseReference;
    private FirebaseUser mCurrentUser;
//change sttaus
    private ProgressDialog progressDialog;

            @BindView(R.id.status_save_btn)
            Button status_save_btn;
            @BindView(R.id.status_input)
            TextInputLayout status_input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        //storage
        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        String CurrentUid =mCurrentUser.getUid();
        mdatabaseReference= FirebaseDatabase.getInstance().getReference().child("Users").child(CurrentUid);

        //backbutton on title bar
        ButterKnife.bind(this);
        toolbar=findViewById(R.id.toolbarid);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("CHAT BOX");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //old Value of status
        Intent i=getIntent();
       String oldStatus= i.getStringExtra("status");
        status_input.getEditText().setText(oldStatus);

        //change status
        status_save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog=new ProgressDialog(StatusActivity.this);
                progressDialog.setTitle("Saving Changes");
                progressDialog.setMessage("Please wait while we save changes");
                progressDialog.show();
                String status=status_input.getEditText().getText().toString();
                mdatabaseReference.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            progressDialog.dismiss();
                            Toast.makeText(StatusActivity.this, "Status Changed", Toast.LENGTH_SHORT).show();

                        }
                        else
                        {
                            Toast.makeText(StatusActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });

            }
        });

    }
}
