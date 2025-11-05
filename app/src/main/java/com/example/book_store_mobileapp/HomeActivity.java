package com.example.book_store_mobileapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
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

import com.example.book_store_mobileapp.ui.auth.LoginActivity;
import com.example.book_store_mobileapp.ui.auth.ProfileActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends BaseActivity implements OnMapReadyCallback {

    private GoogleMap gMap;
    private final Map<String, LatLng> storeLocation = new HashMap<>(); // Tọa độ cửa hàng
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

        initializeStoreLocations();

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
            if(FirebaseAuth.getInstance().getCurrentUser() == null)
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            else{
                startActivity(new Intent(HomeActivity.this, CartActivity.class));
            }
        });

        cardNotification.setOnClickListener(v -> {
            if(FirebaseAuth.getInstance().getCurrentUser() == null)
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            else{
                startActivity(new Intent(HomeActivity.this, NotificationCenterActivity.class));}
        });

        cardProfile.setOnClickListener(v -> {
            if(FirebaseAuth.getInstance().getCurrentUser() == null)
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            else{
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
            }
        });
    }

    // Dữ liệu vị trí Store
    private void initializeStoreLocations() {
        storeLocation.put("Chi nhánh HCM", new LatLng(10.841348873595665, 106.81025314114362));
        storeLocation.put("Chi nhánh HN", new LatLng(21.01253686535662, 105.52568616343122));
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

        if (storeLocation.isEmpty()) {
            Toast.makeText(this, "Không có dữ liệu vị trí cửa hàng.", Toast.LENGTH_SHORT).show();
            return;
        }

        LatLngBounds.Builder boundsBUilder = new LatLngBounds.Builder();
        for (Map.Entry<String, LatLng> entry : storeLocation.entrySet()){
            String storeName = entry.getKey();
            LatLng location = entry.getValue();

            // Thêm một điểm đánh dấu tại vị trí cửa hàng và di chuyển camera
            gMap.addMarker(new MarkerOptions().position(location).title(storeName));
            boundsBUilder.include(location);
        }

        // Di chuyển camera để hiển thị tất cả các điểm đánh dấu
        LatLngBounds bounds = boundsBUilder.build();
        int padding = 150;
        gMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding)); // Zoom gần hơn một chút

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
        // Kiểm tra xem có cửa hàng nào không
        if (storeLocation.isEmpty()) {
            Toast.makeText(this, "Không có vị trí cửa hàng nào để chỉ đường.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Nếu chỉ có một cửa hàng, chỉ đường trực tiếp đến đó.
        if (storeLocation.size() == 1) {
            // Lấy ra cửa hàng
            Map.Entry<String, LatLng> singleEntry = storeLocation.entrySet().iterator().next();
            LatLng location = singleEntry.getValue();
            String storeName = singleEntry.getKey();

            // Tạo Uri với tọa độ và tên để hiển thị trên bản đồ
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + location.latitude + "," + location.longitude);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

            // Thử khởi động Intent, nếu không có app bản đồ sẽ báo lỗi
            try {
                startActivity(mapIntent);
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, "Không tìm thấy ứng dụng bản đồ nào.", Toast.LENGTH_SHORT).show();
            }

        } else {
            // Nếu có nhiều hơn một cửa hàng, yêu cầu người dùng chọn
            Toast.makeText(this, "Vui lòng nhấn vào một cửa hàng trên bản đồ để được chỉ đường.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected int getNavigationMenuItemId() {
        return R.id.nav_home;
    }
}
