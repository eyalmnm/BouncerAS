package com.em_projects.bouncer.model;

import android.util.Log;

import com.em_projects.infra.model.Entity;

import java.util.Comparator;

@SuppressWarnings("serial")
public class Contact extends Entity<String> {
    private static final String TAG = "Contact";

    //Comparator for contacts list sorting
    public static Comparator<Contact> COMPARE_BY_DISPLAY_NAME = new Comparator<Contact>() {
        public int compare(Contact one, Contact other) {
            if (one != null && other != null)
                return one.getDisplayName().toLowerCase().compareTo(other.getDisplayName().toLowerCase());
            else
                return 0;
        }
    };
    private String m_displayName = "";
    private boolean b_isHidden = false;
    private byte[] m_photoData = null;

    public Contact(String uid) {
        super(uid);
        Log.d(TAG, "Contact");
    }

    public void setDetails(String displayName) {
        Log.d(TAG, "setDetails");
        m_displayName = displayName;
    }

    public String getDisplayName() {
        Log.d(TAG, "getDisplayName");
        return m_displayName;
    }

    public boolean isHidden() {
        Log.d(TAG, "isHidden");
        return b_isHidden;
    }

    public void setHidden(boolean isHidden) {
        Log.d(TAG, "setHidden");
        b_isHidden = isHidden;
    }

    public byte[] getPhotoData() {
        Log.d(TAG, "getPhotoData");
        return m_photoData;
    }

    public void setPhotoData(byte[] photoData) {
        Log.d(TAG, "setPhotoData");
        m_photoData = photoData;
    }
}
