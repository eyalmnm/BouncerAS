package com.em_projects.bouncer.model;

import android.util.Log;

public class Phone {
    private static final String TAG = "Phone";

    private String m_number;
    private int m_type;

    public Phone(String number, int type) {
        Log.d(TAG, "Phone");
        this.m_number = number;
        this.m_type = type;
    }

    public String getNumber() {
        Log.d(TAG, "getNumber");
        return m_number;
    }

    public int getType() {
        Log.d(TAG, "getType");
        return m_type;
    }

}