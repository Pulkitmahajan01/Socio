package com.example.socio;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socio.adapters.AdapterComments;
import com.example.socio.models.ModelComments;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.O)
public class PostDetailActivity extends AppCompatActivity {

    String myUid,myName,myEmail,myDp,postId,hisName,pLikes,hisDp,hisUid,pImage;

    ImageView uPictureIv,pImageIv;
    TextView nameTv,pTimeTv,pTitleTv,pDescTv,pLikesTv,pCommentstv;
    ImageButton moreBtn;
    Button LikeBtn,ShareBtn;
    LinearLayout profileLayout;

    EditText commentEt;
    ImageButton sendBtn;
    ImageView cAvatarIv;

    ActionBar actionBar;
    ProgressDialog pd;

    RecyclerView recyclerView;

    List<ModelComments>commentsList;
    AdapterComments adapterComments;

    boolean mProcessComment=false;
    boolean mProcessLikes=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        actionBar=getSupportActionBar();
        actionBar.setTitle("Comments");

        Intent intent=getIntent();
        postId=intent.getStringExtra("postId");

        uPictureIv=findViewById(R.id.uProfileIv);
        pImageIv=findViewById(R.id.pImageIview);
        nameTv=findViewById(R.id.uNameTv);
        pTimeTv=findViewById(R.id.uTimeTv);
        pTitleTv=findViewById(R.id.pTitleTv);
        pDescTv=findViewById(R.id.pDescriptionTv);
        pLikesTv=findViewById(R.id.pLikesTv);
        pCommentstv=findViewById(R.id.pCommentsTv);
        moreBtn=findViewById(R.id.morebtn);
        LikeBtn=findViewById(R.id.likeBtn);
        ShareBtn=findViewById(R.id.ShareBtn);
        commentEt=findViewById(R.id.commentEt);
        profileLayout=findViewById(R.id.profileLayout);
        sendBtn=findViewById(R.id.sendBtn);
        cAvatarIv=findViewById(R.id.cAvtarIv);
        recyclerView=findViewById(R.id.recyclerView);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });

        LikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost();
            }
        });

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions();
            }
        });

        ShareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pTitle=pTitleTv.getText().toString().trim();
                String pDescr=pDescTv.getText().toString().trim();
                BitmapDrawable bitmapDrawable=(BitmapDrawable) pImageIv.getDrawable();
                if (bitmapDrawable==null){
                    shareTextOnly(pTitle,pDescr);
                }
                else {
                    Bitmap bitmap=bitmapDrawable.getBitmap();
                    shareImageANdText(pTitle,pDescr,bitmap);
                }
            }
        });

        pLikesTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(PostDetailActivity.this, PostLikedByActivity.class);
                intent.putExtra("postId",postId);
                startActivity(intent);
            }
        });

        loadPostInfo();
        checkuserStatus();
        loadUserInfo();
        setLikes();
        loadComments();
    }

    private void addToListNotification(String hisUid,String pId,String notification){
        String timestamp=""+System.currentTimeMillis();

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("pId",pId);
        hashMap.put("timestamp",timestamp);
        hashMap.put("pUid",hisUid);
        hashMap.put("notificcation",notification);
        hashMap.put("sUid",myUid);

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUid).child("Notifications").child(timestamp).setValue(hashMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }


    private void shareImageANdText(String pTitle, String pDescr, Bitmap bitmap) {
        String shareBody=pTitle+"\n"+pDescr;
        Uri uri=saveImageToShare(bitmap);
        Intent sIntent=new Intent(Intent.ACTION_SEND);
        sIntent.putExtra(Intent.EXTRA_STREAM,uri);
        sIntent.putExtra(Intent.EXTRA_SUBJECT,"Subject here");
        sIntent.putExtra(Intent.EXTRA_TEXT,shareBody);
        sIntent.setType("image/png");
        startActivity(Intent.createChooser(sIntent,"Share via"));
    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder=new File(getCacheDir(),"images");
        Uri uri=null;
        try {
            imageFolder.mkdirs();
            File file=new File(imageFolder,"shared_images.png");
            FileOutputStream fileOutputStream=new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG,90,fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            uri= FileProvider.getUriForFile(this,"com.example.socio.fileprovider",file);

        }catch (Exception e){
            Toast.makeText(this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
        }
        return uri;
    }

    private void shareTextOnly(String pTitle, String pDescr) {
        String shareBody=pTitle+"\n"+pDescr;
        Intent sIntent=new Intent(Intent.ACTION_SEND);
        sIntent.putExtra(Intent.EXTRA_SUBJECT,"Subject here");
        sIntent.putExtra(Intent.EXTRA_TEXT,shareBody);
        startActivity(Intent.createChooser(sIntent,"Share via"));
    }

    private void loadComments() {
        LinearLayoutManager layoutManager=new LinearLayoutManager(getApplicationContext());
         recyclerView.setLayoutManager(layoutManager);

         commentsList=new ArrayList<>();

         DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
         ref.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 commentsList.clear();
                 for (DataSnapshot ds:dataSnapshot.getChildren()){
                     ModelComments modelComments=ds.getValue(ModelComments.class);
                     commentsList.add(modelComments);

                     adapterComments=new AdapterComments(getApplicationContext(),commentsList,myUid,postId);
                     recyclerView.setAdapter(adapterComments);
                 }
             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {

             }
         });
    }

    private void showMoreOptions() {
        PopupMenu popupMenu=new PopupMenu(this,moreBtn, Gravity.END);

        if (hisUid.equals(myUid)){
            popupMenu.getMenu().add(Menu.NONE,0,0,"Delete");
            popupMenu.getMenu().add(Menu.NONE,1,0,"Edit");
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId()==0){
                    beginDelete();
                }
                else if (item.getItemId()==1){
                    Intent intent =new Intent(PostDetailActivity.this, AddPostsActivity.class);
                    intent.putExtra("key","editPost");
                    intent.putExtra("editPostId","pId");
                    startActivity(intent);
                }

                return false;
            }
        });
        popupMenu.show();

    }

    private void beginDelete() {
        if (pImage.equals("noImage")){
            deleteWithoutImage();
        }
        else {
            deleteWithImage();
        }
    }

    private void deleteWithImage() {
        ProgressDialog pd=new ProgressDialog(this);
        pd.setMessage("Deleting Post...");

        StorageReference picRef= FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Query fquery= FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
                fquery .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds:dataSnapshot.getChildren()){
                            ds.getRef().removeValue();
                        }
                        Toast.makeText(PostDetailActivity.this,"Deleted Successfully",Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(PostDetailActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void deleteWithoutImage() {
        ProgressDialog pd=new ProgressDialog(this);
        pd.setMessage("Deleting Post...");

        Query fquery= FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
        fquery .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    ds.getRef().removeValue();
                }
                Toast.makeText(PostDetailActivity.this,"Deleted Successfully",Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setLikes() {
        DatabaseReference likesRef=FirebaseDatabase.getInstance().getReference().child("Likes");
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(postId).hasChild(myUid)){
                    LikeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked,0,0,0);
                    LikeBtn.setText("Liked");
                }
                else {
                    LikeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black,0,0,0);
                    LikeBtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void likePost() {
        mProcessLikes=true;
        DatabaseReference likesRef=FirebaseDatabase.getInstance().getReference().child("Likes");
        DatabaseReference postsRef=FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (mProcessLikes){
                    if (dataSnapshot.child(postId).hasChild(myUid)){
                        postsRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)-1));
                        likesRef.child(postId).child(myUid).removeValue();
                        mProcessLikes=false;


                    }
                    else {
                        postsRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)+1));
                        likesRef.child(postId).child(myUid).setValue("Liked");
                        mProcessLikes=false;

                        addToListNotification(""+hisUid,""+postId,"Liked your post");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void postComment() {
        pd=new ProgressDialog(this);
        pd.setMessage("Posting Comment...");

        String comment=commentEt.getText().toString().trim();
        String timeStamp= String.valueOf(System.currentTimeMillis());

        if (TextUtils.isEmpty(comment)){
            Toast.makeText(this,"Please Type something",Toast.LENGTH_SHORT).show();
            return;
        }
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("cId",timeStamp);
        hashMap.put("comment",comment);
        hashMap.put("timestamp",timeStamp);
        hashMap.put("uid",myUid);
        hashMap.put("uEmail",myEmail);
        hashMap.put("uDp",myDp);
        hashMap.put("uName",myName);

        ref.child(timeStamp).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                pd.dismiss();
                Toast.makeText(PostDetailActivity.this,"Comment Added",Toast.LENGTH_SHORT).show();
                commentEt.setText("");
                updateCommentCount();
                addToListNotification(""+hisUid,""+postId,"Commented on your post");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(PostDetailActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updateCommentCount() {
        mProcessComment=true;
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (mProcessComment){
                    String comments=""+dataSnapshot.child("pComments").getValue();
                    int newCommentVal=Integer.parseInt(comments)+1;
                    ref.child("pComments").setValue(""+newCommentVal);
                    mProcessComment=false;

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadUserInfo() {
        Query myRef=FirebaseDatabase.getInstance().getReference("Users");
        myRef.orderByChild("uid").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    myName=""+ds.child("name").getValue();
                    myDp=""+ds.child("image").getValue();

                    try {
                        Picasso.get().load(myDp).placeholder(R.drawable.ic_default_img_black).into(cAvatarIv);
                    }
                    catch (Exception e){
                        Picasso.get().load(R.drawable.ic_default_img_black).into(cAvatarIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadPostInfo() {
        DatabaseReference dref= FirebaseDatabase.getInstance().getReference("Posts");
        Query query=dref.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    String pTitle=""+ds.child("pTitle").getValue();
                    String pDescr=""+ds.child("pDescr").getValue();
                    pLikes=""+ds.child("pLikes").getValue();
                    String pTimestamp=""+ds.child("ptime").getValue();
                     pImage=""+ds.child("pImage").getValue();
                    hisDp =""+ds.child("uDp").getValue();
                     hisUid=""+ds.child("uid").getValue();
                    String uEmail=""+ds.child("uEmail").getValue();
                    hisName=""+ds.child("uName").getValue();
                    String commentsCount=""+ds.child("pComments").getValue();

                    Instant inst;
                    if (pTimestamp == null) {
                        inst = Instant.now();
                    } else {
                         inst = Instant.from(timestampFormatter.parse(pTimestamp));
                    }
                    String pTime = inst.atZone(ZoneId.systemDefault()).format(dtf);

                    pTitleTv.setText(pTitle);
                    pDescTv.setText(pDescr);
                    pTimeTv.setText(pTime);
                    pLikesTv.setText(pLikes+" Likes");
                    pCommentstv.setText(commentsCount+" Comments" +
                            "");

                    nameTv.setText(hisName);
                    if(pImage.equals("noImage")){
                        pImageIv.setVisibility(View.GONE);
                    }
                    else{
                        pImageIv.setVisibility(View.VISIBLE);
                        try {
                            Picasso.get().load(pImage).into(pImageIv);
                        }
                        catch (Exception e){

                        }
                    }

                    try {
                        Picasso.get().load(hisDp).placeholder(R.drawable.ic_default_img_black).into(uPictureIv);
                    }
                    catch (Exception e){
                        Picasso.get().load(R.drawable.ic_default_img_black).into(uPictureIv);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkuserStatus(){
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            myEmail=user.getEmail();
            myUid=user.getUid();

        }
        else{
            startActivity(new Intent(this,loginregister.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_group_info).setVisible(false);
        menu.findItem(R.id.action_settings).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==R.id.action_logout){
            FirebaseAuth.getInstance().signOut();
            checkuserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

    private static DateTimeFormatter timestampFormatter = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.INSTANT_SECONDS)
            .appendValue(ChronoField.MILLI_OF_SECOND, 3)
            .toFormatter();

    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
}