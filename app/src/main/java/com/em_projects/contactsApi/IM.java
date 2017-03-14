package com.em_projects.contactsApi;

import android.util.Log;

public class IM {
    private static final String TAG = "IM";
    private String name;
    private String type;

    public IM(String name, String type) {
        Log.d(TAG, "IM");
        this.name = name;
        this.type = type;
    }

    public String getName() {
        Log.d(TAG, "getName");
        return name;
    }

    public void setName(String name) {
        Log.d(TAG, "setName");
        this.name = name;
    }

    public String getType() {
        Log.d(TAG, "getType");
        return type;
    }

    public void setType(String type) {
        Log.d(TAG, "setType");
        this.type = type;
    }
}