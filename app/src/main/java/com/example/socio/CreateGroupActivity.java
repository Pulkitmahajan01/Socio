package com.example.socio;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class CreateGroupActivity extends AppCompatActivity {

    private ActionBar actionBar;

    private FirebaseAuth firebaseAuth;

    ImageView groupIconIv;
    EditText groupTitleEt,groupDescriptionEt;
    FloatingActionButton creategroupBtn;

    private static final int REQUEST_CAMERA_CODE=100;
    private static final int STORAGE_REQUEST_CODE=200;
    private static final int  IMAGE_PICK_GALLERY_REQUEST_CODE=300;
    private static final int  IMAGE_PICK_CAMERA_REQUEST_CODE=400;

    String cameraPermissons[];
    String storagePermissions[];

    Uri image_uri;

    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        actionBar=getSupportActionBar();
        actionBar.setTitle("Create Group");

        groupIconIv=findViewById(R.id.groupIconIv);
        groupTitleEt=findViewById(R.id.groupTitleEt);
        groupDescriptionEt=findViewById(R.id.groupDescriptionEt);
        creategroupBtn=findViewById(R.id.creategroupBtn);

        cameraPermissons=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth=FirebaseAuth.getInstance();
        checkuser();

        groupIconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImgPicDialog();
            }
        });

        creategroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startCreatingGroup();
            }
        });
    }

    private void startCreatingGroup() {
        pd=new ProgressDialog(this);
        pd.setMessage("Creating Group");

        String groupTitle=groupTitleEt.getText().toString().trim();
        String groupDescription=groupDescriptionEt.getText().toString().trim();
        if (TextUtils.isEmpty(groupTitle)){
            Toast.makeText(this,"Please enter group title",Toast.LENGTH_SHORT).show();
            return;
        }
        pd.show();

        String g_timestamp=""+System.currentTimeMillis();

        if (image_uri==null){
            createGroup(""+g_timestamp,""+groupTitle,""+groupDescription,"");
        }
        else {

            String FilePathAndName="GroupImgs/"+"image"+g_timestamp;

            StorageReference storageReference= FirebaseStorage.getInstance().getReference(FilePathAndName);
            storageReference.putFile(image_uri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            Uri downloadUrl=task.getResult();

                            createGroup(""+g_timestamp,""+groupTitle,""+groupDescription,""+downloadUrl);

                        }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(CreateGroupActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });;
        }
    }

    private void createGroup(String g_timestamp,String groupTitle,String groupDescription,String groupIcon) {
        HashMap<String,String> hashMap=new HashMap<>();
        hashMap.put("groupId",""+g_timestamp);
        hashMap.put("groupTitle",""+groupTitle);
        hashMap.put("groupDescription",""+groupDescription);
        hashMap.put("groupIcon",""+groupIcon);
        hashMap.put("timestamp",""+g_timestamp);
        hashMap.put("CreatedBy",""+firebaseAuth.getUid());

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(g_timestamp).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                HashMap<String,String>hashMap1=new HashMap<>();
                hashMap1.put("uid",firebaseAuth.getUid());
                hashMap1.put("role","creator");
                hashMap1.put("timestamp",g_timestamp);

                DatabaseReference reference1=FirebaseDatabase.getInstance().getReference("Groups");
                reference1.child(g_timestamp).child("Participants").child(firebaseAuth.getUid())
                        .setValue(hashMap1).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        pd.dismiss();
                        Toast.makeText(CreateGroupActivity.this,"Group created successfully!",Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(CreateGroupActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(CreateGroupActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkCameraPermissions(){
        boolean result= ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==
                (PackageManager.PERMISSION_GRANTED);
        boolean result1= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermissions(){
        requestPermissions(cameraPermissons,REQUEST_CAMERA_CODE);
    }

    private boolean checkStoragePermissions(){
        boolean result= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                (PackageManager.PERMISSION_DENIED);

        return result;
    }

    private void requestStoragePermissions(){
        requestPermissions(storagePermissions,STORAGE_REQUEST_CODE);
    }

    private void PickFromGallery() {
        Intent galleryIntent=new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,IMAGE_PICK_GALLERY_REQUEST_CODE);
    }

    private void PickFromCamera() {
        ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Group Image Icon title");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Group Image Icon Description");

        image_uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent cameraIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_REQUEST_CODE);
    }

    private void showImgPicDialog() {
        String options[]={"Camera","Gallery"};

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Import Image From");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which==0){
                    //OpenCamera
                    if (!checkCameraPermissions()){
                        requestCameraPermissions();
                    }
                    else {
                        PickFromCamera();
                    }

                }
                else if(which==1){
                    //OpenGallery
                    if (!checkStoragePermissions()){
                        requestStoragePermissions();
                    }
                    else {
                        PickFromGallery();
                    }
                }

            }
        });

        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case REQUEST_CAMERA_CODE:{
                if (grantResults.length>0){
                    boolean cameraAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted=grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted){
                        PickFromCamera();
                    }
                    else{
                        Toast.makeText(this,"Please enable camera and storage permissions",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean writeStorageAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted){
                        PickFromGallery();
                    }
                    else{
                        Toast.makeText(this,"Please enable storage permissions",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode==RESULT_OK ){
            if (requestCode==IMAGE_PICK_GALLERY_REQUEST_CODE){
                image_uri=data.getData();

                groupIconIv.setImageURI(image_uri);
            }
            else if (requestCode==IMAGE_PICK_CAMERA_REQUEST_CODE){
                groupIconIv.setImageURI(image_uri);
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void checkuser() {
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if (user!=null){
            actionBar.setSubtitle(user.getEmail());
        }
    }
}