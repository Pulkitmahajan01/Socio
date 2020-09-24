package com.example.socio.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socio.R;
import com.example.socio.models.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

@RequiresApi(api = Build.VERSION_CODES.O)
public class AdapterChat extends RecyclerView.Adapter<AdapterChat.myHolder> {

    public static final int MSG_TYPE_LEFT=0;
    public static final int MSG_TYPE_RIGHT=1;
    Context context;
    List<ModelChat> chatList;
    String ImageUrl;

    public AdapterChat(Context context, List<ModelChat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.ImageUrl = imageUrl;
    }

    FirebaseUser fUser;

    class myHolder extends RecyclerView.ViewHolder{

        ImageView profileTv,messageIv;
        TextView messageTv,timeTv,IsSeenTv;
        LinearLayout messageLayout;

        public myHolder(@NonNull View itemView) {
            super(itemView);

            profileTv=itemView.findViewById(R.id.profile_tv);
            messageTv=itemView.findViewById(R.id.messageTv);
            timeTv=itemView.findViewById(R.id.timeTv);
            IsSeenTv=itemView.findViewById(R.id.isSeenTv);
            messageLayout=itemView.findViewById(R.id.messageLayout);
            messageIv=itemView.findViewById(R.id.messageIv);
        }
    }

    @NonNull
    @Override
    public myHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==MSG_TYPE_RIGHT){
            View view= LayoutInflater.from(context).inflate(R.layout.row_chat_right,parent,false);
            return new myHolder(view);
        }
        else {
            View view= LayoutInflater.from(context).inflate(R.layout.row_chat_left,parent,false);
            return new myHolder(view);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull myHolder holder, final int position) {

        String message=chatList.get(position).getMessage();
        String timeStamp=chatList.get(position).getTimeStamp();
        String type=chatList.get(position).getType();

       Instant inst;
        if (timeStamp == null) {
            inst = Instant.now();
        } else {
            inst = Instant.from(timestampFormatter.parse(timeStamp));
       }
        String dateTime = inst.atZone(ZoneId.systemDefault()).format(dtf);
        holder.timeTv.setText(dateTime);

        if (type.equals("text")){
            holder.messageTv.setVisibility(View.VISIBLE);
            holder.messageIv.setVisibility(View.GONE);

            holder.messageTv.setText(message);
        }
        else {
            holder.messageTv.setVisibility(View.GONE);
            holder.messageIv.setVisibility(View.VISIBLE);

            Picasso.get().load(message).placeholder(R.drawable.ic_image_black).into(holder.messageIv);
        }

       try {
            Picasso.get().load(ImageUrl).into(holder.profileTv);
        }
        catch (Exception e){

        }

       holder.messageLayout.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               AlertDialog.Builder builder=new AlertDialog.Builder(context);
               builder.setTitle("Delete");
               builder.setMessage("Delete Message?");
               builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                            deleteMessage(position);
                   }
               });
               builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                   }
               });
               builder.create().show();
           }
       });

       if (position==chatList.size()-1){
            if (chatList.get(position).isSeen()) {
                holder.IsSeenTv.setText("Seen");
            }
            else {
                holder.IsSeenTv.setText("Delivered");
            }
        }else{
            holder.IsSeenTv.setVisibility(View.GONE);

        }

    }

    private void deleteMessage(int i) {
        final String myUid=FirebaseAuth.getInstance().getCurrentUser().getUid();


        String msgTimeStamp=chatList.get(i).getTimeStamp();
        DatabaseReference dRef= FirebaseDatabase.getInstance().getReference("Chats");
        Query query=dRef.orderByChild("timeStamp").equalTo(msgTimeStamp);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds:dataSnapshot.getChildren()){

                        if (ds.child("sender").getValue().equals(myUid)){
                        //    ds.getRef().removeValue();
                            HashMap<String,Object> hashMap=new HashMap<>();
                            hashMap.put("message","This message was deleted...");
                            ds.getRef().updateChildren(hashMap);
                            Toast.makeText(context,"message Deleted",Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(context,"you can only delete your messages",Toast.LENGTH_SHORT).show();
                        }


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

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        fUser= FirebaseAuth.getInstance().getCurrentUser();
        if (chatList.get(position).getSender().equals(fUser.getUid())){
            return MSG_TYPE_RIGHT;
        }
    else {
    return MSG_TYPE_LEFT;
        }
    }



}
