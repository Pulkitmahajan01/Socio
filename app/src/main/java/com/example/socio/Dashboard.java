package com.example.socio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.socio.notifications.Token;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.List;

public class Dashboard extends AppCompatActivity {

    FirebaseAuth firebaseAuth;

   private BottomNavigationView navigationView;

    String mUID;
   // RecyclerView recyclerView;
   // AdapterUser adapterUser;
   // List<ModelUsers> usersList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        firebaseAuth=FirebaseAuth.getInstance();
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();

        navigationView=findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

        HomeFragment fragment1=new HomeFragment();
        FragmentTransaction f1=getSupportFragmentManager().beginTransaction();
        f1.replace(R.id.content,fragment1,"");
        f1.commit();

        checkuserStatus();
        if (firebaseUser!=null && firebaseUser.isEmailVerified()){
        updateToken(FirebaseInstanceId.getInstance().getToken());}
    }

    @Override
    protected void onResume() {
        checkuserStatus();
        super.onResume();

    }

    public void updateToken(String token){
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken=new Token(token);
        ref.child(mUID).setValue(mToken);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener=
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()){
                        case R.id.nav_home:
                            HomeFragment fragment1=new HomeFragment();
                            FragmentTransaction f1=getSupportFragmentManager().beginTransaction();
                            f1.replace(R.id.content,fragment1,"");
                            f1.commit();
                            return true;
                        case R.id.nav_profile:
                            ProfileFragment fragment2=new ProfileFragment();
                            FragmentTransaction f2=getSupportFragmentManager().beginTransaction();
                            f2.replace(R.id.content,fragment2,"");
                            f2.commit();
                            return true;
                        case R.id.nav_users:
                            UsersFragment fragment3=new UsersFragment();
                            FragmentTransaction f3=getSupportFragmentManager().beginTransaction();
                            f3.replace(R.id.content,fragment3,"");
                            f3.commit();
                            return true;
                        case R.id.nav_chat:
                            ChatListFragment fragment4=new ChatListFragment();
                            FragmentTransaction f4=getSupportFragmentManager().beginTransaction();
                            f4.replace(R.id.content,fragment4,"");
                            f4.commit();
                            return true;
                           // case R.id.nav_notification:
                           /* NotificationFragment fragment5=new NotificationFragment();
                            FragmentTransaction f5=getSupportFragmentManager().beginTransaction();
                            f5.replace(R.id.content,fragment5,"");
                            f5.commit();
                            return true;
                            */
                          case R.id.nav_more:
                          showMoreOptions();
                           return true;
                    }
                    return false;
                }
            };

    private void showMoreOptions() {
        PopupMenu popupMenu=new PopupMenu(this,navigationView, Gravity.END);
        popupMenu.getMenu().add(Menu.NONE,0,0,"Notifications");
        popupMenu.getMenu().add(Menu.NONE,1,0,"Group Chats");

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id=item.getItemId();
                if (id==0){

                    NotificationFragment fragment5=new NotificationFragment();
                    FragmentTransaction f5=getSupportFragmentManager().beginTransaction();
                    f5.replace(R.id.content,fragment5,"");
                    f5.commit();
                }
                if (id==1){
                    GroupChatsFragment fragment6=new GroupChatsFragment();
                    FragmentTransaction f6=getSupportFragmentManager().beginTransaction();
                    f6.replace(R.id.content,fragment6,"");
                    f6.commit();
                }
                return false;
            }
        });
popupMenu.show();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

  private void checkuserStatus(){
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if(user!=null && user.isEmailVerified()){
            mUID=user.getUid();

            SharedPreferences sp=getSharedPreferences("SP_USER",MODE_PRIVATE);
            SharedPreferences.Editor editor= sp.edit();
            editor.putString("CURRENT_USERID",mUID);
            editor.apply();
        }
        else{
            startActivity(new Intent(Dashboard.this,loginregister.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        checkuserStatus();
        super.onStart();
    }

}