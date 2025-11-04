package com.example.book_store_mobileapp;

import android.app.Application;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.Environment;

public class ZaloPaySDKInit extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ZaloPaySDK.init(AppInfo.APP_ID, Environment.SANDBOX);
    }
}
