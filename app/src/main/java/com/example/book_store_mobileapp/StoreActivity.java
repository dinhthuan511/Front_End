package com.example.book_store_mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.book_store_mobileapp.adapter.BookAdapter;
import com.example.book_store_mobileapp.data.Book;
import com.example.book_store_mobileapp.data.BookCategory;
import com.example.book_store_mobileapp.data.BookFilter;
import com.example.book_store_mobileapp.network.CartCountRepository;
import com.example.book_store_mobileapp.network.FirebaseBookService;
import com.example.book_store_mobileapp.ui.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class StoreActivity extends BaseActivity {

    private GridView gridView;
    private ProgressBar progressBar;
    private EditText txtSearchName;
    private ImageButton btnCart, btnFilter, btnSort, btnChatFloating;
    private BookAdapter bookAdapter;
    private BookFilter bookFilter;
    private List<Book> initialBookList = new ArrayList<>();
    private List<Book> displayedBookList = new ArrayList<>();
    private String currentSearchQuery = "";
    private int activePriceFilter = -1;
    private List<Long> activeCategoryFilters = new ArrayList<>();

    private FrameLayout cartBtnContainer;
    private TextView cartBadge;
    private final CartCountRepository.CartCountCallback cartCountCallback = count -> runOnUiThread(() -> {
        if (cartBadge == null) return;
        if (count > 0) {
            cartBadge.setText(String.valueOf(count > 99 ? "99+" : count));
            cartBadge.setVisibility(View.VISIBLE);
        } else {
            cartBadge.setVisibility(View.GONE);
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_store);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ✅ Khởi tạo view
        btnCart = findViewById(R.id.btnCart);
        cartBtnContainer = findViewById(R.id.cart_btn_container);
        btnFilter = findViewById(R.id.btnFilter);
        btnSort = findViewById(R.id.btnSort);
        txtSearchName = findViewById(R.id.txtSearchName);
        gridView = findViewById(R.id.grid_view);
        progressBar = findViewById(R.id.progressBar);
        btnChatFloating = findViewById(R.id.btnChatFloating);



        // ✅ Giỏ hàng
        btnCart.setOnClickListener(v -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                // Chuyển hướng đến trang đăng nhập
                Intent intent = new Intent(StoreActivity.this, LoginActivity.class);
                startActivity(intent);
            } else {
                // Chuyển hướng đến giỏ hàng
                Intent intent = new Intent(StoreActivity.this, CartActivity.class);
                startActivity(intent);
            }

        });
        // Setup simple top-right cart badge TextView
        cartBadge = findViewById(R.id.top_cart_badge);
        cartBadge.setVisibility(View.GONE);
        // ✅ Bộ lọc & sắp xếp
        bookFilter = new BookFilter();

        // Name search
        txtSearchName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString();
                applyFiltersAndSearch();
            }
        });

        // Sorting
        btnSort.setOnClickListener(v -> {
            final CharSequence[] options = {"Mặc định", "Giá thấp đến cao", "Giá cao đến thấp"};
            new AlertDialog.Builder(StoreActivity.this)
                    .setTitle("Sắp xếp theo")
                    .setItems(options, (dialog, item) -> {
                        bookFilter.sortBooksByPrice(displayedBookList, item);
                        bookAdapter.notifyDataSetChanged();
                    })
                    .show();
        });

        // Filtering
        btnFilter.setOnClickListener(v -> showFilterDialog());
//        btnFilter.setOnClickListener(v -> {
//            final CharSequence[] options = {"Under 100,000 VNĐ", "100,000 - 200,000 VNĐ", "Over 200,000 VNĐ", "Clear Filter"};
//            new AlertDialog.Builder(StoreActivity.this)
//                    .setTitle("Filter by Price Range")
//                    .setItems(options, (dialog, item) -> {
//                        if (item == 3) activePriceFilter = -1;
//                        else activePriceFilter = item;
//                        applyFiltersAndSearch();
//                    })
//                    .show();
//        });

        bookAdapter = new BookAdapter(this, displayedBookList);
        gridView.setAdapter(bookAdapter);

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Book selectedBook = displayedBookList.get(position);
            Intent intent = new Intent(StoreActivity.this, BookDetailActivity.class);
            intent.putExtra("SELECTED_BOOK", selectedBook);
            startActivity(intent);
        });

        fetchBooksFromFirebase();
    }

    private void showFilterDialog() {
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.book_filter, null);

        final RadioGroup rgPriceFilter = dialogView.findViewById(R.id.rg_price_filter);
        final LinearLayout llCategoryCheckboxes = dialogView.findViewById(R.id.ll_category_checkboxes);

        // --- Thiết lập trạng thái hiện tại cho các nút lọc ---
        if (activePriceFilter != -1) {
            if (activePriceFilter == 0) rgPriceFilter.check(R.id.rb_price_1);
            else if (activePriceFilter == 1) rgPriceFilter.check(R.id.rb_price_2);
            else if (activePriceFilter == 2) rgPriceFilter.check(R.id.rb_price_3);
        }

        // --- Tạo động các CheckBox cho thể loại ---
        FirebaseBookService.getInstance().getAllCategories(new FirebaseBookService.FirestoreCallback<List<BookCategory>>() {
            @Override
            public void onSuccess(List<BookCategory> categories) {
                llCategoryCheckboxes.removeAllViews(); // Xóa các checkbox cũ

                if (categories == null || categories.isEmpty()) {
                    Toast.makeText(StoreActivity.this, "Không có thể loại nào.", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (BookCategory category : categories) {
                    try {
                        // Chuyển đổi ID từ String (Firestore doc ID) sang Long để so sánh với categoryId trong Book
                        long categoryId = Long.parseLong(category.getId());
                        CheckBox cb = new CheckBox(StoreActivity.this);
                        cb.setText(category.getCategoryName());
                        cb.setTag(categoryId); // Gắn ID (dạng Long) vào tag để lấy lại sau
                        // Kiểm tra xem thể loại này đã được chọn trong lần lọc trước chưa
                        if (activeCategoryFilters.contains(categoryId)) {
                            cb.setChecked(true);
                        }
                        llCategoryCheckboxes.addView(cb);
                    } catch (NumberFormatException e) {
                        // Ghi log và bỏ qua nếu ID của category trong Firestore không phải là một số
                        System.err.println("Lỗi định dạng ID thể loại: " + category.getId());
                    }
                }
            }
            @Override
            public void onError(String message) {
                // Hiển thị lỗi cho người dùng nếu không tải được thể loại
                Toast.makeText(StoreActivity.this, "Lỗi tải thể loại: " + message, Toast.LENGTH_SHORT).show();
            }
        });

        // --- Xây dựng và hiển thị Dialog ---
        new AlertDialog.Builder(this)
                .setTitle("Bộ lọc")
                .setView(dialogView)
                .setNeutralButton("Xóa bộ lọc", (dialog, which) -> {
                    activePriceFilter = -1;
                    activeCategoryFilters.clear();
                    applyFiltersAndSearch();
                    Toast.makeText(this, "Đã xóa bộ lọc", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Áp dụng", (dialog, which) -> {
                    // 1. Lấy giá trị lọc giá
                    int selectedPriceId = rgPriceFilter.getCheckedRadioButtonId();
                    if (selectedPriceId == R.id.rb_price_0) activePriceFilter = -1;
                    else if (selectedPriceId == R.id.rb_price_1) activePriceFilter = 0;
                    else if (selectedPriceId == R.id.rb_price_2) activePriceFilter = 1;
                    else if (selectedPriceId == R.id.rb_price_3) activePriceFilter = 2;
                    else activePriceFilter = -1;

                    // 2. Lấy giá trị lọc thể loại
                    activeCategoryFilters.clear();
                    for (int i = 0; i < llCategoryCheckboxes.getChildCount(); i++) {
                        View child = llCategoryCheckboxes.getChildAt(i);
                        if (child instanceof CheckBox) {
                            CheckBox cb = (CheckBox) child;
                            if (cb.isChecked()) {
                                activeCategoryFilters.add((Long) cb.getTag());
                            }
                        }
                    }

                    applyFiltersAndSearch();
                })
                .show();
    }

    private void applyFiltersAndSearch() {
        List<Book> filteredList = new ArrayList<>(initialBookList);

        if (activePriceFilter != -1) {
            if (activePriceFilter == 0) { // Under 100,000
                filteredList = bookFilter.filterBooksByPriceRange(filteredList, 0, 99999);
            } else if (activePriceFilter == 1) { // 100,000 - 200,000
                filteredList = bookFilter.filterBooksByPriceRange(filteredList, 100000, 200000);
            } else if (activePriceFilter == 2) { // Over 200,000
                filteredList = bookFilter.filterBooksByPriceRange(filteredList, 200001, Double.MAX_VALUE);
            }
        }

        if (!activeCategoryFilters.isEmpty()) {
            filteredList = bookFilter.filterBooksByCategories(filteredList, activeCategoryFilters);
        }

        if (!currentSearchQuery.isEmpty()) {
            filteredList = bookFilter.searchBooks(filteredList, currentSearchQuery);
        }

        updateDisplayedBooks(filteredList);
    }

    private void updateDisplayedBooks(List<Book> bookList) {
        displayedBookList.clear();
        displayedBookList.addAll(bookList);
        bookAdapter.notifyDataSetChanged();
    }

    /**
     * 🟢 Load sách từ Firestore thay cho MockAPI
     */
    private void fetchBooksFromFirebase() {
        progressBar.setVisibility(View.VISIBLE);

        FirebaseBookService.getInstance().getAllBooks(new FirebaseBookService.FirestoreCallback<List<Book>>() {
            @Override
            public void onSuccess(List<Book> data) {
                progressBar.setVisibility(View.GONE);
                initialBookList.clear();
                initialBookList.addAll(data);
                updateDisplayedBooks(initialBookList);
            }

            @Override
            public void onError(String message) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(StoreActivity.this, "Lỗi tải sách: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Override phương thức này để cho BaseActivity biết cần highlight mục nào
    @Override
    protected int getNavigationMenuItemId() {
        return R.id.nav_store;
    }
}