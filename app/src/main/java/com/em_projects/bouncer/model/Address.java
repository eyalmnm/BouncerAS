package com.em_projects.bouncer.model;

import android.util.Log;

import com.em_projects.infra.model.ValueObject;

@SuppressWarnings("serial")
public class Address extends ValueObject {

    private static final String TAG = "Address";

    public String Street, City, State;

    public Address(String street, String city, String state) {
        Log.d(TAG, "Address");
        Street = street;
        City = city;
        State = state;
    }
}
