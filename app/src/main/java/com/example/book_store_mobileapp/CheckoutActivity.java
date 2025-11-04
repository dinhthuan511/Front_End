package com.example.book_store_mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.book_store_mobileapp.adapter.CheckoutAdapter;
import com.example.book_store_mobileapp.data.CartItem;
import org.json.JSONObject;

import java.util.ArrayList;

public class CheckoutActivity extends AppCompatActivity {

    private ArrayList<CartItem> cartItems;
    private TextView txtTotal;
    private EditText edtName, edtPhone, edtAddress;
    private RadioGroup paymentMethodGroup;
    private Button btnConfirm;
    private ImageButton btnBack;
    private double total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // views
        txtTotal = findViewById(R.id.txtTotal);
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        paymentMethodGroup = findViewById(R.id.paymentMethodGroup);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnBack = findViewById(R.id.btnBack);

        RecyclerView recyclerView = findViewById(R.id.recyclerCheckout);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // get cart from Intent
        cartItems = getIntent().getParcelableArrayListExtra("cartItems");
        if (cartItems == null) cartItems = new ArrayList<>();

        CheckoutAdapter adapter = new CheckoutAdapter(this, cartItems);
        recyclerView.setAdapter(adapter);

        // calc total
        total = 0;
        for (CartItem item : cartItems) {
            total += item.getBook().getPrice() * item.getQuantity();
        }
        txtTotal.setText("Tổng cộng: " + FormatUtils.formatCurrency(total));

        btnBack.setOnClickListener(v -> finish());
        btnConfirm.setOnClickListener(v -> handleConfirm());
    }

    private void handleConfirm() {
        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedMethodId  = paymentMethodGroup.getCheckedRadioButtonId();
        if (selectedMethodId  == -1) {
            Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedMethod = findViewById(selectedMethodId );
        String method = selectedMethod.getText().toString();


        if (method.contains("ZaloPay")) {
            Log.d("CHECKOUT", "🟢 Bắt đầu gọi createOrderAndStartPayment()");
            createOrderAndStartPayment(name, phone, address);
        }
        else if (method.contains("Tiền mặt")) {
            Log.d("CHECKOUT", "💰 Thanh toán tiền mặt - lưu đơn hàng ngay");
            OrderRepository.saveOrder(
                    this,
                    cartItems,
                    total,
                    name,
                    phone,
                    address,
                    "Tiền mặt",
                    "CHƯA THANH TOÁN",
                    null,
                    null,
                    null
            );
            Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Phương thức này hiện chưa được hỗ trợ!", Toast.LENGTH_SHORT).show();
        }
    }


    private void createOrderAndStartPayment(String name, String phone, String address) {
        new Thread(() -> {
            try {
                Log.d("CreateOrder", "🔹 Bắt đầu tạo đơn hàng ZaloPay...");
                CreateOrder createOrder = new CreateOrder();
                long amount = Math.round(total); // Ép kiểu double → long, đảm bảo chính xác
                JSONObject resp = createOrder.createOrder(String.valueOf(amount));

                Log.d("CreateOrder", "✅ Response từ ZaloPay: " + resp.toString());

                int returnCode = resp.optInt("return_code", -1);
                if (returnCode == 1) {
                    String zpTransToken = resp.optString("zp_trans_token", null);
                    Log.d("CreateOrder", "🎯 zp_trans_token = " + zpTransToken);
                    runOnUiThread(() -> {
                        Log.d("CREATE_ORDER", "zp_trans_token gửi sang ZaloPayActivity: " + zpTransToken);
                        Intent intent = new Intent(CheckoutActivity.this, ZaloPayActivity.class);
                        intent.putParcelableArrayListExtra("cartItems", cartItems);
                        intent.putExtra("total", amount);
                        intent.putExtra("name", name);
                        intent.putExtra("phone", phone);
                        intent.putExtra("address", address);
                        intent.putExtra("zp_trans_token", zpTransToken);
                        startActivity(intent);
                    });
                } else {
                    final String msg = resp.optString("return_message", "Tạo đơn lỗi");
                    Log.e("CreateOrder", "❌ Tạo đơn thất bại: " + msg);
                    runOnUiThread(() ->
                            Toast.makeText(CheckoutActivity.this, "Tạo đơn thất bại: " + msg, Toast.LENGTH_LONG).show()
                    );
                }
            } catch (Exception e) {
                Log.e("CreateOrder", "🔥 Lỗi tạo đơn: " + e.getMessage(), e);
                runOnUiThread(() ->
                        Toast.makeText(CheckoutActivity.this, "Lỗi tạo đơn: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }
}
