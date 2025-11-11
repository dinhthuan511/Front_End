package com.example.book_store_mobileapp.ui.admin;

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

import com.example.book_store_mobileapp.R;
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

public class AdminChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<Message> messageList;
    private EditText editText;
    private Button sendButton;
    private Toolbar toolbar;

    private DatabaseReference databaseReference;
    private String targetUsername;
    private String userEmail;
    private String adminId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Get user info from intent (userId is now username/sanitized email)
        targetUsername = getIntent().getStringExtra("userId"); // This is actually the username now
        userEmail = getIntent().getStringExtra("userEmail");

        if (targetUsername == null) {
            Toast.makeText(this, "Error: Username not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup toolbar
        toolbar = findViewById(R.id.toolbar_chat);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Nhắn với " + (userEmail != null ? userEmail : "User"));
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_view_chat);
        editText = findViewById(R.id.edit_text_chatbox);
        sendButton = findViewById(R.id.button_send);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        // Get admin ID
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            adminId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            adminId = "admin";
        }

        // Reference to the user's chat using username
        databaseReference = FirebaseDatabase.getInstance().getReference("chat").child(targetUsername);

        sendButton.setOnClickListener(v -> sendMessage());
        attachDatabaseReadListener();
    }

    private void sendMessage() {
        String messageText = editText.getText().toString().trim();
        if (!TextUtils.isEmpty(messageText) && databaseReference != null) {
            // Send message as admin with "Admin" as sender name
            Message message = new Message(adminId, "Admin", messageText);
            databaseReference.push().setValue(message)
                    .addOnSuccessListener(aVoid -> editText.setText(""))
                    .addOnFailureListener(e ->
                            Toast.makeText(AdminChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(AdminChatActivity.this, "Error loading message", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(AdminChatActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}