package com.em_projects.bouncer.model;

import android.util.Log;

import com.em_projects.infra.model.Entity;

@SuppressWarnings("serial")
public class Building extends Entity<Address> {

    private static final String TAG = "Building";

    public Building(Address uid) {
        super(uid);
        Log.d(TAG, "Building");
    }
}
