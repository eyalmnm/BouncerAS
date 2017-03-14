package com.em_projects.bouncer.network.comm;

import android.util.Log;

import com.em_projects.bouncer.config.Constants;
import com.em_projects.utils.StringUtil;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Communicator implements Runnable {

    private static final String TAG = "Communicator";

    private static Communicator instance = null;

    private Thread runner = null;
    private boolean running = false;
    private Object monitor = new Object();

    private ArrayList<RequestHolder> queue = new ArrayList<RequestHolder>();

    private HttpClient client;     // Manage the cookies.

    private Communicator() {
        client = new DefaultHttpClient();
        running = true;
        runner = new Thread(this);
        runner.start();
    }

    public static Communicator getInstance() {
        if (instance == null) {
            Log.d(TAG, "getInstance() create instance!");
            instance = new Communicator();
        }
        return instance;
    }

    public void registration(String name, String phone, CommListener commListener) throws UnsupportedEncodingException {
        String serverUrl = Constants.SERVER.URL + "/register";
        Map<String, String> params = new HashMap<String, String>();
        params.put("name", name);
        params.put("phone", phone);

        post(serverUrl, params, commListener);
    }

    @Override
    public void run() {
        while (running) {
            if (queue.isEmpty()) {
                synchronized (monitor) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
            RequestHolder requestHolder = null;
            String response = null;
            try {
                requestHolder = queue.remove(0);
                if (requestHolder != null) {
                    Log.d(TAG, "run " + requestHolder.toString());
                    response = transmitData(requestHolder);
                    if (requestHolder.getListener() != null) {
                        if (StringUtil.isNullOrEmpty(response)) {
                            requestHolder.getListener().exceptionThrown(new Exception());
                        } else {
                            requestHolder.getListener().newDataArrived(response);
                        }
                    }
                }
            } catch (Exception ex) {
                Log.e(TAG, "run()", ex);
                ex.printStackTrace();
                if (requestHolder != null && requestHolder.getListener() != null) {
                    requestHolder.getListener().exceptionThrown(ex);
                }

            }
        }
    }

    private String transmitData(RequestHolder commRequest) throws IOException {
        Map<String, String> params = commRequest.getParams();
        RequestHolder.MethodType method = commRequest.getMethodType();
        String serverUrl = commRequest.getServerURL();
        HttpResponse httpResponse = null;
        HttpClient client = new DefaultHttpClient();
        if (method == RequestHolder.MethodType.GET) {
            String body = encodeParams(params);
            String urlString = serverUrl + "?" + body;
            Log.d(TAG, "transmitData urlString: " + urlString);
            HttpGet request = new HttpGet(urlString);
            httpResponse = client.execute(request);
        } else if (method == RequestHolder.MethodType.POST) {
            HttpPost httpPost = new HttpPost(serverUrl);
            ArrayList<NameValuePair> nameValuePairs = convertMapToNameValuePairs(params);
            Log.d(TAG, "transmitData urlString: " + serverUrl);
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            httpResponse = client.execute(httpPost);
        }

        // Check if server response is valid
        StatusLine status = httpResponse.getStatusLine();
        if (status.getStatusCode() != 200) {
            throw new IOException("Invalid response from server: " + status.toString());
        }
        // Return result from buffered stream
        String answer = handleHttpResponse(httpResponse);
        return answer;
    }

    // constructs the GET body using the parameters
    private String encodeParams(Map<String, String> params) {
        StringBuilder bodyBuilder = new StringBuilder();
        Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> param = iterator.next();
            if (param.getValue() != null) {
                try {
                    bodyBuilder.append(param.getKey()).append('=').append(URLEncoder.encode(param.getValue(), "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "encodeParams", e);
                }
                if (iterator.hasNext()) {
                    bodyBuilder.append('&');
                }
            }
        }
        return bodyBuilder.toString();
    }

    // constructs the POST body using the parameters
    private ArrayList<NameValuePair> convertMapToNameValuePairs(Map<String, String> params) {
        Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
        ArrayList<NameValuePair> nameValuePairs = new ArrayList(params.size());
        // constructs the POST body using the parameters
        while (iterator.hasNext()) {
            Map.Entry<String, String> param = iterator.next();
            if (param.getValue() != null) {
                nameValuePairs.add(new EmNameValuePair(param.getKey(), param.getValue()));
            }
        }
        return nameValuePairs;
    }

    private String handleHttpResponse(HttpResponse httpResponse) throws IllegalStateException, IOException {
        InputStream is = httpResponse.getEntity().getContent();
        InputStreamReader isr = new InputStreamReader(is, "UTF-8");
        BufferedReader bufferedReader = new BufferedReader(isr);
        StringBuffer stringBuffer = new StringBuffer("");
        String line = "";
        while ((line = bufferedReader.readLine()) != null) {
            stringBuffer.append(line);
        }
        bufferedReader.close();
        return stringBuffer.toString();
    }

    private void post(final String serverURL, final Map<String, String> params, CommListener listener) {
        queue.add(new RequestHolder(serverURL, params, listener));
        synchronized (monitor) {
            monitor.notifyAll();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        running = false;
        if (runner != null) {
            runner.join();
            runner = null;
        }
        if (client != null) {
            client = null;
        }
    }
}
