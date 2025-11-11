package com.example.book_store_mobileapp.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.book_store_mobileapp.R;
import com.example.book_store_mobileapp.adapter.AdminUserChatAdapter;
import com.example.book_store_mobileapp.data.UserChatInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminChatListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminUserChatAdapter adapter;
    private List<UserChatInfo> userChatList;
    private DatabaseReference chatRef;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_chat_list);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Trò chuyện");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_view_user_chats);
        userChatList = new ArrayList<>();

        adapter = new AdminUserChatAdapter(this, userChatList, userChatInfo -> {
            // Open chat with this user
            Intent intent = new Intent(AdminChatListActivity.this, AdminChatActivity.class);
            intent.putExtra("userId", userChatInfo.getUserId());
            intent.putExtra("userEmail", userChatInfo.getUserEmail());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        chatRef = FirebaseDatabase.getInstance().getReference("chat");
        loadUserChats();
    }

    private void loadUserChats() {
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userChatList.clear();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String username = userSnapshot.getKey(); // Now this is the sanitized email

                    // Get last message
                    String lastMessage = "";
                    long lastTimestamp = 0;
                    for (DataSnapshot messageSnapshot : userSnapshot.getChildren()) {
                        Object textObj = messageSnapshot.child("text").getValue();
                        Object timestampObj = messageSnapshot.child("timestamp").getValue();

                        if (textObj != null && timestampObj != null) {
                            lastMessage = textObj.toString();
                            lastTimestamp = Long.parseLong(timestampObj.toString());
                        }
                    }

                    if (!lastMessage.isEmpty()) {
                        // Convert sanitized username back to readable email format
                        String displayEmail = username.replace("_at_", "@").replace("_", ".");
                        UserChatInfo chatInfo = new UserChatInfo(username, displayEmail, lastMessage, lastTimestamp);
                        userChatList.add(chatInfo);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminChatListActivity.this,
                        "Error loading chats: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}