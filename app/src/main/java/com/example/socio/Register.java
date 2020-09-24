package com.example.socio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Register extends AppCompatActivity {

    TextInputLayout Nemail,Npassword;
    Button btnReg,btnLog;
    ProgressDialog progressDialog;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

      Nemail=findViewById(R.id.signup_email);
        Npassword=findViewById(R.id.signup_password);
        btnReg=findViewById(R.id.btnReg);
        btnLog=findViewById(R.id.tvLog);

        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Creating Your Account...");

        mAuth = FirebaseAuth.getInstance();

        btnLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this,loginregister.class));
                finish();
            }
        });

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=Nemail.getEditText().getText().toString().trim();
                String password=Npassword.getEditText().getText().toString().trim();

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    Nemail.setError("Invalid Email");
                    Nemail.setFocusable(true);
                }
                else if(password.length()<6){
                    Npassword.setError("Password must contain atleast 6 characters");
                    Npassword.setFocusable(true);
                }
                else{
                    registerUser(email,password);
                }
            }
        });
    }

    private void registerUser(String email, String password) {
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            progressDialog.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();
                           String email=user.getEmail();
                           String uid=user.getUid();

                           user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                               @Override
                               public void onComplete(@NonNull Task<Void> task) {
                                 if (task.isSuccessful()){
                                     HashMap<Object,String> hashMap=new HashMap<>();
                                     hashMap.put("email",email);
                                     hashMap.put("uid",uid);
                                     hashMap.put("name","");
                                     hashMap.put("onlineStatus","online");
                                     hashMap.put("typingTo","noOne");
                                     hashMap.put("phone","");
                                     hashMap.put("image","");
                                     hashMap.put("cover","");

                                     FirebaseDatabase database=FirebaseDatabase.getInstance();
                                     DatabaseReference reference=database.getReference("Users");
                                     reference.child(uid).setValue(hashMap);

                                     Toast.makeText(Register.this,"Registered Successfully. Check your email for verification.\n"+user.getEmail(),Toast.LENGTH_LONG).show();
                                     startActivity(new Intent(Register.this, loginregister.class));
                                     finish();
                                 }
                                 else {
                                     Toast.makeText(Register.this,""+task.getException().getMessage(),Toast.LENGTH_LONG).show();

                                 }
                               }
                           });


                        } else {
                            // If sign in fails, display a message to the user.
                            progressDialog.dismiss();
                            Toast.makeText(Register.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }// ...
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(Register.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}