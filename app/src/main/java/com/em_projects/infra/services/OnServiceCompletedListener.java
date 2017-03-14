package com.em_projects.infra.services;

import android.util.Log;

import com.em_projects.infra.activity.BasicActivity;

public abstract class OnServiceCompletedListener<T> {
    private static final String TAG = "OnServiceCompletedLstnr";

    protected abstract void onServiceCompleted(T args);

    protected abstract void onServiceProgressChanged(T args);

    protected abstract void onServiceError(ServiceErrorArgs args);

    protected BasicActivity isToReturnOnUIThread() {
        Log.d(TAG, "isToReturnOnUIThread");
        return null;
    }
}
