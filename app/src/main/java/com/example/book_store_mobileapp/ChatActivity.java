package com.example.book_store_mobileapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.book_store_mobileapp.adapter.ChatAdapter;
import com.example.book_store_mobileapp.data.chat.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<Message> messageList;
    private EditText editText;
    private Button sendButton;
    private Toolbar toolbar;

    private DatabaseReference databaseReference;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Setup toolbar
        toolbar = findViewById(R.id.toolbar_chat);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Trò chuyện với cửa hàng");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_view_chat);
        editText = findViewById(R.id.edit_text_chatbox);
        sendButton = findViewById(R.id.button_send);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        // Check if user is logged in
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            // Use email as username, sanitize it for Firebase key
            String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            String username = userEmail != null ? userEmail.replace(".", "_").replace("@", "_at_") : currentUserId;

            databaseReference = FirebaseDatabase.getInstance().getReference("chat").child(username);

            sendButton.setOnClickListener(v -> sendMessage());
            attachDatabaseReadListener();
        } else {
            Toast.makeText(this, "Bạn phải đăng nhập để sử dụng trò chuyện", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void sendMessage() {
        String messageText = editText.getText().toString().trim();
        if (!TextUtils.isEmpty(messageText) && databaseReference != null) {
            // Get username from email
            String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            String username = userEmail != null ? userEmail.split("@")[0] : "User";

            Message message = new Message(currentUserId, username, messageText);
            databaseReference.push().setValue(message)
                    .addOnSuccessListener(aVoid -> editText.setText(""))
                    .addOnFailureListener(e ->
                            Toast.makeText(ChatActivity.this, "Gửi tin nhắn thất bại", Toast.LENGTH_SHORT).show()
                    );
        }
    }

    private void attachDatabaseReadListener() {
        if (databaseReference == null) return;

        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                try {
                    Message message = snapshot.getValue(Message.class);
                    if (message != null) {
                        messageList.add(message);
                        chatAdapter.notifyDataSetChanged();
                        recyclerView.scrollToPosition(messageList.size() - 1);
                    }
                } catch (Exception e) {
                    Toast.makeText(ChatActivity.this, "Lỗi khi tải tin nhắn", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
