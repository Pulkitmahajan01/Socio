package com.example.socio.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socio.R;
import com.example.socio.models.ModelUsers;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdapterAddParticipants extends RecyclerView.Adapter<AdapterAddParticipants.HolderAddParticipants>{

    Context context;
    ArrayList<ModelUsers> usersList;
    String groupId,myGroupRole;

    public AdapterAddParticipants(Context context, ArrayList<ModelUsers> usersList, String groupId, String myGroupRole) {
        this.context = context;
        this.usersList = usersList;
        this.groupId = groupId;
        this.myGroupRole = myGroupRole;
    }

    @NonNull
    @Override
    public HolderAddParticipants onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_add_participants,parent,false);
        return new HolderAddParticipants(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderAddParticipants holder, int position) {
        ModelUsers users=usersList.get(position);
        String name=users.getName();
        String email=users.getEmail();
        String image=users.getImage();
        String uid=users.getUid();

       holder.mNameTv.setText(name);
       holder.mEmailTv.setText(email);

        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_default_img_black).into(holder.mAvatarIv);
        }
        catch (Exception e){

        }
        
        checkIfAlreadyExists(users,holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
                ref.child(groupId).child("Participants").child(uid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    String previousRole=""+dataSnapshot.child("role").getValue();
                                     myGroupRole=""+dataSnapshot.child("role").getValue();

                                    String[] options;

                                    AlertDialog.Builder builder=new AlertDialog.Builder(context);
                                    builder.setTitle("Choose Option");
                                    if (myGroupRole.equals("creator")){
                                        if (previousRole.equals("admin")){
                                            options=new String[]{"Remove Admin","Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (which==0){
                                                        removeAdmin(users);
                                                    }
                                                    else if(which==1){
                                                        removeParticipants(users);
                                                    }
                                                }
                                            }).create().show();

                                        }
                                        else if (previousRole.equals("participant")){
                                            options =new String[]{"Make Admin","Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (which==0){
                                                        makeAdmin(users);
                                                    }
                                                    else if(which==1){
                                                        removeParticipants(users);
                                                    }
                                                }
                                            }).create().show();
                                        }
                                    }
                                    else if (myGroupRole.equals("admin")){
                                        if (previousRole.equals("creator")){
                                            Toast.makeText(context,"Creator of the group",Toast.LENGTH_SHORT).show();
                                        }
                                        else if (previousRole.equals("admin")){
                                            options=new String[]{"Remove Admin","Remove User"};
                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (which==0){
                                                        removeAdmin(users);
                                                    }
                                                    else if(which==1){
                                                        removeParticipants(users);
                                                    }
                                                }
                                            }).create().show();
                                        }
                                    }
                                    else if (previousRole.equals("participant")){
                                        options =new String[]{"Make Admin","Remove User"};
                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (which==0){
                                                    makeAdmin(users);
                                                }
                                                else if(which==1){
                                                    removeParticipants(users);
                                                }
                                            }
                                        }).show();
                                    }

                                }
                                else {
                                    AlertDialog.Builder builder=new AlertDialog.Builder(context);
                                    builder.setTitle("Add Participant");
                                    builder.setMessage("Add user to this group?")
                                            .setPositiveButton("add", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    addParticipant(users);
                                                    
                                                }
                                            })
                                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            }).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }
        });
    }

    private void makeAdmin(ModelUsers users) {

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("role","admin");


        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(users.getUid()).updateChildren(hashMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(context,"User is now Admin.",Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeAdmin(ModelUsers users) {
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("role","participant");


        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(users.getUid()).updateChildren(hashMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(context,"User is no longer an admin.",Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeParticipants(ModelUsers users) {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(users.getUid()).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addParticipant(ModelUsers users) {
        String timestamp=""+System.currentTimeMillis();

        HashMap<String,String> hashMap=new HashMap<>();
        hashMap.put("uid",users.getUid());
        hashMap.put("role","participant");
        hashMap.put("timestamp",""+timestamp);

        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(users.getUid()).setValue(hashMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(context,"Added Successfully!",Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfAlreadyExists(ModelUsers users, HolderAddParticipants holder) {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(users.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String hisRole=""+dataSnapshot.child("role").getValue();
                            holder.statusTv.setText(hisRole);
                        }
                        else {
                            holder.statusTv.setText("");
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

    class HolderAddParticipants extends RecyclerView.ViewHolder{

        ImageView mAvatarIv;
        TextView mEmailTv,mNameTv,statusTv;

        public HolderAddParticipants(@NonNull View itemView) {
            super(itemView);

            mAvatarIv=itemView.findViewById(R.id.avatarIv);
            mEmailTv=itemView.findViewById(R.id.EmailTv);
            mNameTv=itemView.findViewById(R.id.NameTv);
            statusTv=itemView.findViewById(R.id.statusTv);
        }
    }
}
