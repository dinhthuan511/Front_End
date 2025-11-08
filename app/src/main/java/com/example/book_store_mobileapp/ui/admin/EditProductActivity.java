package com.example.book_store_mobileapp.ui.admin;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.book_store_mobileapp.R;
import com.example.book_store_mobileapp.network.FirebaseAdminService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditProductActivity extends AppCompatActivity {

    private static final int MAX_IMAGE_BYTES = 900 * 1024;
    private static final int MAX_DIMENSION   = 1200;
    private static final int JPEG_QUALITY    = 85;

    private ImageView ivPreview;
    private FloatingActionButton btnPickImage;
    private android.widget.Button btnRemoveImage;
    private android.widget.Button btnSave;
    private android.widget.Button btnDelete;

    private TextInputEditText edtProductName, edtAuthor, edtPrice, edtStock,
            edtCategoryId, edtIsbn, edtBrief, edtFull, edtSpecs;

    private String productId;
    private String currentImage; // có thể là base64 hoặc data-url
    private Uri pickedImageUri = null;
    private boolean removeImage = false;

    private final FirebaseAdminService adminService = new FirebaseAdminService();
    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        MaterialToolbar tb = findViewById(R.id.topAppBar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tb.setNavigationOnClickListener(v -> finish());

        bindViews();
        setupFormatter();

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        pickedImageUri = uri;
                        removeImage = false;
                        Glide.with(this).load(uri).centerCrop().into(ivPreview);
                    }
                });
        btnPickImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        btnRemoveImage.setOnClickListener(v -> {
            pickedImageUri = null;
            currentImage = null;
            removeImage = true;
            ivPreview.setImageDrawable(null);
            toast("Đã bỏ ảnh, khi lưu sẽ xoá ảnh");
        });

        btnSave.setOnClickListener(v -> saveChanges());
        btnDelete.setOnClickListener(v -> new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xoá sản phẩm?")
                .setMessage("Thao tác không thể hoàn tác.")
                .setPositiveButton("Xoá", (d, w) ->
                        adminService.deleteProduct(productId, res -> {
                            if (res.isSuccess()) { toast("Đã xoá"); finish(); }
                            else toast("Lỗi: " + res.getMessage());
                        })
                )
                .setNegativeButton("Huỷ", null)
                .show());

        productId = getIntent().getStringExtra("productId");
        if (TextUtils.isEmpty(productId)) { toast("Thiếu productId"); finish(); return; }

        prefillFromIntent();
        fetchLatestFromFirestore();
    }

    private void bindViews() {
        ivPreview      = findViewById(R.id.ivPreview);
        btnPickImage   = findViewById(R.id.btnPickImage);
        btnRemoveImage = findViewById(R.id.btnRemoveImage);
        btnSave        = findViewById(R.id.btnSave);
        btnDelete      = findViewById(R.id.btnDelete);

        edtProductName = findViewById(R.id.edtProductName);
        edtAuthor      = findViewById(R.id.edtAuthor);
        edtPrice       = findViewById(R.id.edtPrice);
        edtStock       = findViewById(R.id.edtStock);
        edtCategoryId  = findViewById(R.id.edtCategoryId);
        edtIsbn        = findViewById(R.id.edtIsbn);
        edtBrief       = findViewById(R.id.edtBrief);
        edtFull        = findViewById(R.id.edtFull);
        edtSpecs       = findViewById(R.id.edtSpecs);
    }

    private void setupFormatter() {
        edtPrice.addTextChangedListener(new TextWatcher() {
            private String current = "";
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    edtPrice.removeTextChangedListener(this);
                    String clean = s.toString().replace(".", "");
                    if (!clean.isEmpty()) {
                        try {
                            String f = NumberFormat.getInstance(new Locale("vi","VN")).format(Long.parseLong(clean));
                            current = f; edtPrice.setText(f); edtPrice.setSelection(f.length());
                        } catch (NumberFormatException ignore) {}
                    }
                    edtPrice.addTextChangedListener(this);
                }
            }
        });
    }

    private void prefillFromIntent() {
        setIfNotNull(edtProductName, getIntent().getStringExtra("productName"));
        setIfNotNull(edtAuthor, getIntent().getStringExtra("author"));
        setIfNotNull(edtIsbn, getIntent().getStringExtra("isbn"));
        setIfNotNull(edtBrief, getIntent().getStringExtra("briefDescription"));
        setIfNotNull(edtFull, getIntent().getStringExtra("fullDescription"));
        setIfNotNull(edtSpecs, getIntent().getStringExtra("technicalSpecifications"));

        long price = getIntent().getLongExtra("price", 0);
        if (price > 0)
            edtPrice.setText(NumberFormat.getInstance(new Locale("vi","VN")).format(price));

        long stock = getIntent().getLongExtra("stock", 0);
        if (stock >= 0)
            edtStock.setText(String.valueOf(stock));

        long cat = getIntent().getLongExtra("categoryId", 0);
        if (cat > 0)
            edtCategoryId.setText(String.valueOf(cat));

        // hỗ trợ cả base64 và imageUrl
        String url = getIntent().getStringExtra("imageUrl");
        if (TextUtils.isEmpty(url))
            url = getIntent().getStringExtra("imageBase64");

        if (!TextUtils.isEmpty(url)) {
            if (!url.startsWith("http") && !url.startsWith("data:"))
                url = "data:image/jpeg;base64," + url;
            currentImage = url;
            Glide.with(this).load(currentImage).centerCrop().into(ivPreview);
        }
    }

    private void fetchLatestFromFirestore() {
        FirebaseFirestore.getInstance().collection("products").document(productId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) { toast("Không tìm thấy sản phẩm"); finish(); return; }

                    setIfNotNull(edtProductName, doc.getString("productName"));
                    setIfNotNull(edtAuthor, doc.getString("author"));
                    setIfNotNull(edtIsbn, doc.getString("isbn"));
                    setIfNotNull(edtBrief, doc.getString("briefDescription"));
                    setIfNotNull(edtFull, doc.getString("fullDescription"));
                    setIfNotNull(edtSpecs, doc.getString("technicalSpecifications"));

                    Long price = doc.getLong("price");
                    if (price != null && price > 0)
                        edtPrice.setText(NumberFormat.getInstance(new Locale("vi","VN")).format(price));

                    Long stock = doc.getLong("stock");
                    if (stock != null)
                        edtStock.setText(String.valueOf(stock));

                    Long catId = doc.getLong("categoryId");
                    if (catId != null && catId > 0)
                        edtCategoryId.setText(String.valueOf(catId));

                    String img = doc.getString("imageUrl");
                    if (TextUtils.isEmpty(img))
                        img = doc.getString("imageBase64");

                    if (!TextUtils.isEmpty(img) && pickedImageUri == null && !removeImage) {
                        if (!img.startsWith("http") && !img.startsWith("data:"))
                            img = "data:image/jpeg;base64," + img;
                        currentImage = img;
                        Glide.with(this).load(currentImage).centerCrop().into(ivPreview);
                    }
                })
                .addOnFailureListener(e -> toast("Lỗi tải sản phẩm: " + e.getMessage()));
    }

    private void saveChanges() {
        String name = t(edtProductName);
        String priceS = t(edtPrice).replace(".", "");
        String stockS = t(edtStock);
        String catS = t(edtCategoryId);

        if (TextUtils.isEmpty(name)) { toast("Tên không được trống"); return; }
        if (TextUtils.isEmpty(priceS)) { toast("Giá không được trống"); return; }

        long price = parseLongOr(priceS, -1);
        long stock = parseLongOr(stockS, 0);
        long cat = parseLongOr(catS, 0);

        Map<String, Object> up = new HashMap<>();
        up.put("productName", name);
        up.put("author", emptyToNull(t(edtAuthor)));
        up.put("price", price);
        up.put("stock", stock);
        up.put("categoryId", cat == 0 ? null : cat);
        up.put("isbn", emptyToNull(t(edtIsbn)));
        up.put("briefDescription", emptyToNull(t(edtBrief)));
        up.put("fullDescription", emptyToNull(t(edtFull)));
        up.put("technicalSpecifications", emptyToNull(t(edtSpecs)));
        up.put("updatedAt", System.currentTimeMillis());

        if (pickedImageUri != null) {
            String b64 = imageToBase64(pickedImageUri, MAX_DIMENSION, JPEG_QUALITY, MAX_IMAGE_BYTES);
            if (b64 == null) { toast("Ảnh quá lớn hoặc lỗi chuyển ảnh"); return; }
            up.put("imageBase64", b64);
            up.put("imageUrl", "data:image/jpeg;base64," + b64);
        } else if (removeImage) {
            up.put("imageBase64", null);
            up.put("imageUrl", null);
        }

        adminService.updateProduct(productId, up, res -> {
            if (res.isSuccess()) { toast("✅ Đã lưu thay đổi"); finish(); }
            else toast("Lỗi cập nhật: " + res.getMessage());
        });
    }

    private @Nullable String imageToBase64(Uri uri, int maxDim, int quality, int maxBytes) {
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            Bitmap src = BitmapFactory.decodeStream(in);
            if (src == null) return null;

            int w = src.getWidth(), h = src.getHeight(), longSide = Math.max(w, h);
            if (longSide > maxDim) {
                float sc = maxDim * 1f / longSide;
                src = Bitmap.createScaledBitmap(src, Math.round(w * sc), Math.round(h * sc), true);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            src.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            byte[] bytes = baos.toByteArray();
            if (bytes.length > maxBytes) return null;

            return Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (Exception e) {
            return null;
        }
    }

    private void setIfNotNull(TextInputEditText e, @Nullable String v) {
        if (e != null && !TextUtils.isEmpty(v)) e.setText(v);
    }

    private long parseLongOr(String s, long fb) {
        try { return TextUtils.isEmpty(s) ? fb : Long.parseLong(s); }
        catch (Exception e) { return fb; }
    }

    private String t(TextInputEditText e) {
        return (e == null || e.getText() == null) ? "" : e.getText().toString().trim();
    }

    private @Nullable Object emptyToNull(String s) {
        return TextUtils.isEmpty(s) ? null : s;
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }
}