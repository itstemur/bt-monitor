package com.tz.btmonitor;

import android.app.Application;

public class BluetoothApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SharedPref.init(this);
    }
}
