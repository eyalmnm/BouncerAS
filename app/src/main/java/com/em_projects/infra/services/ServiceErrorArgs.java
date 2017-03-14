package com.em_projects.infra.services;

import android.util.Log;

public class ServiceErrorArgs {
    private static final String TAG = "ServiceErrorArgs";

    public final int ErrorCode;
    public final String ErrorMessage;

    public ServiceErrorArgs(int errorCode, String message) {
        Log.d(TAG, "ServiceErrorArgs");
        ErrorCode = errorCode;
        ErrorMessage = message;
    }
}
