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
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class AddPostsActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    DatabaseReference dbRef,databaseReference;

    ActionBar actionBar;

    EditText titleEt,descriptionEt;
    ImageView imageIv;
    Button uploadBtn;
    ImageButton btnAdd;

    String name,email,uid,dp;

    String editTitle,editDescription,editImage;

    Uri image_uri=null;

    ProgressDialog pd;

    private static final int CAMERA_REQUEST_CODE=100;
    private static final int STORAGE_REQUEST_CODE=200;
    private static final int  IMAGE_PICK_GALLERY_REQUEST_CODE=300;
    private static final int  IMAGE_PICK_CAMERA_REQUEST_CODE=400;

    String cameraPermissons[];
    String storagePermissions[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_posts);

        actionBar=getSupportActionBar();
        actionBar.setTitle("Add Post");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        cameraPermissons=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        titleEt=findViewById(R.id.pTitleEt);
       descriptionEt=findViewById(R.id.pCaptionEt);
        imageIv=findViewById(R.id.pImageIv);
       uploadBtn =findViewById(R.id.pUploadbtn);
       btnAdd=findViewById(R.id.btnadd);

       pd=new ProgressDialog(this);

        firebaseAuth=FirebaseAuth.getInstance();
        checkuserStatus();

        Intent intent=getIntent();

        String action=intent.getAction();
        String type=intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type!=null){
            if ("text/plain".equals(type)){
                handleSendText(intent);
            }
            else if (type.startsWith("image")){
                handleSendImage(intent);
            }
        }

        String isUpdatekey=""+intent.getStringExtra("key");
        String editPostId=""+intent.getStringExtra("editPostId");

        if (isUpdatekey.equals("editPost")){
            //editpost
            actionBar.setTitle("Edit Post");
            uploadBtn.setText("Update");
            loadPostData(editPostId);

        }else{
            //add new Post
            actionBar.setTitle("Add new Post");
            uploadBtn.setText("Upload");
        }

        actionBar.setSubtitle(email);

        dbRef= FirebaseDatabase.getInstance().getReference("Users");
        Query query=dbRef.orderByChild("email").equalTo(email);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    name=""+ds.child("name").getValue();
                    email=""+ds.child("email").getValue();
                    dp=""+ds.child("image").getValue();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title=titleEt.getText().toString().trim();
                String description=descriptionEt.getText().toString().trim();

                if (TextUtils.isEmpty(title)){
                    Toast.makeText(AddPostsActivity.this,"Enter Title",Toast.LENGTH_SHORT).show();
                    return;
                }
                 if (TextUtils.isEmpty(description)){
                    Toast.makeText(AddPostsActivity.this,"Enter description",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (isUpdatekey.equals("editPost")){
                    beginUpdate(title,description,editPostId);

                }else{

                    uploadData(title,description);

                }
                /* if (image_uri==null){
                    uploadData(title,description,"noImage");
                }
                 else{
                     uploadData(title,description,String.valueOf(image_uri));
                }*/

            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showimagePickDialog();
            }
        });
    }

    private void handleSendImage(Intent intent) {
        Uri imageUri=(Uri)intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri!=null){
            image_uri=imageUri;
            imageIv.setImageURI(image_uri);
        }
    }

    private void handleSendText(Intent intent) {
        String sharedText=intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText!=null){
            descriptionEt.setText(sharedText);
        }
    }

    private void beginUpdate(String title, String description, String editPostId) {
        pd.setMessage("Updating Post...");
        pd.show();
     /*   if (!editImage.equals("noImage")){
            updatingWithImage(title,description,editPostId);
        }*/
         if(imageIv.getDrawable()!=null) {
           // updatingWithNowImage(title,description,editPostId);
             updatingWithImage(title,description,editPostId);

         }
        else {
            updateWithoutImage(title,description,editPostId);
        }
    }

    private void updateWithoutImage(String title, String description, String editPostId) {
        HashMap<String,Object> results=new HashMap<>();
        results.put("uid",uid);
        results.put("uName",name);
        results.put("uEmail",email);
        results.put("uDp",dp);
       // results.put("pId",timestamp);
        results.put("pTitle",title);
        results.put("pDescr",description);
        results.put("pImage","noImage");
       // results.put("ptime",timestamp);

        databaseReference=   FirebaseDatabase.getInstance().getReference("Posts");

        databaseReference.child(editPostId).updateChildren(results).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(AddPostsActivity.this,"Updated!",Toast.LENGTH_SHORT).show();
                    titleEt.setText("");
                    descriptionEt.setText("");
                    imageIv.setImageURI(null);
                    image_uri=null;
                    pd.dismiss();
                }
                else {
                    Toast.makeText(AddPostsActivity.this,"Failed!",Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    private void updatingWithNowImage(String title, String description, String editPostId) {
        String timestamp= String.valueOf(System.currentTimeMillis());
        String fileAndPathName="Posts/"+"post_"+timestamp;

if (imageIv.getDrawable()!=null) {
    Bitmap bitmap = ((BitmapDrawable) imageIv.getDrawable()).getBitmap();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
    byte[] data = baos.toByteArray();
    StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileAndPathName);
    ref.putBytes(data).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
        @Override
        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return ref.getDownloadUrl();
        }
    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
        @Override
        public void onComplete(@NonNull Task<Uri> task) {
            if (task.isSuccessful()) {
                Uri downloadUrl = task.getResult();
                String nUri = downloadUrl.toString();

                HashMap<String, Object> results = new HashMap<>();
                results.put("uid", uid);
                results.put("uName", name);
                results.put("uEmail", email);
                results.put("uDp", dp);
                // results.put("pId",timestamp);
                results.put("pTitle", title);
                results.put("pDescr", description);
                results.put("pImage", nUri);
                // results.put("ptime",timestamp);

                databaseReference = FirebaseDatabase.getInstance().getReference("Posts");

                databaseReference.child(editPostId).updateChildren(results);
                Toast.makeText(AddPostsActivity.this, "Updated!", Toast.LENGTH_SHORT).show();
                titleEt.setText("");
                descriptionEt.setText("");
                imageIv.setImageURI(null);
                image_uri = null;
                pd.dismiss();
            } else {
                Toast.makeText(AddPostsActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
            }
        }
    }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            pd.dismiss();
            Toast.makeText(AddPostsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

        }
    });

}
    }

    private void updatingWithImage(String title, String description, String editPostId) {
        //updating requires deleting of previous post
        StorageReference mPictureRef=FirebaseStorage.getInstance().getReferenceFromUrl(editImage);
        mPictureRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                String timestamp= String.valueOf(System.currentTimeMillis());
                String fileAndPathName="Posts/"+"post_"+timestamp;

                if (imageIv.getDrawable()!=null){

                    Bitmap bitmap=((BitmapDrawable)imageIv.getDrawable()).getBitmap();
                    ByteArrayOutputStream baos=new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
                    byte[] data=baos.toByteArray();
                    StorageReference ref= FirebaseStorage.getInstance().getReference().child(fileAndPathName);
                    ref.putBytes(data).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()){
                                throw task.getException();
                            }
                            return ref.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()){
                                Uri downloadUrl=task.getResult();
                                String nUri=downloadUrl.toString();

                                HashMap<String,Object> results=new HashMap<>();
                                results.put("uid",uid);
                                results.put("uName",name);
                                results.put("uEmail",email);
                                results.put("uDp",dp);
                              //  results.put("pId",timestamp);
                                results.put("pTitle",title);
                                results.put("pDescr",description);
                                results.put("pImage",nUri);
                               // results.put("ptime",timestamp);

                                databaseReference=   FirebaseDatabase.getInstance().getReference("Posts");

                                databaseReference.child(editPostId).updateChildren(results);
                                Toast.makeText(AddPostsActivity.this,"Updated!",Toast.LENGTH_SHORT).show();
                                titleEt.setText("");
                                descriptionEt.setText("");
                                imageIv.setImageURI(null);
                                image_uri=null;
                                pd.dismiss();
                            }
                            else {
                                Toast.makeText(AddPostsActivity.this,"Failed!",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(AddPostsActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddPostsActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }
        });
    }

    private void loadPostData(String editPostId) {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
        Query fQuery=ref.orderByChild("pId").equalTo(editPostId);
        fQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    editTitle=""+ds.child("pTitle").getValue();
                    editDescription=""+ds.child("pDescr").getValue();
                    editImage=""+ds.child("pImage").getValue();

                    titleEt.setText(editTitle);
                    descriptionEt.setText(editDescription);

                    if (!editImage.equals("noImage")){
                        try {
                            Picasso.get().load(editImage).into(imageIv);
                        }
                        catch (Exception e){

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void uploadData(String title, String description) {
    pd.setMessage("Publishing Post...");
    pd.show();

    String timestamp= String.valueOf(System.currentTimeMillis());

    String fileAndPathName="Posts/"+"post_"+timestamp;

    if (imageIv.getDrawable()!=null){

        Bitmap bitmap=((BitmapDrawable)imageIv.getDrawable()).getBitmap();
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[] data=baos.toByteArray();

        StorageReference ref= FirebaseStorage.getInstance().getReference().child(fileAndPathName);
        ref.putBytes(data).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()){
                    throw task.getException();
                }
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()){
                    Uri downloadUrl=task.getResult();
                    String nUri=downloadUrl.toString();

                    HashMap<String,Object> results=new HashMap<>();
                    results.put("uid",uid);
                    results.put("uName",name);
                    results.put("uEmail",email);
                    results.put("uDp",dp);
                    results.put("pId",timestamp);
                    results.put("pTitle",title);
                    results.put("pDescr",description);
                    results.put("pImage",nUri);
                    results.put("ptime",timestamp);
                    results.put("pLikes","0");
                    results.put("pComments","0");

                    databaseReference=   FirebaseDatabase.getInstance().getReference("Posts");

                    databaseReference.child(timestamp).updateChildren(results);
                    Toast.makeText(AddPostsActivity.this,"Post Published!",Toast.LENGTH_SHORT).show();
                    titleEt.setText("");
                    descriptionEt.setText("");
                    imageIv.setImageURI(null);
                    image_uri=null;

                    prepareNotifications(
                            ""+timestamp,
                            ""+name+" added a new Post.",
                            ""+title+"\n"+description,
                            "PostNotification",
                            "POST"
                    );

                    pd.dismiss();
                }
                else {
                    Toast.makeText(AddPostsActivity.this,"Failed!",Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(AddPostsActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });

    }else {
        HashMap<String,Object> results=new HashMap<>();
        results.put("uid",uid);
        results.put("uName",name);
        results.put("uEmail",email);
        results.put("uDp",dp);
        results.put("pId",timestamp);
        results.put("pTitle",title);
        results.put("pDescr",description);
        results.put("pImage","noImage");
        results.put("ptime",timestamp);
        results.put("pLikes","0");
        results.put("pComments","0");

        databaseReference=   FirebaseDatabase.getInstance().getReference("Posts");

        databaseReference.child(timestamp).updateChildren(results).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(AddPostsActivity.this,"Post Published!",Toast.LENGTH_SHORT).show();
                    titleEt.setText("");
                    descriptionEt.setText("");
                    imageIv.setImageURI(null);
                    image_uri=null;

                    prepareNotifications(
                            ""+timestamp,
                            ""+name+" added a new Post.",
                            ""+title+"\n"+description,
                            "PostNotification",
                            "POST"
                    );

                    pd.dismiss();
                }
                else {
                    Toast.makeText(AddPostsActivity.this,"Failed!",Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    }

    private void showimagePickDialog() {
        String[] options={"Camera","Gallery"};
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Choose Image From");

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

    private boolean checkStoragePermissions(){
        boolean result= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                (PackageManager.PERMISSION_DENIED);
        return result;
    }

    private void requestStoragePermissions(){
        requestPermissions(storagePermissions,STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermissions(){
        boolean result= ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==
                (PackageManager.PERMISSION_GRANTED);
        boolean result1= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermissions(){
        requestPermissions(cameraPermissons,CAMERA_REQUEST_CODE);
    }

    private void PickFromGallery() {
        Intent galleryIntent=new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,IMAGE_PICK_GALLERY_REQUEST_CODE);
    }

    private void PickFromCamera() {
        ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Temp pick");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Temp Descr");

        image_uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent cameraIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
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

                imageIv.setImageURI(image_uri);
            }
            else if (requestCode==IMAGE_PICK_CAMERA_REQUEST_CODE){
                imageIv.setImageURI(image_uri);
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

private void prepareNotifications(String pId,String title,String description,String notificationType,String notificationTopic){
        String NOTIFICATION_TOPIC="/topics/"+notificationTopic;
        String NOTIFICATION_TITLE=title;
        String NOTIFICATION_MSG=description;
        String NOTIFICATON_TYPE=notificationType;

    JSONObject  notificationJO=new JSONObject();
    JSONObject notificationBodyJO=new JSONObject();
    try {
        notificationBodyJO.put("notificationType",NOTIFICATON_TYPE);
        notificationBodyJO.put("sender",uid);
        notificationBodyJO.put("pId",pId);
        notificationBodyJO.put("pTitle",NOTIFICATION_TITLE);
        notificationBodyJO.put("pDescription",NOTIFICATION_MSG);

        notificationJO.put("to",NOTIFICATION_TOPIC);
        notificationJO.put("data",notificationBodyJO);
    } catch (JSONException e) {
        Toast.makeText(this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
    }
    sendPostNotificaton(notificationJO);
}

    private void sendPostNotificaton(JSONObject notificationJO) {
        JsonObjectRequest objectRequest=new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJO,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("FCM_RESPONSE","onResponse: "+response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(AddPostsActivity.this,error.toString(),Toast.LENGTH_SHORT).show();
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers=new HashMap<>();
                headers.put("Content-Type","application/json");
                headers.put("Authorization", "key=AAAAxHGN8o8:APA91bHFqzHpNMpwwFGq01nSspg4xhzaPbWxd5Vk30fPQ2y8C1l4fyiQKCXMpvExCk-CgCaWj6n3DCNTUrhL5IZNvbPBDJ6sUGFmoSBluKayExHWpYiAy43KGhAeTbbQriSzx0fEXvUG");
                return headers;
            }
        };
        Volley.newRequestQueue(this).add(objectRequest);
    }


    @Override
    protected void onStart() {
        super.onStart();
        checkuserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkuserStatus();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_group_info).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==R.id.action_logout){
            firebaseAuth.signOut();
            checkuserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
    private void checkuserStatus(){
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if(user!=null){
            email=user.getEmail();
            uid=user.getUid();

        }
        else{
            startActivity(new Intent(this,loginregister.class));
            finish();
        }
    }
}