package com.em_projects.infra.services.http;

import android.util.Log;

import java.io.IOException;


public class TestRawRequest extends RawWebRequest<TestRawResponse> {
    private static final String TAG = "TestRawRequest";

    public TestRawRequest() {
        Log.d(TAG, "TestRawRequest");
        setMethod(RequestMethod.GET);
        setTimeout(1000 * 30);
    }

    @Override
    protected byte[] getRawDataToSend() {
        Log.d(TAG, "getRawDataToSend");
        //no data to send
        return null;
    }

    @Override
    protected TestRawResponse parseRecievedRawData(byte[] data)
            throws IOException {
        Log.d(TAG, "parseRecievedRawData");
        String encoding = getResponseContentEncoding();
        if (encoding != null) {
            return new TestRawResponse(new String(data, encoding));
        }

        return new TestRawResponse(new String(data));
    }

    @Override
    protected String getURL() {
        Log.d(TAG, "getURL");
        return "http://www.google.com";
    }

}
