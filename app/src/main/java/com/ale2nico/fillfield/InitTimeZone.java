package com.ale2nico.fillfield;

import android.app.Application;

import com.jakewharton.threetenabp.AndroidThreeTen;

public class InitTimeZone extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);
    }
}
