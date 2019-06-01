package com.example.pharmaliv;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import mumayank.com.airlocationlibrary.AirLocation;

public class GPSService extends Service {

    AirLocation airLocation;

    public GPSService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
