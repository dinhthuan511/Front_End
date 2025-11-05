package com.example.book_store_mobileapp.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.book_store_mobileapp.R;
import com.example.book_store_mobileapp.network.FirebaseAdminService; // ✅ service
import com.example.book_store_mobileapp.ui.admin.adapter.AdminProductAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý danh sách sản phẩm:
 * - Lắng nghe realtime products qua FirebaseAdminService
 * - onEdit: mở EditProductActivity (KHÔNG dùng layout cũ có edtImageURL nữa)
 * - onDelete: gọi service xóa
 */
public class AdminProductsActivity extends AppCompatActivity implements AdminProductAdapter.OnProductAction {

    private RecyclerView rv;
    private AdminProductAdapter adapter;
    private final List<DocumentSnapshot> docs = new ArrayList<>();

    private FirebaseAdminService adminService;
    private ListenerRegistration productsReg = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_products);

        // Toolbar
        MaterialToolbar tb = findViewById(R.id.topAppBar);
        if (tb != null) {
            setSupportActionBar(tb);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Quản lý sản phẩm");
            }
            tb.setNavigationOnClickListener(v -> finish());
        }

        adminService = new FirebaseAdminService();

        rv = findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminProductAdapter(docs, this);
        rv.setAdapter(adapter);

        listenProducts();
    }

    private void listenProducts() {
        productsReg = adminService.listenProducts(200, new FirebaseAdminService.ProductsListener() {
            @Override public void onChanged(List<DocumentSnapshot> snapshots) {
                docs.clear();
                if (snapshots != null) {
                    docs.addAll(snapshots);
                    for (DocumentSnapshot d : snapshots) {
                        String name  = d.getString("productName");
                        String url   = d.getString("imageURL");
                        String b64   = d.getString("imageBase64");
                        Log.d("ADMIN_PRODUCTS",
                                "docId=" + d.getId()
                                        + ", name=" + name
                                        + ", price=" + d.get("price")
                                        + ", imageURL=" + url
                                        + ", imageBase64=" + (b64 == null ? "null" : ("len=" + b64.length())));
                    }
                }
                adapter.notifyDataSetChanged();

                if (docs.isEmpty()) {
                    Toast.makeText(AdminProductsActivity.this, "Chưa có sản phẩm nào", Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onError(String message) {
                Toast.makeText(AdminProductsActivity.this, "Lỗi load: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override public void onEdit(DocumentSnapshot docSnap) {
        Intent i = new Intent(this, EditProductActivity.class);
        i.putExtra("productId", docSnap.getId());
        i.putExtra("productName", docSnap.getString("productName"));
        i.putExtra("author", docSnap.getString("author"));
        // ✅ ưu tiên truyền base64
        i.putExtra("imageBase64", docSnap.getString("imageBase64"));
        // (giữ URL nếu các item cũ còn xài Storage)
        i.putExtra("imageURL", docSnap.getString("imageURL"));
        if (docSnap.getLong("price") != null)      i.putExtra("price", docSnap.getLong("price"));
        if (docSnap.getLong("stock") != null)      i.putExtra("stock", docSnap.getLong("stock"));
        if (docSnap.getLong("categoryId") != null) i.putExtra("categoryId", docSnap.getLong("categoryId"));
        i.putExtra("isbn", docSnap.getString("isbn"));
        i.putExtra("briefDescription", docSnap.getString("briefDescription"));
        i.putExtra("fullDescription", docSnap.getString("fullDescription"));
        i.putExtra("technicalSpecifications", docSnap.getString("technicalSpecifications"));
        startActivity(i);
    }


    /** ✅ Bấm xóa: gọi service */
    @Override public void onDelete(DocumentSnapshot doc) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xoá sản phẩm?")
                .setMessage("Thao tác không thể hoàn tác.")
                .setPositiveButton("Xoá", (d, w) ->
                        adminService.deleteProduct(doc.getId(), res -> {
                            if (res.isSuccess()) {
                                Toast.makeText(this, "Đã xoá", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Lỗi: " + res.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })
                )
                .setNegativeButton("Huỷ", null)
                .show();
    }
}