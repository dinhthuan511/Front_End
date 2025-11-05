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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddProductActivity extends AppCompatActivity {

    // Giới hạn để ảnh + các field khác không vượt 1MiB/doc của Firestore
    private static final int MAX_IMAGE_BYTES = 900 * 1024;      // 900 KB
    private static final int MAX_DIMENSION   = 1200;            // px cạnh dài
    private static final int JPEG_QUALITY    = 85;              // %

    private TextInputEditText edtProductName, edtAuthor, edtPrice, edtStock,
            edtCategoryId, edtIsbn, edtBrief, edtFull, edtSpecs;
    private ImageView ivPreview;
    private FloatingActionButton btnPickImage;
    private android.widget.Button btnSave;

    private Uri pickedImageUri = null;
    private FirebaseAdminService adminService;
    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        MaterialToolbar tb = findViewById(R.id.topAppBar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tb.setNavigationOnClickListener(v -> finish());

        adminService = new FirebaseAdminService();
        bindViews();
        setupPriceFormatter();

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        pickedImageUri = uri;
                        // preview
                        Glide.with(this).load(uri).into(ivPreview);
                    }
                });

        btnPickImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        btnSave.setOnClickListener(v -> save());
    }

    private void bindViews() {
        edtProductName = findViewById(R.id.edtProductName);
        edtAuthor      = findViewById(R.id.edtAuthor);
        edtPrice       = findViewById(R.id.edtPrice);
        edtStock       = findViewById(R.id.edtStock);
        edtCategoryId  = findViewById(R.id.edtCategoryId);
        edtIsbn        = findViewById(R.id.edtIsbn);
        edtBrief       = findViewById(R.id.edtBrief);
        edtFull        = findViewById(R.id.edtFull);
        edtSpecs       = findViewById(R.id.edtSpecs);
        ivPreview      = findViewById(R.id.ivPreview);
        btnPickImage   = findViewById(R.id.btnPickImage);
        btnSave        = findViewById(R.id.btnSave);
    }

    private void setupPriceFormatter() {
        edtPrice.addTextChangedListener(new TextWatcher() {
            private String current = "";
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    edtPrice.removeTextChangedListener(this);
                    String clean = s.toString().replace(".", "");
                    if (!clean.isEmpty()) {
                        try {
                            String formatted = NumberFormat.getInstance(new Locale("vi","VN"))
                                    .format(Long.parseLong(clean));
                            current = formatted;
                            edtPrice.setText(formatted);
                            edtPrice.setSelection(formatted.length());
                        } catch (NumberFormatException ignore) {}
                    }
                    edtPrice.addTextChangedListener(this);
                }
            }
        });
    }

    private void save() {
        String name   = t(edtProductName);
        String priceS = t(edtPrice).replace(".", "");
        String stockS = t(edtStock);
        String catS   = t(edtCategoryId);

        if (TextUtils.isEmpty(name))   { toast("Tên sản phẩm không được trống"); return; }
        if (TextUtils.isEmpty(priceS)) { toast("Giá không được trống"); return; }
        if (pickedImageUri == null)    { toast("Chưa chọn ảnh sản phẩm"); return; }

        long price = parseLongOr(priceS, -1);
        if (price <= 0) { toast("Giá phải > 0"); return; }

        long stock = parseLongOr(stockS, 0);
        if (stock < 0)  { toast("Tồn kho phải ≥ 0"); return; }

        long categoryId = parseLongOr(catS, 0);

        // chuyển ảnh -> base64 và lưu Firestore
        String b64 = imageToBase64(pickedImageUri, MAX_DIMENSION, JPEG_QUALITY, MAX_IMAGE_BYTES);
        if (b64 == null) { toast("Ảnh quá lớn hoặc lỗi chuyển ảnh"); return; }

        Map<String, Object> doc = new HashMap<>();
        doc.put("productName", name);
        doc.put("author", empty(edtAuthor));
        doc.put("price", price);
        doc.put("stock", stock);
        doc.put("categoryId", categoryId);
        doc.put("isbn", empty(edtIsbn));
        doc.put("imageBase64", b64);
        doc.put("briefDescription", empty(edtBrief));
        doc.put("fullDescription", empty(edtFull));
        doc.put("technicalSpecifications", empty(edtSpecs));
        doc.put("createdAt", System.currentTimeMillis());

        adminService.addProduct(doc, res -> {
            if (!res.isSuccess()) {
                toast("Lỗi thêm: " + res.getMessage());
                return;
            }
            toast("✅ Đã thêm sản phẩm");
            finish();
        });
    }

    /** Convert Uri -> base64 (JPEG), có resize + giới hạn kích thước */
    private @Nullable String imageToBase64(Uri uri, int maxDim, int quality, int maxBytes) {
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            Bitmap src = BitmapFactory.decodeStream(in);
            if (src == null) return null;

            Bitmap bmp = downscaleIfNeeded(src, maxDim);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            byte[] bytes = baos.toByteArray();

            if (bytes.length > maxBytes) return null; // quá to

            return Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (Exception e) {
            return null;
        }
    }

    private Bitmap downscaleIfNeeded(Bitmap src, int maxDim) {
        int w = src.getWidth(), h = src.getHeight();
        int longSide = Math.max(w, h);
        if (longSide <= maxDim) return src;
        float scale = (float) maxDim / longSide;
        int nw = Math.round(w * scale), nh = Math.round(h * scale);
        return Bitmap.createScaledBitmap(src, nw, nh, true);
    }

    private long parseLongOr(String s, long fallback) {
        try { return TextUtils.isEmpty(s) ? fallback : Long.parseLong(s); }
        catch (Exception ignore) { return fallback; }
    }

    private String t(TextInputEditText e){ return e.getText()==null? "": e.getText().toString().trim(); }
    private String empty(TextInputEditText e){ return TextUtils.isEmpty(t(e)) ? null : t(e); }
    private void toast(String m){ Toast.makeText(this, m, Toast.LENGTH_SHORT).show(); }
}
