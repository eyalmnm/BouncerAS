package com.em_projects.contactsApi;

import android.util.Log;

public class Email {
    private static final String TAG = "Email";

    private String address;
    private String type;

    public Email(String a, String t) {
        Log.d(TAG, "Email");
        this.address = a;
        this.type = t;
    }

    public String getAddress() {
        Log.d(TAG, "getAddress");
        return address;
    }

    public void setAddress(String address) {
        Log.d(TAG, "setAddress");
        this.address = address;
    }

    public String getType() {
        Log.d(TAG, "getType");
        return type;
    }

    public void setType(String t) {
        Log.d(TAG, "setType");
        this.type = t;
    }
}