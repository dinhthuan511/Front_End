package com.example.book_store_mobileapp;


import java.text.DecimalFormat;

public class FormatUtils {

    public static String formatCurrency(double amount) {
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(amount) + " VND";
    }
}