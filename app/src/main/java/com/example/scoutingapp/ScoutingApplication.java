package com.example.scoutingapp;

import android.app.Application;

import com.example.scoutingapp.data.scout.PendingUploadStore;
import com.example.scoutingapp.sync.SyncManager;

/**
 * Wires up the offline-first infrastructure once, at process start, before any Activity or
 * ViewModel runs:
 *  - PendingUploadStore hydrates any match submissions still waiting to be uploaded.
 *  - SyncManager starts listening for Wi-Fi so those uploads happen automatically.
 */
public class ScoutingApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PendingUploadStore.init(this);
        SyncManager.registerAutoSync(this);
    }
}
