package com.em_projects.bouncer.network.comm;

// import android.graphics.Bitmap;

import android.util.Log;

import com.em_projects.bouncer.config.Constants;
import com.em_projects.bouncer.utils.StringUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
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
import java.util.ArrayList;

//import org.apache.http.entity.mime.HttpMultipartMode;
//import org.apache.http.entity.mime.MultipartEntity;
//import org.apache.http.entity.mime.content.ByteArrayBody;
//import org.apache.http.entity.mime.content.StringBody;


@SuppressWarnings("deprecation")
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

//    public void uploadPicture(String riddle, Bitmap bitmap, CommListener commListener) throws UnsupportedEncodingException {
//        String request = Constants.SERVER.URL + "/UploadPicture";
//        MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
//        multipartEntity.addPart("riddle", new StringBody(riddle));
//        multipartEntity.addPart("image", new ByteArrayBody(ImageUtils.convertBitmap2ByteArray(bitmap), String.valueOf(System.currentTimeMillis()) + ".png"));
//        sendRequest(Constants.SERVER.HTTP_POST, request, (HttpEntity) multipartEntity, null, commListener);
//    }

    public void registration(String name, String phone, CommListener commListener) throws UnsupportedEncodingException {
        String request = Constants.SERVER.URL + "/register";
        ArrayList<NameValuePair> emNameValuePairs = new ArrayList<NameValuePair>(2);
        emNameValuePairs.add(new EmNameValuePair("name", name));
        emNameValuePairs.add(new EmNameValuePair("phone", phone));
        sendRequest(Constants.SERVER.HTTP_POST, request, new UrlEncodedFormEntity(emNameValuePairs), null, commListener);
    }

    private void sendRequest(int httpMethod, String request, HttpEntity entity, ArrayList<NameValuePair> nameValuePairs, CommListener listener) {
        Log.d(TAG, "sendRequest");
        queue.add(new RequestHolder(httpMethod, request, entity, nameValuePairs, listener));
        synchronized (monitor) {
            monitor.notify();
        }
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
                    response = transmitData(requestHolder.getHttpMethod(), requestHolder.getRequest(), requestHolder.getHttpEntity(), requestHolder.getNameValuePairs());
                    if (requestHolder.getListener() != null) {
                        if (StringUtils.isNullOrEmpty(response)) {
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

    private synchronized String transmitData(int method, String request, HttpEntity httpEntity, ArrayList<NameValuePair> nameValuePairs) throws IOException {
        BufferedReader bufferedReader = null;
        Log.d(TAG, "transmitData");
        String data = null;
        if (method == Constants.SERVER.HTTP_POST) {
            if (httpEntity == null) {
                throw new NullPointerException("HttpEntity can not be null when POST method is applied!");
            }
            HttpPost httpPost = new HttpPost(request);
            httpPost.setEntity(httpEntity); // (new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse httpResponse = client.execute(httpPost);
            InputStream is = httpResponse.getEntity().getContent();
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            bufferedReader = new BufferedReader(isr);
            StringBuffer stringBuffer = new StringBuffer("");
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }
            bufferedReader.close();
            data = stringBuffer.toString();
        } else if (method == Constants.SERVER.HTTP_GET) {       // Handling GET
            if (nameValuePairs == null) {
                throw new NullPointerException("NameValuePairs can not be null when GET method is applied!");
            }
            String realRequest = getRequestBuilder(request, nameValuePairs);
            HttpGet httpGet = new HttpGet(realRequest);
            HttpResponse httpResponse = client.execute(httpGet);
            InputStream is = httpResponse.getEntity().getContent();
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            bufferedReader = new BufferedReader(isr);
            StringBuffer stringBuffer = new StringBuffer("");
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }
            bufferedReader.close();
            data = stringBuffer.toString();
        } else if (method == Constants.SERVER.HTTP_SOCKET) {
            // DO Nothing Yet!
        }
        return data;
    }


    private String getRequestBuilder(String request, ArrayList<NameValuePair> nameValuePairs) {
        if (request.endsWith("/")) {
            request = request.substring(0, request.length() - 1);
        }
        StringBuilder stringBuilder = new StringBuilder(request);
        if (nameValuePairs.size() > 0) {
            stringBuilder.append("?");
            NameValuePair tempNameValuePair = null;
            for (int i = 0; i < nameValuePairs.size(); i++) {
                tempNameValuePair = nameValuePairs.get(i);
                stringBuilder.append(tempNameValuePair.getName());
                stringBuilder.append("=");
                stringBuilder.append(tempNameValuePair.getValue());
                if (i < nameValuePairs.size() - 1) {
                    stringBuilder.append("&");
                }
            }
        }
        return stringBuilder.toString();
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
