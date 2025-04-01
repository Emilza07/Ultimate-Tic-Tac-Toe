package com.emil_z.repository.BASE;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class FirebaseInstance {
    private static volatile FirebaseInstance _instance = null;
    public static FirebaseApp app;

    private FirebaseInstance(Context context) {
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setProjectId("ultimate-tic-tac-toe-project")
                .setApplicationId("ultimate-tic-tac-toe-project")
                .setApiKey("AIzaSyCqKjSsYdTE9JuwOTWBxpm55qxTiUFNQxs")
                .setStorageBucket("ultimate-tic-tac-toe-project.firebasestorage.app")
                .build();

        app = FirebaseApp.initializeApp(context, options);
    }

    public static FirebaseInstance instance(Context context) {
        if (_instance == null) {  // 1st check
            synchronized (FirebaseInstance.class) {
                if (_instance == null){ // 2nd check
                    _instance = new FirebaseInstance(context);
                }
            }
        }

        return _instance;
    }
}