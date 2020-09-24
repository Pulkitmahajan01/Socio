package com.example.socio.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socio.Chats;
import com.example.socio.R;
import com.example.socio.TheirProfileActivity;
import com.example.socio.models.ModelUsers;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class AdapterUser extends RecyclerView.Adapter<AdapterUser.MyHolder> {

    Context context;
    List<ModelUsers> usersList;

    FirebaseAuth firebaseAuth;
    String myUid;

    public AdapterUser(Context context, List<ModelUsers> usersList) {
        this.context = context;
        this.usersList = usersList;

        firebaseAuth=FirebaseAuth.getInstance();
        myUid=firebaseAuth.getUid();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(context).inflate(R.layout.row_users,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        final String hisUid=usersList.get(position).getUid();
        String UserImage=usersList.get(position).getImage();
        String UserName=usersList.get(position).getName();
        final String UserEmail=usersList.get(position).getEmail();

       holder.mNameTv.setText(UserName);
       holder.mEmailTv.setText(UserEmail);

       try {
           Picasso.get().load(UserImage).placeholder(R.drawable.ic_default_img_black).into(holder.mAvatarIv);
       }
       catch (Exception e){

       }

       holder.blockIv.setImageResource(R.drawable.ic_unblocked_green);
       checkIsBlocked(hisUid,holder,position);

       holder.itemView.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

               AlertDialog.Builder builder=new AlertDialog.Builder(context);
               builder.setItems(new String[]{"Profile", "Chat"}, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {

                       if (which==0){
                           //view Profile
                           Intent intent=new Intent(context, TheirProfileActivity.class);
                           intent.putExtra("uid",hisUid);
                           context.startActivity(intent);
                       }
                       else if(which==1){
                                isBlockedOrNot(hisUid);
                       }
                   }
               });
               builder.create().show();
           }
       });

       holder.blockIv.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if (usersList.get(position).isBlocked()){
                   unblockUser(hisUid);
               }
               else {
                    blockUser(hisUid);
               }

           }
       });

    }

    private void isBlockedOrNot(String hisUid){
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUid).child("BlockedUsers").orderByChild("uid").equalTo(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds:dataSnapshot.getChildren()){
                            if (ds.exists()){
                                Toast.makeText(context,"Messages could not be sent to this chat",Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        Intent intent=new Intent(context, Chats.class);
                        intent.putExtra("hisUid",hisUid);
                        context.startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void checkIsBlocked(String hisUid, MyHolder holder, int position) {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds:dataSnapshot.getChildren()){
                            if (ds.exists()){
                                holder.blockIv.setImageResource(R.drawable.ic_blocked_red);
                                usersList.get(position).setBlocked(true);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void blockUser(String hisUid) {
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("uid",hisUid);

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").child(hisUid).setValue(hashMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(context,"Blocked successfully!",Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context,"Failed: "+e.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void unblockUser(String hisUid) {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid).child("BlockedUsers").orderByChild("uid").equalTo(hisUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds:dataSnapshot.getChildren()){
                            if (ds.exists()){
                                ds.getRef().removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(context,"User unblocked!",Toast.LENGTH_SHORT).show();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context,"Failed: "+e.getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {

    ImageView mAvatarIv,blockIv;
    TextView mEmailTv,mNameTv;

    public MyHolder(@NonNull View itemView) {
        super(itemView);

        mAvatarIv=itemView.findViewById(R.id.avatarIv);
        mEmailTv=itemView.findViewById(R.id.EmailTv);
        mNameTv=itemView.findViewById(R.id.NameTv);
        blockIv=itemView.findViewById(R.id.blockIv);
    }
}

}
