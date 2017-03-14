package com.em_projects.bouncer.network.comm;

import java.util.Map;


// http://stackoverflow.com/questions/6028981/using-httpclient-and-httppost-in-android-with-post-parameters
// http://stackoverflow.com/questions/2017414/post-multipart-request-with-android-sdk
// http://masl.cis.gvsu.edu/2010/04/05/android-code-sample-asynchronous-http-connections/

public class RequestHolder {

    private String serverURL;
    ;
    private Map<String, String> params;
    private CommListener listener;
    private MethodType methodType;

    public RequestHolder(String serverURL, Map<String, String> params, CommListener listener) {
        this.serverURL = serverURL;
        this.params = params;
        this.listener = listener;
        this.methodType = MethodType.POST;
    }


    public RequestHolder(String serverURL, Map<String, String> params, MethodType methodType, CommListener listener) {
        this.serverURL = serverURL;
        this.params = params;
        this.listener = listener;
        this.methodType = methodType;
    }

    public String getServerURL() {
        return serverURL;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public CommListener getListener() {
        return listener;
    }

    public MethodType getMethodType() {
        return methodType;
    }

    public enum MethodType {GET, POST, SOCKET}
}
