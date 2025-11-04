package com.example.book_store_mobileapp.network;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Singleton that manages one Firestore snapshot listener for the current user's cart items
 * and forwards the aggregated count to registered callbacks.
 */
public class CartCountRepository {
    private static final String TAG = "CartCountRepository";
    private static CartCountRepository instance;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private final List<CartCountCallback> listeners = new CopyOnWriteArrayList<>();
    private ListenerRegistration registration;

    private final FirebaseAuth.AuthStateListener authStateListener = firebaseAuth -> {
        // restart listener if auth state changes
        restartListenerIfNeeded();
    };

    public interface CartCountCallback {
        void onCartCount(int count);
    }

    private CartCountRepository() {
        auth.addAuthStateListener(authStateListener);
    }

    public static synchronized CartCountRepository getInstance() {
        if (instance == null) instance = new CartCountRepository();
        return instance;
    }

    public void registerListener(CartCountCallback cb) {
        if (cb == null) return;
        listeners.add(cb);
        // deliver an immediate one-time count to the newly registered callback
        deliverCurrentCountOnce(cb);

        // ensure the persistent snapshot listener is running
        if (listeners.size() == 1) {
            startSnapshotListener();
        }
    }

    public void unregisterListener(CartCountCallback cb) {
        if (cb == null) return;
        listeners.remove(cb);
        if (listeners.isEmpty()) stopSnapshotListener();
    }

    private void restartListenerIfNeeded() {
        if (!listeners.isEmpty()) {
            stopSnapshotListener();
            startSnapshotListener();
        }
    }

    private CollectionReference getCartRefForCurrentUser() {
        if (auth.getCurrentUser() == null) return null;
        return db.collection("carts").document(auth.getCurrentUser().getUid()).collection("items");
    }

    private void startSnapshotListener() {
        CollectionReference cartRef = getCartRefForCurrentUser();
        if (cartRef == null) {
            notifyAllListeners(0);
            return;
        }

        registration = cartRef.addSnapshotListener((value, error) -> {
            if (error != null || value == null) {
                notifyAllListeners(0);
                return;
            }

            int total = 0;
            for (DocumentSnapshot doc : value.getDocuments()) {
                Long q = doc.getLong("quantity");
                if (q != null) total += q.intValue();
            }
            notifyAllListeners(total);
        });
    }

    private void deliverCurrentCountOnce(CartCountCallback cb) {
        CollectionReference cartRef = getCartRefForCurrentUser();
        if (cartRef == null) {
            cb.onCartCount(0);
            return;
        }
        cartRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                int total = 0;
                for (DocumentSnapshot doc : task.getResult()) {
                    Long q = doc.getLong("quantity");
                    if (q != null) total += q.intValue();
                }
                cb.onCartCount(total);
            } else {
                cb.onCartCount(0);
            }
        });
    }

    private void stopSnapshotListener() {
        if (registration != null) {
            registration.remove();
            registration = null;
        }
    }

    private void notifyAllListeners(int count) {
        for (CartCountCallback cb : listeners) {
            try {
                cb.onCartCount(count);
            } catch (Exception e) {
                Log.w(TAG, "listener threw", e);
            }
        }
    }

    /**
     * Cleanup for tests or process shutdown
     */
    public void cleanup() {
        stopSnapshotListener();
        auth.removeAuthStateListener(authStateListener);
        listeners.clear();
        instance = null;
    }

    /**
     * Force a one-time fetch of current cart count and notify listeners immediately.
     * Useful to call right after performing a write to the cart so UI updates without waiting
     * for the snapshot listener propagation.
     */
    public void refreshCartCount() {
        CollectionReference cartRef = getCartRefForCurrentUser();
        if (cartRef == null) {
            notifyAllListeners(0);
            return;
        }

        cartRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                int total = 0;
                for (DocumentSnapshot doc : task.getResult()) {
                    Long q = doc.getLong("quantity");
                    if (q != null) total += q.intValue();
                }
                notifyAllListeners(total);
            } else {
                notifyAllListeners(0);
            }
        });
    }
}
