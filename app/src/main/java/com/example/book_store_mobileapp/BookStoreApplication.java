package com.example.book_store_mobileapp;

import android.app.Application;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

public class BookStoreApplication extends Application implements LifecycleObserver {

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Create notification channel on app start
        NotificationHelper.createCartChannel(this);
        
        // Register lifecycle observer to track app background/foreground
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackgrounded() {
        // App goes to background - update system notification with current cart count
        NotificationHelper.updateCartSystemNotification(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onAppForegrounded() {
        // App comes to foreground - keep system notification (don't cancel it)
        // User can still see cart count in pull-down notification
    }
}
