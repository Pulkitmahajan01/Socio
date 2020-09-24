package com.example.socio.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.socio.Chats;
import com.example.socio.PostDetailActivity;
import com.example.socio.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class FirebaseMessaging extends FirebaseMessagingService {

    private static final String ADMIN_CHANNEL_ID = "admin_channel";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        SharedPreferences sp=getSharedPreferences("SP_USER",MODE_PRIVATE);
          String savedCurrentUser=  sp.getString("CURRENT_USERID","None");

          String notificationType=remoteMessage.getData().get("notificationType");
          if (notificationType.equals("PostNotification")){
              String sender=remoteMessage.getData().get("sender");
              String pId=remoteMessage.getData().get("pId");
              String pTitle=remoteMessage.getData().get("pTitle");
              String pDescription=remoteMessage.getData().get("pDescription");

              if (!sender.equals(savedCurrentUser)){
                  sendPostNotification(""+pId,""+pTitle,""+pDescription);
              }
          }
          else if (notificationType.equals("ChatNotification")){
              String sent=remoteMessage.getData().get("sent");
              String user=remoteMessage.getData().get("user");
              FirebaseUser fUSer= FirebaseAuth.getInstance().getCurrentUser();
              if (fUSer!=null && sent.equals(fUSer.getUid())){
                  if (!savedCurrentUser.equals(user)){
                      if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
                          sendOandAboveNotifications(remoteMessage);
                      }
                      else {
                          sendNormalNotifications(remoteMessage);
                      }
                  }
              }
          }

    }

    private void sendPostNotification(String pId, String pTitle, String pDescription) {
        NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId=new Random().nextInt(3000);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            setupPostNotificationChannel(notificationManager);
        }

        Intent intent=new Intent(this, PostDetailActivity.class);
        intent.putExtra("postId",pId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pIntent=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

        Bitmap largeIcon= BitmapFactory.decodeResource(getResources(), R.drawable.logo);
        Uri defSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder=new NotificationCompat.Builder(this,""+ADMIN_CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setLargeIcon(largeIcon)
                .setContentTitle(pTitle)
                .setContentText(pDescription)
                .setSound(defSoundUri)
                .setContentIntent(pIntent);

        notificationManager.notify(notificationId,builder.build());

    }

    private void setupPostNotificationChannel(NotificationManager notificationManager) {
        CharSequence channelName="New Notification";
        String channelDescription="device to device post Notification";

        NotificationChannel adminChannel=new NotificationChannel(ADMIN_CHANNEL_ID,channelName,NotificationManager.IMPORTANCE_HIGH);
        adminChannel.setDescription(channelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.GREEN);
        adminChannel.enableVibration(true);
        if (notificationManager!=null){
            notificationManager.createNotificationChannel(adminChannel);
        }
    }

    private void sendNormalNotifications(RemoteMessage remoteMessage) {
        String user=remoteMessage.getData().get("user");
        String body=remoteMessage.getData().get("body");
        String title=remoteMessage.getData().get("title");
        String icon=remoteMessage.getData().get("icon");

        RemoteMessage.Notification notification=remoteMessage.getNotification();
        int i=Integer.parseInt(user.replaceAll("[\\D]",""));
        Intent intent=new Intent(this, Chats.class);
        Bundle bundle=new Bundle();
        bundle.putString("hisUid",user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent=PendingIntent.getActivity(this,i,intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentText(body)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setSound(defSoundUri)
                .setContentIntent(pIntent);

        NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int j=0;
        if (i>0){
            j=i;
        }
        notificationManager.notify(j,builder.build());
    }

    private void sendOandAboveNotifications(RemoteMessage remoteMessage) {
        String user=remoteMessage.getData().get("user");
        String body=remoteMessage.getData().get("body");
        String title=remoteMessage.getData().get("title");
        String icon=remoteMessage.getData().get("icon");

        RemoteMessage.Notification notification=remoteMessage.getNotification();
        int i=Integer.parseInt(user.replaceAll("[\\D]",""));
        Intent intent=new Intent(this, Chats.class);
        Bundle bundle=new Bundle();
        bundle.putString("hisUid",user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent=PendingIntent.getActivity(this,i,intent,PendingIntent.FLAG_ONE_SHOT);

        Uri defSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoAndAboveNotification oreoAndAboveNotification=new OreoAndAboveNotification(this);
        Notification.Builder builder=oreoAndAboveNotification.getNotifications(title,  body, pIntent, defSoundUri,icon);

        int j=0;
        if (i>0){
            j=i;
        }
        oreoAndAboveNotification.getManager().notify(j,builder.build());
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        if (user!=null){
            updateToken(s);
        }
    }

    private void updateToken(String tokenRefresh) {
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Tokens");
        Token token=new Token(tokenRefresh);
        reference.child(user.getUid()).setValue(token);
    }
}
