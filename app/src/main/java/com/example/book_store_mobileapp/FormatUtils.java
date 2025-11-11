package com.example.book_store_mobileapp;


import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class FormatUtils {

    public static String formatCurrency(Double value) {
        if (value == null) return "0 ₫";
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(value);
    }

}