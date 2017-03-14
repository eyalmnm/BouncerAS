package com.em_projects.bouncer.network.comm;

import org.apache.http.NameValuePair;

/**
 * Created by Eyal Muchtar on 20/03/14.
 */
public class EmNameValuePair implements NameValuePair {

    private String name = null;
    private String value = null;

    public EmNameValuePair(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public EmNameValuePair(String name, long value) {
        this.name = name;
        this.value = String.valueOf(value);
    }

    public EmNameValuePair(String name, int value) {
        this.name = name;
        this.value = String.valueOf(value);
    }

    public EmNameValuePair(String name, boolean value) {
        this.name = name;
        this.value = String.valueOf(value);
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
