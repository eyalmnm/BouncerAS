package com.em_projects.bouncer.views.model;

import android.util.Log;

import com.em_projects.bouncer.model.Contact;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ContactViewModel implements Serializable {
    private static final String TAG = "ContactViewModel";

    public final String UID;
    public final String FullName;
    public boolean IsHidden;

    public ContactViewModel(Contact c) {
        Log.d(TAG, "ContactViewModel");
        UID = c.getUID();
        FullName = c.getDisplayName();
    }
}
