package com.em_projects.contactsApi;

import android.util.Log;

public class Organization {
    private static final String TAG = "Organization";
    private String organization = "";
    private String title = "";

    public Organization() {
        Log.d(TAG, "Organization");
    }

    public Organization(String org, String title) {
        Log.d(TAG, "Organization");
        this.organization = org;
        this.title = title;
    }

    public String getOrganization() {
        Log.d(TAG, "getOrganization");
        return organization;
    }

    public void setOrganization(String organization) {
        Log.d(TAG, "setOrganization");
        this.organization = organization;
    }

    public String getTitle() {
        Log.d(TAG, "getTitle");
        return title;
    }

    public void setTitle(String title) {
        Log.d(TAG, "setTitle");
        this.title = title;
    }
}