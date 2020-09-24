package com.example.socio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socio.adapters.AdapterAddParticipants;
import com.example.socio.models.ModelUsers;
import com.example.socio.notifications.Data;
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

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;

public class GroupInfoActivity extends AppCompatActivity {

    String groupId;
    ActionBar actionBar;

    ImageView groupIconIv;
    TextView groupDescriptionTv,createdByTv,editGroupTv,addparticipantsTv,leavegrpTv,participantsCountTv;
    RecyclerView participantsRv;

    private String myGroupRole="";

    ArrayList<ModelUsers> usersArrayList;
    AdapterAddParticipants adapterAddParticipants;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        groupId=getIntent().getStringExtra("groupId");
        actionBar=getSupportActionBar();
      //  actionBar.setTitle("Group Info");
        groupIconIv=findViewById(R.id.groupIconIv);
        groupDescriptionTv=findViewById(R.id.groupDescriptionTv);
        createdByTv=findViewById(R.id.createdByTv);
        editGroupTv=findViewById(R.id.editGroupTv);
        addparticipantsTv=findViewById(R.id.addparticipantsTv);
        leavegrpTv=findViewById(R.id.leavegrpTv);
        participantsCountTv=findViewById(R.id.participantsCountTv);
        participantsRv=findViewById(R.id.participantsRv);

        addparticipantsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(GroupInfoActivity.this,AddGroupParticipantsActivity.class);
                intent.putExtra("groupId",groupId);
                startActivity(intent);
            }
        });

        leavegrpTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dialogTitle="";
                String dialogDesc="";
                String positiveButtonTitle="";
                if (myGroupRole.equals("creator")){
                    dialogTitle="Delete Group";
                    dialogDesc="Are you sure you want to permanently delete this group?";
                    positiveButtonTitle="DELETE";
                }
                else{
                    dialogTitle="Leave Group";
                    dialogDesc="Are you sure you want to leave this group?";
                    positiveButtonTitle="LEAVE";
                }

                AlertDialog.Builder builder=new AlertDialog.Builder(GroupInfoActivity.this);
                builder.setTitle(dialogTitle)
                        .setMessage(dialogDesc)
                        .setPositiveButton(positiveButtonTitle, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                        if (myGroupRole.equals("creator")){
                                            deleteGroup();
                                        }else {
                                            leaveGroup();
                                        }
                            }
                        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
            }
        });

        editGroupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(GroupInfoActivity.this,GroupEditActivity.class);
                intent.putExtra("groupId",groupId);
                startActivity(intent);
            }
        });

        firebaseAuth=FirebaseAuth.getInstance();
        loadGroupInfo();
        loadMyGroupRole();

    }

    private void leaveGroup() {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(firebaseAuth.getUid()).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(GroupInfoActivity.this,"You left the Group",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(GroupInfoActivity.this,Dashboard.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupInfoActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteGroup() {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(GroupInfoActivity.this,"Group Deleted",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(GroupInfoActivity.this,Dashboard.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupInfoActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMyGroupRole() {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds:dataSnapshot.getChildren()){
                            myGroupRole=""+ds.child("role").getValue();
                            actionBar.setSubtitle(firebaseAuth.getCurrentUser().getDisplayName()+"("+myGroupRole+")");

                            if (myGroupRole.equals("participant")){
                                editGroupTv.setVisibility(View.GONE);
                                addparticipantsTv.setVisibility(View.GONE);
                                leavegrpTv.setText("Leave Group");
                            }
                            else if (myGroupRole.equals("admin")){
                                editGroupTv.setVisibility(View.GONE);
                                addparticipantsTv.setVisibility(View.VISIBLE);
                                leavegrpTv.setText("Leave Group");
                            }
                            else if(myGroupRole.equals("creator")){
                                editGroupTv.setVisibility(View.VISIBLE);
                                addparticipantsTv.setVisibility(View.VISIBLE);
                                leavegrpTv.setText("Delete Group");
                            }
                        }
                        loadParticipants();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadParticipants() {
        usersArrayList=new ArrayList<>();
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersArrayList.clear();
             for (DataSnapshot ds:dataSnapshot.getChildren()){
                 String uid=""+ds.child("uid").getValue();

                 DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
                 ref.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
                     @Override
                     public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                         for (DataSnapshot ds:dataSnapshot.getChildren()){
                             ModelUsers users=ds.getValue(ModelUsers.class);
                             usersArrayList.add(users);

                         }
                         adapterAddParticipants=new AdapterAddParticipants(GroupInfoActivity.this,usersArrayList,groupId,myGroupRole);
                         participantsCountTv.setText("Participants ("+usersArrayList.size()+")");
                         participantsRv.setAdapter(adapterAddParticipants);
                     }

                     @Override
                     public void onCancelled(@NonNull DatabaseError databaseError) {

                     }
                 });
             }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void loadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
             for (DataSnapshot ds: dataSnapshot.getChildren()){
                 String groupTitle = "" + ds.child("groupTitle").getValue();
                 String groupId = "" + ds.child("groupId").getValue();
                 String groupDescription = "" + ds.child("groupDescription").getValue();
                 String groupIcon = "" + ds.child("groupIcon").getValue();
                 String timestamp = "" + ds.child("timestamp").getValue();
                 String CreatedBy = "" + ds.child("CreatedBy").getValue();

                 actionBar.setTitle(groupTitle);
                 groupDescriptionTv.setText(groupDescription);

                 Instant inst;
                 if (timestamp == null) {
                     inst = Instant.now();
                 } else {
                     inst = Instant.from(timestampFormatter.parse(timestamp));
                 }
                 String dateTime = inst.atZone(ZoneId.systemDefault()).format(dtf);

                 loadCreatorInfo(dateTime,CreatedBy);

                 try {
                     Picasso.get().load(groupIcon).placeholder(R.drawable.ic__group_white).into(groupIconIv);

                 }catch (Exception e){
                     groupIconIv.setImageResource(R.drawable.ic__group_white);

                 }
             }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void loadCreatorInfo(String dateTime, String CreatedBy) {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(CreatedBy).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    String name=""+ds.child("name").getValue();
                    createdByTv.setText("Created by "+name+" on "+dateTime);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private static DateTimeFormatter timestampFormatter = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.INSTANT_SECONDS)
            .appendValue(ChronoField.MILLI_OF_SECOND, 3)
            .toFormatter();

    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
}