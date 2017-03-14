package com.em_projects.bouncer.network.comm;

import com.em_projects.bouncer.config.Constants;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;

import java.util.ArrayList;


// http://stackoverflow.com/questions/6028981/using-httpclient-and-httppost-in-android-with-post-parameters
// http://stackoverflow.com/questions/2017414/post-multipart-request-with-android-sdk
// http://masl.cis.gvsu.edu/2010/04/05/android-code-sample-asynchronous-http-connections/

public class RequestHolder {

    private String request = null;
    private HttpEntity httpEntity;
    private CommListener listener;
    private ArrayList<NameValuePair> nameValuePairs;
    private int httpMethod = Constants.SERVER.HTTP_POST;


    public RequestHolder(int httpMethod, String request, HttpEntity httpEntity, ArrayList<NameValuePair> nameValuePairs, CommListener listener) {
        super();
        this.request = request;
        this.listener = listener;
        this.httpMethod = httpMethod;
        this.httpEntity = httpEntity;
        this.nameValuePairs = nameValuePairs;
    }

    public int getHttpMethod() {
        return httpMethod;
    }

    public String getRequest() {
        return request;
    }

    public HttpEntity getHttpEntity() {
        return httpEntity;
    }

    public CommListener getListener() {
        return listener;
    }

    public ArrayList<NameValuePair> getNameValuePairs() {
        return nameValuePairs;
    }
}
