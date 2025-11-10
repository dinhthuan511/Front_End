package com.example.book_store_mobileapp.ui.admin;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.book_store_mobileapp.R;
import com.example.book_store_mobileapp.adapter.ImagesAdapter;
import com.example.book_store_mobileapp.network.FirebaseAdminService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AddProductActivity extends AppCompatActivity {

    private TextInputEditText edtProductName, edtAuthor, edtPrice, edtStock,
            edtCategoryId, edtIsbn, edtBrief, edtFull, edtSpecs;
    private RecyclerView rvImages;
    private FloatingActionButton btnPickImage;
    private Button btnSave;

    private List<Uri> pickedImages = new ArrayList<>();
    private ImagesAdapter imagesAdapter;
    private FirebaseAdminService adminService;
    private ActivityResultLauncher<String> pickImageLauncher;

    private FirebaseStorage storage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        MaterialToolbar tb = findViewById(R.id.topAppBar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tb.setNavigationOnClickListener(v -> finish());

        adminService = new FirebaseAdminService();
        storage = FirebaseStorage.getInstance();

        bindViews();
        setupPriceFormatter();
        setupImagePicker();
        setupRecyclerView();

        btnSave.setOnClickListener(v -> saveProduct());
    }

    private void bindViews() {
        edtProductName = findViewById(R.id.edtProductName);
        edtAuthor = findViewById(R.id.edtAuthor);
        edtPrice = findViewById(R.id.edtPrice);
        edtStock = findViewById(R.id.edtStock);
        edtCategoryId = findViewById(R.id.edtCategoryId);
        edtIsbn = findViewById(R.id.edtIsbn);
        edtBrief = findViewById(R.id.edtBrief);
        edtFull = findViewById(R.id.edtFull);
        edtSpecs = findViewById(R.id.edtSpecs);
        rvImages = findViewById(R.id.rvImages);
        btnPickImage = findViewById(R.id.btnPickImage);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupPriceFormatter() {
        edtPrice.addTextChangedListener(new TextWatcher() {
            private String cur = "";

            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                if (!s.toString().equals(cur)) {
                    edtPrice.removeTextChangedListener(this);
                    String clean = s.toString().replace(".", "");
                    if (!clean.isEmpty()) {
                        try {
                            String f = NumberFormat.getInstance(new Locale("vi", "VN"))
                                    .format(Long.parseLong(clean));
                            cur = f;
                            edtPrice.setText(f);
                            edtPrice.setSelection(f.length());
                        } catch (NumberFormatException ignore) {}
                    }
                    edtPrice.addTextChangedListener(this);
                }
            }
        });
    }

    private void setupImagePicker() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetMultipleContents(),
                uris -> {
                    if (uris != null && !uris.isEmpty()) {
                        pickedImages.clear();
                        pickedImages.addAll(uris);
                        imagesAdapter.notifyDataSetChanged();
                    }
                });
        btnPickImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
    }

    private void setupRecyclerView() {
        imagesAdapter = new ImagesAdapter(this, pickedImages);
        rvImages.setAdapter(imagesAdapter);
        rvImages.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
    }

    private void saveProduct() {
        String name = t(edtProductName);
        String priceS = t(edtPrice).replace(".", "");
        String stockS = t(edtStock);
        String catS = t(edtCategoryId);

        if (TextUtils.isEmpty(name)) { toast("Tên sản phẩm không được trống"); return; }
        if (TextUtils.isEmpty(priceS)) { toast("Giá không được trống"); return; }
        if (pickedImages.isEmpty()) { toast("Chưa chọn ảnh sản phẩm"); return; }

        long price = parseLongOr(priceS, -1);
        if (price <= 0) { toast("Giá phải > 0"); return; }
        long stock = parseLongOr(stockS, 0);
        if (stock < 0) { toast("Tồn kho phải ≥ 0"); return; }
        long categoryId = parseLongOr(catS, 0);

        // upload ảnh lên Firebase Storage
        uploadImagesThenSave(name, price, stock, categoryId);
    }

    private void uploadImagesThenSave(String name, long price, long stock, long categoryId) {
        List<String> uploadedUrls = new ArrayList<>();
        AtomicInteger uploadedCount = new AtomicInteger(0);

        for (Uri uri : pickedImages) {
            String fileName = System.currentTimeMillis() + "_" + uri.getLastPathSegment();
            StorageReference ref = storage.getReference()
                    .child("books/" + name + "/" + fileName);

            ref.putFile(uri)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful()) throw task.getException();
                        return ref.getDownloadUrl();
                    })
                    .addOnSuccessListener(downloadUrl -> {
                        uploadedUrls.add(downloadUrl.toString());
                        if (uploadedCount.incrementAndGet() == pickedImages.size()) {
                            saveProductToFirestore(name, price, stock, categoryId, uploadedUrls);
                        }
                    })
                    .addOnFailureListener(e -> {
                        toast("❌ Lỗi upload ảnh: " + e.getMessage());
                    });
        }
    }

    private void saveProductToFirestore(String name, long price, long stock,
                                        long categoryId, List<String> imageUrls) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("productName", name);
        doc.put("author", empty(edtAuthor));
        doc.put("price", price);
        doc.put("stock", stock);
        doc.put("categoryId", categoryId);
        doc.put("isbn", empty(edtIsbn));
        doc.put("imageBase64", imageUrls); // ⚠ giữ nguyên field cũ để code khác không cần sửa
        doc.put("briefDescription", empty(edtBrief));
        doc.put("fullDescription", empty(edtFull));
        doc.put("technicalSpecifications", empty(edtSpecs));
        doc.put("createdAt", System.currentTimeMillis());

        adminService.addProduct(doc, res -> {
            if (!res.isSuccess()) {
                toast("Lỗi thêm: " + res.getMessage());
                return;
            }
            toast("✅ Đã thêm sản phẩm thành công!");
            finish();
        });
    }

    private long parseLongOr(String s, long fb) {
        try { return TextUtils.isEmpty(s) ? fb : Long.parseLong(s); }
        catch (Exception e) { return fb; }
    }

    private String t(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private String empty(TextInputEditText e) {
        return TextUtils.isEmpty(t(e)) ? null : t(e);
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
