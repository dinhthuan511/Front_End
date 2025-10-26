package com.example.book_store_mobileapp;

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

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Quản lý sản phẩm trong collection "products" */
public class ManageProductsActivity extends AppCompatActivity implements AdminProductAdapter.OnProductAction {

    private RecyclerView rv;
    private AdminProductAdapter adapter;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final List<DocumentSnapshot> docs = new ArrayList<>();

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

        rv = findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminProductAdapter(docs, this);
        rv.setAdapter(adapter);

        load();
    }

    private void load() {
        db.collection("products")
                .limit(100)
                .addSnapshotListener((snaps, e) -> {
                    if (e != null) {
                        Log.e("MANAGE", "load error", e);
                        Toast.makeText(this, "Lỗi load: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int count = (snaps != null ? snaps.size() : 0);
                    Log.d("MANAGE", "docs=" + count);

                    docs.clear();
                    if (snaps != null) {
                        for (DocumentSnapshot d : snaps.getDocuments()) {
                            // In chi tiết để chắc chắn đang đọc đúng collection & field
                            Log.d("MANAGE", "docId=" + d.getId()
                                    + ", productName=" + d.getString("productName")
                                    + ", imageURL=" + d.getString("imageURL")
                                    + ", price=" + d.get("price")
                                    + ", createdAt=" + d.get("createdAt"));
                        }
                        docs.addAll(snaps.getDocuments());
                    }
                    adapter.notifyDataSetChanged();

                    if (count == 0) {
                        Toast.makeText(this, "Chưa có sản phẩm nào", Toast.LENGTH_SHORT).show();
                    }
                });

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

                    docSnap.getReference().update(up)
                            .addOnSuccessListener(vv -> Toast.makeText(this, "Đã cập nhật", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(err -> Toast.makeText(this, "Lỗi: " + err.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    @Override public void onDelete(DocumentSnapshot doc) {
        new AlertDialog.Builder(this)
                .setTitle("Xoá sản phẩm?")
                .setMessage("Thao tác không thể hoàn tác.")
                .setPositiveButton("Xoá", (d, w) ->
                        doc.getReference().delete()
                                .addOnSuccessListener(v -> Toast.makeText(this, "Đã xoá", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()))
                .setNegativeButton("Huỷ", null)
                .show();
    }
}
