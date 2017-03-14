package com.em_projects.infra.services.http;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

public abstract class RawWebRequest<T extends Serializable> extends BasicWebRequest<T> {
    private static final String TAG = "RawWebRequest";

    @Override
    protected final T parseFromInStream(InputStream is) throws IOException {
        Log.d(TAG, "parseFromInStream");
        byte[] data = readRawData(is);

        return parseRecievedRawData(data);
    }

    private byte[] readRawData(InputStream is) throws IOException {
        Log.d(TAG, "readRawData");
        int contentLength = getResponseContentLength();
        BufferedInputStream bis = new BufferedInputStream(is);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(contentLength != -1 ? contentLength : 1024);

        byte[] buffer = new byte[1024];
        int bytesRead = 0;
        while ((bytesRead = bis.read(buffer, 0, 1024)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }

        return baos.toByteArray();
    }

    @Override
    protected void writeToOutStream(OutputStream os) throws IOException {
        Log.d(TAG, "writeToOutStream");
        byte[] dataToSend = getRawDataToSend();
        os.write(dataToSend);
        os.flush();
    }

    protected abstract byte[] getRawDataToSend();

    protected abstract T parseRecievedRawData(byte[] data) throws IOException;
}
