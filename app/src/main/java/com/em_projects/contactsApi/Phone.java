package com.em_projects.contactsApi;

import android.util.Log;

public class Phone {
    private static final String TAG = "Phone";

    private String m_number;
    private String m_type;

    public Phone(String number, String type) {
        Log.d(TAG, "Phone");
        this.m_number = number;
        this.m_type = type;
    }

    public String getNumber() {
        Log.d(TAG, "getNumber");
        return m_number;
    }

    public void setNumber(String number) {
        Log.d(TAG, "setNumber");
        this.m_number = number;
    }

    public String getType() {
        Log.d(TAG, "getType");
        return m_type;
    }

    public void setType(String type) {
        Log.d(TAG, "setType");
        this.m_type = type;
    }

}