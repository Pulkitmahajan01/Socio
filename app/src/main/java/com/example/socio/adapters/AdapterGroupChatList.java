package com.example.socio.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socio.GroupChatActivity;
import com.example.socio.R;
import com.example.socio.models.ModelGroupChatList;
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

public class AdapterGroupChatList extends RecyclerView.Adapter<AdapterGroupChatList.HolderGroupChatList> {

    private Context context;
    private ArrayList<ModelGroupChatList>groupChatLists;

    public AdapterGroupChatList(Context context, ArrayList<ModelGroupChatList> groupChatLists) {
        this.context = context;
        this.groupChatLists = groupChatLists;
    }

    @NonNull
    @Override
    public HolderGroupChatList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_groupchats_list,parent,false);
        return new HolderGroupChatList(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupChatList holder, int position) {
        ModelGroupChatList model=groupChatLists.get(position);
        String groupId=model.getGroupId();
        String groupIcon=model.getGroupIcon();
        String groupTitle=model.getGroupTitle();

        holder.groupTitleTv.setText(groupTitle);
        try {
            Picasso.get().load(groupIcon).placeholder(R.drawable.ic__group_white).into(holder.groupIconIv);
        }catch (Exception e){
            Picasso.get().load(R.drawable.ic__group_white).into(holder.groupIconIv);
        }

        holder.messageTv.setText("");
        holder.nameTv.setText("");
        holder.timeTv.setText("");

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
                public void onClick(View v) {
                    Intent intent=new Intent(context, GroupChatActivity.class);
                    intent.putExtra("groupId",groupId);
                    context.startActivity(intent);
                }
        });

        loadLastMessage(model,holder);
    }

    private void loadLastMessage(ModelGroupChatList model, HolderGroupChatList holder) {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(model.getGroupId()).child("Messages").limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds:dataSnapshot.getChildren()){
                            String message=""+ds.child("message").getValue();
                            String sender=""+ds.child("sender").getValue();
                            String timeStamp=""+ds.child("timeStamp").getValue();
                            String messageType=""+ds.child("type").getValue();

                            Instant inst;
                            if (timeStamp == null) {
                                inst = Instant.now();
                            } else {
                                inst = Instant.from(timestampFormatter.parse(timeStamp));
                            }
                            String dateTime = inst.atZone(ZoneId.systemDefault()).format(dtf);
                            holder.timeTv.setText(dateTime);

                            if (messageType.equals("image")){
                                holder.messageTv.setText("sent a photo.");
                            }
                            else{
                                holder.messageTv.setText(message);
                            }


                            DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
                            ref.orderByChild("uid").equalTo(sender)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot ds:dataSnapshot.getChildren()){
                                                String name=""+ds.child("name").getValue();

                                                holder.nameTv.setText(name+" :");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return groupChatLists.size();
    }

    class HolderGroupChatList extends RecyclerView.ViewHolder{
        ImageView groupIconIv;
        TextView groupTitleTv,nameTv,messageTv,timeTv;

        public HolderGroupChatList(@NonNull View itemView) {
            super(itemView);
            groupIconIv=itemView.findViewById(R.id.groupIconIv);
            groupTitleTv=itemView.findViewById(R.id.groupTitleTv);
            nameTv=itemView.findViewById(R.id.nameTv);
            messageTv=itemView.findViewById(R.id.messageTv);
            timeTv=itemView.findViewById(R.id.timeTv);
        }
    }

    private static DateTimeFormatter timestampFormatter = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.INSTANT_SECONDS)
            .appendValue(ChronoField.MILLI_OF_SECOND, 3)
            .toFormatter();

    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
}
