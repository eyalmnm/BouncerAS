package com.em_projects.infra.services.http;

import android.util.Log;

import java.io.Serializable;

@SuppressWarnings("serial")
public class TestXMLResponse implements Serializable {
    private static final String TAG = "TestXMLResponse";

    public final String XML;

    public TestXMLResponse(String xml) {
        Log.d(TAG, "TestXMLResponse");
        XML = xml;
    }
}
