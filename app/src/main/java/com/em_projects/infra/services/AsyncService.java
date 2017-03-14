package com.em_projects.infra.services;

import android.util.Log;

import java.io.Serializable;

public abstract class AsyncService<T extends Serializable> extends Service<T> {
    private static final String TAG = "AsyncService";

    @Override
    public final void startService() {
        Log.d(TAG, "startService");
        new Thread(this).start();
    }
}
