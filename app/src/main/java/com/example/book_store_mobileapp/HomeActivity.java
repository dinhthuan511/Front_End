package com.example.book_store_mobileapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View; // Thêm import này
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView; // Thêm import này
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeActivity extends BaseActivity implements OnMapReadyCallback {

    private GoogleMap gMap;
    private final LatLng storeLocation = new LatLng(10.852903, 106.629555); // Tọa độ cửa hàng
    private CardView cardStore, cardCart, cardNotification, cardProfile;

    // Launcher để xử lý kết quả yêu cầu quyền
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    enableMyLocation();
                } else {
                    Toast.makeText(this, "Bạn cần cấp quyền vị trí để xem vị trí hiện tại.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ánh xạ view
        cardStore = findViewById(R.id.card_store);
        cardCart = findViewById(R.id.card_cart);
        cardNotification = findViewById(R.id.card_notification);
        cardProfile = findViewById(R.id.card_profile);


        // Lấy SupportMapFragment và nhận thông báo khi bản đồ sẵn sàng
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Xử lý sự kiện nhấn nút chỉ đường
        FloatingActionButton fabDirections = findViewById(R.id.fab_directions);
        fabDirections.setOnClickListener(view -> getDirections());

        // Gán sự kiện click cho các CardView
        setupCardClickListeners();
    }

    // Gán sự kiện click
    private void setupCardClickListeners() {
        cardStore.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, StoreActivity.class));
        });

        cardCart.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, CartActivity.class));
        });

        cardNotification.setOnClickListener(v -> {;
            Toast.makeText(this, "Chức năng Thông báo sẽ sớm được cập nhật!", Toast.LENGTH_SHORT).show();
        });

        cardProfile.setOnClickListener(v -> {;
            Toast.makeText(this, "Chức năng Hồ sơ sẽ sớm được cập nhật!", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Được gọi khi bản đồ đã sẵn sàng để sử dụng.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;

        // Tùy chỉnh giao diện bản đồ
        gMap.getUiSettings().setZoomControlsEnabled(true);
        gMap.getUiSettings().setCompassEnabled(false);

        // Thêm một điểm đánh dấu tại vị trí cửa hàng và di chuyển camera
        gMap.addMarker(new MarkerOptions().position(storeLocation).title("Book Store"));
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(storeLocation, 16f)); // Zoom gần hơn một chút

        checkLocationPermission();
    }

    /**
     * Kiểm tra quyền truy cập vị trí.
     */
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Kích hoạt layer hiển thị vị trí của người dùng trên bản đồ.
     */
    private void enableMyLocation() {
        if (gMap != null && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            gMap.setMyLocationEnabled(true);
            gMap.getUiSettings().setMyLocationButtonEnabled(true); // Hiển thị nút vị trí của tôi
        }
    }

    /**
     * Mở ứng dụng Google Maps để chỉ đường đến cửa hàng.
     */
    private void getDirections() {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + storeLocation.latitude + "," + storeLocation.longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Toast.makeText(this, "Không tìm thấy ứng dụng Google Maps.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected int getNavigationMenuItemId() {
        return R.id.nav_home;
    }
}
