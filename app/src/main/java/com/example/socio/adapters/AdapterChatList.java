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

import com.example.socio.Chats;
import com.example.socio.R;
import com.example.socio.models.ModelUsers;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class AdapterChatList extends RecyclerView.Adapter<AdapterChatList.MyHolder>{

    Context context;
    List<ModelUsers> usersList;
    private HashMap<String,String> lastMsgMap;

    public AdapterChatList(Context context, List<ModelUsers> usersList) {
        this.context = context;
        this.usersList = usersList;
        lastMsgMap = new HashMap<>();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_chatlist,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int i) {
        String hisUid=usersList.get(i).getUid();
        String userImage=usersList.get(i).getImage();
        String userName=usersList.get(i).getName();
        String lastMsg=lastMsgMap.get(hisUid);


        if (lastMsg==null){
            lastMsg=" ";
        }

        holder.nameTv.setText(userName);
      /*  if (lastMsg!=null || lastMsg.equals("default")){
            holder.lastMsgTv.setVisibility(View.GONE);
        }*/
       // else {
            holder.lastMsgTv.setVisibility(View.VISIBLE);
            holder.lastMsgTv.setText(lastMsg);
     //   }

        try {
            Picasso.get().load(userImage).placeholder(R.drawable.ic_default_img_black).into(holder.profileIv);
        }
        catch (Exception e){
            Picasso.get().load(R.drawable.ic_default_img_black).into(holder.profileIv);
        }

        if (usersList.get(i).getOnlineStatus().equals("online")){
           holder.onlineStatusIv.setImageResource(R.drawable.circle_online);
        }
        else {
            holder.onlineStatusIv.setImageResource(R.drawable.circle_offline);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, Chats.class);
                intent.putExtra("hisUid",hisUid);
                context.startActivity(intent);
            }
        });
    }

    public void setLastMsgMap(String userId,String lastMsg){
        lastMsgMap.put(userId,lastMsg);
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        ImageView profileIv,onlineStatusIv;
        TextView nameTv,lastMsgTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profileIv=itemView.findViewById(R.id.profileIv);
            onlineStatusIv=itemView.findViewById(R.id.onlineStatusIv);
            nameTv=itemView.findViewById(R.id.nameTv);
            lastMsgTv=itemView.findViewById(R.id.lastMsgTv);
        }
    }
}

