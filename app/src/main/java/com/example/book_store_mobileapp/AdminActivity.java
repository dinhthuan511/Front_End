    package com.example.book_store_mobileapp;

    import android.content.Intent;
    import android.os.Bundle;
    import android.widget.Toast;

    import androidx.annotation.Nullable;
    import androidx.appcompat.app.AppCompatActivity;

    import com.google.android.material.appbar.MaterialToolbar;
    import com.google.android.material.button.MaterialButton;
    import com.google.firebase.auth.FirebaseAuth;

    public class AdminActivity extends AppCompatActivity {

        private MaterialButton btnAddProduct, btnModifyProduct;

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_admin);

            MaterialToolbar topAppBar = findViewById(R.id.topAppBar);

            // KHÔNG gọi setSupportActionBar(topAppBar);
            // Title đã đặt trong XML (app:title), vẫn có thể set lại nếu muốn:
            topAppBar.setTitle("Hello, Admin");

            // Xử lý click menu (Logout) – app:menu trong XML đã tự inflate
            topAppBar.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_logout) {
                    FirebaseAuth.getInstance().signOut();
                    Intent i = new Intent(this, LoginActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    return true;
                }
                return false;
            });

            btnAddProduct    = findViewById(R.id.btnAddProduct);
            btnModifyProduct = findViewById(R.id.btnModifyProduct);

            btnAddProduct.setOnClickListener(v ->
                    startActivity(new Intent(this, AddProductActivity.class)));
            btnModifyProduct.setOnClickListener(v ->
                    startActivity(new Intent(this, ManageProductsActivity.class)));
        }



        private void go(Class<?> cls, String fallbackToast) {
            try {
                startActivity(new Intent(this, cls));
            } catch (Exception e) {
                // Nếu Activity đích chưa tạo, vẫn báo cho bạn biết
                Toast.makeText(this, fallbackToast + " (chưa tạo Activity đích)", Toast.LENGTH_SHORT).show();
            }
        }
    }
