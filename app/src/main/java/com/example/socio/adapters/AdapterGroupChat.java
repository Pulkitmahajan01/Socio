package com.example.socio.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socio.R;
import com.example.socio.models.ModelGroupChat;
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

public class AdapterGroupChat extends RecyclerView.Adapter<AdapterGroupChat.HolderGroupChat> {
    public static final int MSG_TYPE_LEFT=0;
    public static final int MSG_TYPE_RIGHT=1;
    Context context;
    ArrayList<ModelGroupChat>groupChatArrayList;

    FirebaseAuth firebaseAuth;

    public AdapterGroupChat(Context context, ArrayList<ModelGroupChat> groupChatArrayList) {
        this.context = context;
        this.groupChatArrayList = groupChatArrayList;

        firebaseAuth = FirebaseAuth.getInstance();
        }

    @NonNull
    @Override
    public HolderGroupChat onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==MSG_TYPE_RIGHT){
            View view= LayoutInflater.from(context).inflate(R.layout.row_groupchat_right,parent,false);
            return new HolderGroupChat(view);
        }
        else {
            View view= LayoutInflater.from(context).inflate(R.layout.row_groupchat_left,parent,false);
            return new HolderGroupChat(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupChat holder, int position) {
        ModelGroupChat model=groupChatArrayList.get(position);

        String message=model.getMessage();
        String senderUid=model.getSender();
        String timestamp=model.getTimeStamp();
        String messageType=model.getType();

        if (messageType.equals("text")){
            holder.messageTv.setVisibility(View.VISIBLE);
            holder.messageIv.setVisibility(View.GONE);
            holder.messageTv.setText(message);
        }
        else {
            holder.messageTv.setVisibility(View.GONE);
            holder.messageIv.setVisibility(View.VISIBLE);

            try {
                Picasso.get().load(message).placeholder(R.drawable.ic_image_black).into(holder.messageIv);
            }
            catch (Exception e){
                Picasso.get().load(R.drawable.ic_image_black).into(holder.messageIv);

            }
        }

        Instant inst;
        if (timestamp == null) {
            inst = Instant.now();
        } else {
            inst = Instant.from(timestampFormatter.parse(timestamp));
        }
        String dateTime = inst.atZone(ZoneId.systemDefault()).format(dtf);
        holder.timeTv.setText(dateTime);

        setUserName(model,holder);
    }


    @Override
    public int getItemCount() {
        return groupChatArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (groupChatArrayList.get(position).getSender().equals(firebaseAuth.getUid())){
            return MSG_TYPE_RIGHT;
        }
        else {
            return MSG_TYPE_LEFT;
        }
    }

    private void setUserName(ModelGroupChat model, HolderGroupChat holder) {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(model.getSender())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds:dataSnapshot.getChildren()){
                            String name=""+ds.child("name").getValue();

                            holder.nameTv.setText(name);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    class HolderGroupChat extends RecyclerView.ViewHolder {

            TextView messageTv,timeTv,nameTv;
            ImageView messageIv;

            public HolderGroupChat(@NonNull View itemView) {
                super(itemView);
                messageTv=itemView.findViewById(R.id.messageTv);
                messageIv=itemView.findViewById(R.id.messageIv);
                timeTv=itemView.findViewById(R.id.timeTv);
                nameTv=itemView.findViewById(R.id.nameTv);
            }
        }

    private static DateTimeFormatter timestampFormatter = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.INSTANT_SECONDS)
            .appendValue(ChronoField.MILLI_OF_SECOND, 3)
            .toFormatter();

    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
}
