package com.em_projects.infra.application;

import android.util.Log;

public abstract class UserSession {
    private static final String TAG = "UserSession";

    protected abstract void persist();

    protected abstract void restore();

    public String getAccountType() {
        Log.d(TAG, "getAccountType");
        return null;
    }

    public String getAccountName() {
        Log.d(TAG, "getAccountName");
        return null;
    }
}
