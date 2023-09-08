package com.gpp.dmr_print.app;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class Myapp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}
