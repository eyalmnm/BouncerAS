package com.em_projects.contactsApi;

import android.util.Log;

public class Address {
    private static final String TAG = "Address";

    private String poBox;
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String type;
    private String asString = "";

    public Address(String asString, String type) {
        Log.d(TAG, "Address");
        this.asString = asString;
        this.type = type;
    }

    public Address(String poBox, String street, String city, String state, String postal, String country, String type) {
        Log.d(TAG, "Address");
        this.setPoBox(poBox);
        this.setStreet(street);
        this.setCity(city);
        this.setState(state);
        this.setPostalCode(postal);
        this.setCountry(country);
        this.setType(type);
    }

    public String getType() {
        Log.d(TAG, "getType");
        return type;
    }

    public void setType(String type) {
        Log.d(TAG, "setType");
        this.type = type;
    }

    public String getPoBox() {
        Log.d(TAG, "getPoBox");
        return poBox;
    }

    public void setPoBox(String poBox) {
        Log.d(TAG, "setPoBox");
        this.poBox = poBox;
    }

    public String getStreet() {
        Log.d(TAG, "getStreet");
        return street;
    }

    public void setStreet(String street) {
        Log.d(TAG, "setStreet");
        this.street = street;
    }

    public String getCity() {
        Log.d(TAG, "getCity");
        return city;
    }

    public void setCity(String city) {
        Log.d(TAG, "setCity");
        this.city = city;
    }

    public String getState() {
        Log.d(TAG, "getState");
        return state;
    }

    public void setState(String state) {
        Log.d(TAG, "setState");
        this.state = state;
    }

    public String getPostalCode() {
        Log.d(TAG, "getPostalCode");
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        Log.d(TAG, "setPostalCode");
        this.postalCode = postalCode;
    }

    public String getCountry() {
        Log.d(TAG, "getCountry");
        return country;
    }

    public void setCountry(String country) {
        Log.d(TAG, "setCountry");
        this.country = country;
    }

    public String toString() {
        if (this.asString.length() > 0) {
            return (this.asString);
        } else {
            String addr = "";
            if (this.getPoBox() != null) {
                addr = addr + this.getPoBox() + "n";
            }
            if (this.getStreet() != null) {
                addr = addr + this.getStreet() + "n";
            }
            if (this.getCity() != null) {
                addr = addr + this.getCity() + ", ";
            }
            if (this.getState() != null) {
                addr = addr + this.getState() + " ";
            }
            if (this.getPostalCode() != null) {
                addr = addr + this.getPostalCode() + " ";
            }
            if (this.getCountry() != null) {
                addr = addr + this.getCountry();
            }
            return (addr);
        }
    }
}