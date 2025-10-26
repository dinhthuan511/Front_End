package com.example.book_store_mobileapp.ui.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.book_store_mobileapp.R;
import com.example.book_store_mobileapp.network.FirebaseAdminService; // ✅ dùng service
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

/** Thêm tài liệu vào collection "products" theo schema mới (qua service) */
public class AddEditProductActivity extends AppCompatActivity {

    private TextInputEditText edtProductName, edtAuthor, edtPrice, edtStock, edtCategoryId,
            edtIsbn, edtImageURL, edtBrief, edtFull, edtSpecs;
    private ImageView ivPreview;
    private Button btnPreview, btnSave;

    // ✅ Service admin
    private FirebaseAdminService adminService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        // Toolbar back
        MaterialToolbar tb = findViewById(R.id.topAppBar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tb.setNavigationOnClickListener(v -> finish());

        adminService = new FirebaseAdminService(); // ✅ init service

        // Bind
        edtProductName = findViewById(R.id.edtProductName);
        edtAuthor      = findViewById(R.id.edtAuthor);
        edtPrice       = findViewById(R.id.edtPrice);
        edtStock       = findViewById(R.id.edtStock);
        edtCategoryId  = findViewById(R.id.edtCategoryId);
        edtIsbn        = findViewById(R.id.edtIsbn);
        edtImageURL    = findViewById(R.id.edtImageURL);
        edtBrief       = findViewById(R.id.edtBrief);
        edtFull        = findViewById(R.id.edtFull);
        edtSpecs       = findViewById(R.id.edtSpecs);
        ivPreview      = findViewById(R.id.ivPreview);
        btnPreview     = findViewById(R.id.btnPreview);
        btnSave        = findViewById(R.id.btnSave);

        btnPreview.setOnClickListener(v -> {
            String url = t(edtImageURL);
            if (TextUtils.isEmpty(url)) { toast("Nhập imageURL trước"); return; }
            Glide.with(this).load(url).into(ivPreview);
        });

        btnSave.setOnClickListener(v -> save());
    }

    private void save() {
        String name  = t(edtProductName);
        String priceS= t(edtPrice);
        String stockS= t(edtStock);
        String catS  = t(edtCategoryId);

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceS)) {
            toast("Cần ít nhất productName + price");
            return;
        }

        long price, stock = 0, categoryId = 0;
        try { price = Long.parseLong(priceS); } catch (Exception e) { toast("price không hợp lệ"); return; }
        try { if (!TextUtils.isEmpty(stockS)) stock = Long.parseLong(stockS); } catch (Exception e) { toast("stock không hợp lệ"); return; }
        try { if (!TextUtils.isEmpty(catS)) categoryId = Long.parseLong(catS); } catch (Exception e) { toast("categoryId không hợp lệ"); return; }

        Map<String, Object> doc = new HashMap<>();
        doc.put("productName", name);
        doc.put("author", emptyToNull(t(edtAuthor)));
        doc.put("price", price);
        doc.put("stock", stock);
        doc.put("categoryId", categoryId);
        doc.put("isbn", emptyToNull(t(edtIsbn)));
        doc.put("imageURL", emptyToNull(t(edtImageURL)));
        doc.put("briefDescription", emptyToNull(t(edtBrief)));
        doc.put("fullDescription", emptyToNull(t(edtFull)));
        doc.put("technicalSpecifications", emptyToNull(t(edtSpecs)));
        doc.put("createdAt", System.currentTimeMillis());

        // ✅ Gọi service thay vì db.collection("products").add(...)
        adminService.addProduct(doc, res -> {
            if (!res.isSuccess()) {
                toast("Lỗi thêm: " + res.getMessage());
                return;
            }
            toast("Đã thêm sản phẩm (id=" + res.getData() + ")");
            finish();
        });
    }

    private static String t(TextInputEditText e){ return e==null?"":String.valueOf(e.getText()).trim(); }
    private static Object emptyToNull(String s){ return TextUtils.isEmpty(s)?null:s; }
    private void toast(String m){ Toast.makeText(this, m, Toast.LENGTH_SHORT).show(); }
}
