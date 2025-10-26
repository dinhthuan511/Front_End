package com.example.book_store_mobileapp.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.book_store_mobileapp.R;
import com.example.book_store_mobileapp.network.FirebaseAdminService; // ✅ service
import com.example.book_store_mobileapp.ui.admin.adapter.AdminProductAdapter; // ✅ (nếu m đã tách adapter vào ui.admin.adapter)
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Quản lý sản phẩm trong collection "products" (qua service) */
public class AdminProductsActivity extends AppCompatActivity implements AdminProductAdapter.OnProductAction {

    private RecyclerView rv;
    private AdminProductAdapter adapter;
    private final List<DocumentSnapshot> docs = new ArrayList<>();

    private FirebaseAdminService adminService;        // ✅ service
    private ListenerRegistration productsReg = null;  // ✅ giữ listener để remove

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_products);

        MaterialToolbar tb = findViewById(R.id.topAppBar);
        if (tb != null) {
            setSupportActionBar(tb);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Modify product");
            }
            tb.setNavigationOnClickListener(v -> finish());
        }

        adminService = new FirebaseAdminService(); // ✅

        rv = findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminProductAdapter(docs, this);
        rv.setAdapter(adapter);

        listenProducts();
    }

    private void listenProducts() {
        // ✅ thay vì db.collection().addSnapshotListener(...)
        productsReg = adminService.listenProducts(100, new FirebaseAdminService.ProductsListener() {
            @Override public void onChanged(List<DocumentSnapshot> snapshots) {
                int count = (snapshots != null ? snapshots.size() : 0);
                Log.d("MANAGE", "docs=" + count);

                docs.clear();
                if (snapshots != null) {
                    // In chi tiết để chắc chắn đang đọc đúng collection & field
                    for (DocumentSnapshot d : snapshots) {
                        Log.d("MANAGE", "docId=" + d.getId()
                                + ", productName=" + d.getString("productName")
                                + ", imageURL=" + d.getString("imageURL")
                                + ", price=" + d.get("price")
                                + ", createdAt=" + d.get("createdAt"));
                    }
                    docs.addAll(snapshots);
                }
                adapter.notifyDataSetChanged();

                if (count == 0) {
                    Toast.makeText(AdminProductsActivity.this, "Chưa có sản phẩm nào", Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onError(String message) {
                Toast.makeText(AdminProductsActivity.this, "Lỗi load: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        // ✅ nhớ huỷ listener để tránh rò rỉ
        if (productsReg != null) {
            productsReg.remove();
            productsReg = null;
        }
    }

    /** Sửa đầy đủ field */
    @Override public void onEdit(DocumentSnapshot docSnap) {
        var view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_product, null, false);
        EditText edtName = view.findViewById(R.id.edtName);
        EditText edtAuthor = view.findViewById(R.id.edtAuthor);
        EditText edtPrice = view.findViewById(R.id.edtPrice);
        EditText edtStock = view.findViewById(R.id.edtStock);
        EditText edtCategoryId = view.findViewById(R.id.edtCategoryId);
        EditText edtIsbn = view.findViewById(R.id.edtIsbn);
        EditText edtImage = view.findViewById(R.id.edtImageURL);
        EditText edtBrief = view.findViewById(R.id.edtBrief);
        EditText edtFull = view.findViewById(R.id.edtFull);
        EditText edtSpecs = view.findViewById(R.id.edtSpecs);

        // fill sẵn
        edtName.setText(docSnap.getString("productName"));
        edtAuthor.setText(docSnap.getString("author"));
        Long price = docSnap.getLong("price");
        Long stock = docSnap.getLong("stock");
        Long catId = docSnap.getLong("categoryId");
        edtPrice.setText(price != null ? String.valueOf(price) : "");
        edtStock.setText(stock != null ? String.valueOf(stock) : "");
        edtCategoryId.setText(catId != null ? String.valueOf(catId) : "");
        edtIsbn.setText(docSnap.getString("isbn"));
        edtImage.setText(docSnap.getString("imageURL"));
        edtBrief.setText(docSnap.getString("briefDescription"));
        edtFull.setText(docSnap.getString("fullDescription"));
        edtSpecs.setText(docSnap.getString("technicalSpecifications"));

        new AlertDialog.Builder(this)
                .setTitle("Sửa sản phẩm")
                .setView(view)
                .setPositiveButton("Lưu", (d, w) -> {
                    String n = edtName.getText().toString().trim();
                    String a = edtAuthor.getText().toString().trim();
                    String p = edtPrice.getText().toString().trim();
                    String s = edtStock.getText().toString().trim();
                    String c = edtCategoryId.getText().toString().trim();
                    String isbn = edtIsbn.getText().toString().trim();
                    String img = edtImage.getText().toString().trim();
                    String b = edtBrief.getText().toString().trim();
                    String f = edtFull.getText().toString().trim();
                    String sp = edtSpecs.getText().toString().trim();

                    if (TextUtils.isEmpty(n) || TextUtils.isEmpty(p)) {
                        Toast.makeText(this, "productName/price không hợp lệ", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Long priceL, stockL = null, catL = null;
                    try { priceL = Long.parseLong(p); } catch (Exception ex) { Toast.makeText(this, "price không hợp lệ", Toast.LENGTH_SHORT).show(); return; }
                    try { if (!TextUtils.isEmpty(s)) stockL = Long.parseLong(s); } catch (Exception ex) { Toast.makeText(this, "stock không hợp lệ", Toast.LENGTH_SHORT).show(); return; }
                    try { if (!TextUtils.isEmpty(c)) catL = Long.parseLong(c); } catch (Exception ex) { Toast.makeText(this, "categoryId không hợp lệ", Toast.LENGTH_SHORT).show(); return; }

                    Map<String, Object> up = new HashMap<>();
                    up.put("productName", n);
                    up.put("author", TextUtils.isEmpty(a) ? null : a);
                    up.put("price", priceL);
                    if (stockL != null) up.put("stock", stockL); else up.put("stock", null);
                    if (catL != null) up.put("categoryId", catL); else up.put("categoryId", null);
                    up.put("isbn", TextUtils.isEmpty(isbn)? null : isbn);
                    up.put("imageURL", TextUtils.isEmpty(img)? null : img);
                    up.put("briefDescription", TextUtils.isEmpty(b)? null : b);
                    up.put("fullDescription", TextUtils.isEmpty(f)? null : f);
                    up.put("technicalSpecifications", TextUtils.isEmpty(sp)? null : sp);

                    // ✅ gọi service update
                    adminService.updateProduct(docSnap.getId(), up, res -> {
                        if (res.isSuccess()) {
                            Toast.makeText(this, "Đã cập nhật", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Lỗi: " + res.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    @Override public void onDelete(DocumentSnapshot doc) {
        new AlertDialog.Builder(this)
                .setTitle("Xoá sản phẩm?")
                .setMessage("Thao tác không thể hoàn tác.")
                .setPositiveButton("Xoá", (d, w) ->
                        // ✅ gọi service delete
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
