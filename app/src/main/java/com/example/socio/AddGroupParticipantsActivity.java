package com.example.socio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.socio.adapters.AdapterAddParticipants;
import com.example.socio.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AddGroupParticipantsActivity extends AppCompatActivity {

    RecyclerView usersRv;
    ActionBar actionBar;

    FirebaseAuth firebaseAuth;

    String groupId,myGroupRole;

    private ArrayList<ModelUsers>usersList;
    AdapterAddParticipants adapterAddParticipants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group_participants);

        usersRv=findViewById(R.id.usersRv);


        actionBar=getSupportActionBar();
        actionBar.setTitle("Add Participants");

        firebaseAuth=FirebaseAuth.getInstance();

        groupId=getIntent().getStringExtra("groupId");


        loadGroupInfo();
    }

    private void getAllUsers() {
        usersList =new ArrayList<>();
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelUsers model=ds.getValue(ModelUsers.class);

                    if (!firebaseAuth.getUid().equals(model.getUid())){
                    usersList.add(model);
                    }
                }
                adapterAddParticipants=new AdapterAddParticipants(
                        AddGroupParticipantsActivity.this,
                        usersList,
                        ""+groupId,
                        ""+myGroupRole);
                usersRv.setAdapter(adapterAddParticipants);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadGroupInfo() {
        DatabaseReference ref1= FirebaseDatabase.getInstance().getReference("Groups");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()) {
                    String groupTitle = "" + ds.child("groupTitle").getValue();
                    String groupDescription = "" + ds.child("groupDescription").getValue();
                    String groupIcon = "" + ds.child("groupIcon").getValue();
                    String timestamp = "" + ds.child("timestamp").getValue();
                    String createdBy = "" + ds.child("CreatedBy").getValue();
                    actionBar.setTitle("Add Participants");

                    ref1.child(groupId).child("Participants").child(firebaseAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()){
                                        myGroupRole=""+dataSnapshot.child("role").getValue();
                                        actionBar.setTitle(groupTitle+"("+myGroupRole+")");
                                        getAllUsers();

                                    }
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
}