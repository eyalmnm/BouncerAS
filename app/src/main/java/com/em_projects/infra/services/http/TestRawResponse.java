package com.em_projects.infra.services.http;

import android.util.Log;

import java.io.Serializable;

@SuppressWarnings("serial")
public class TestRawResponse implements Serializable {
    private static final String TAG = "TestRawResponse";

    public final String HTML;

    public TestRawResponse(String html) {
        Log.d(TAG, "TestRawResponse");
        HTML = html;
    }
}
