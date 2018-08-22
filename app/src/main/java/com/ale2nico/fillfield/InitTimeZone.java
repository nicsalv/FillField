package com.ale2nico.fillfield;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.jakewharton.threetenabp.AndroidThreeTen;

public class InitTimeZone extends MultiDexApplication {

    private String activeTAG ;
    @Override
    public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}
