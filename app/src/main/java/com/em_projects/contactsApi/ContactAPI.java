package com.em_projects.contactsApi;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;

public abstract class ContactAPI {
    private static final String TAG = "ContactAPI";

    private static ContactAPI api;

    private Cursor cur;
    private ContentResolver cr;

    public static ContactAPI getAPI() {
        Log.d(TAG, "getAPI");
        if (api == null) {
            String apiClass;
            if (Integer.parseInt(Build.VERSION.SDK) >= Build.VERSION_CODES.ECLAIR) {
                apiClass = "com.vario.infra.contactsApi.ContactApiSdk5";
            } else {
                apiClass = "com.vario.infra.contactsApi.ContactApiSdk3";
            }

            try {
                Class<? extends ContactAPI> realClass = Class.forName(apiClass).asSubclass(ContactAPI.class);
                api = realClass.newInstance();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }

        }
        return api;
    }

    public abstract Intent getContactIntent();

    public abstract ContactList newContactList();

    public abstract Cursor getCur();

    public abstract void setCur(Cursor cur);

    public abstract ContentResolver getCr();

    public abstract void setCr(ContentResolver cr);

    public abstract ArrayList<Phone> getPhoneNumbers(String id);

    public abstract Contact getContact(String contactId);
}