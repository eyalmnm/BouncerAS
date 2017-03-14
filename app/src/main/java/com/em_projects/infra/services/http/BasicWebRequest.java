package com.em_projects.infra.services.http;

import android.util.Log;

import com.em_projects.infra.services.AsyncService;
import com.em_projects.infra.services.ServiceErrorArgs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

public abstract class BasicWebRequest<T extends Serializable> extends AsyncService<T> {
    public static final int ERR_URL_MALFORMED = 1000;
    public static final int ERR_OPEN_CONNECTION = 1001;
    public static final int ERR_OPEN_INPUT_STREAM = 1002;
    public static final int ERR_PARSING_INPUT_STREAM = 1003;
    public static final int ERR_OPEN_OUTPUT_STREAM = 1004;
    public static final int ERR_WRITING_OUTPUT_STREAM = 1005;
    public static final int ERR_GETTING_RESPONSE_CODE = 1006;
    public static final int ERR_HTTP_RESPONSE_CODE = 1007;
    public static final int ERR_CONNECTION_TIME_OUT = 1008;
    public static final int ERR_READ_TIME_OUT = 1009;
    private static final String TAG = "BasicWebRequest";
    private RequestMethod m_method = RequestMethod.GET;
    private Hashtable<String, String> m_headers = new Hashtable<String, String>();
    private int m_responseContentLength = -1;
    private String m_responseContentEncoding = null;
    private String m_responseContentType = null;
    private int m_timeOut = 0;

    /**
     * Sets the time-out for connection open and stream read.<br>
     * By default time-out is set to infinite.
     *
     * @param timeOutMilli
     */
    protected final void setTimeout(int timeOutMilli) {
        Log.d(TAG, "setTimeout");
        if (m_timeOut < 0)
            throw new RuntimeException("Timeout cannot be a negative number");

        m_timeOut = timeOutMilli;
    }

    /**
     * Sets the request method.<br>
     * By default the method is GET.
     *
     * @param method
     */
    protected final void setMethod(RequestMethod method) {
        Log.d(TAG, "setMethod");
        m_method = method;
    }

    /**
     * Set a request header.
     *
     * @param key
     * @param value
     */
    protected final void setHeader(String key, String value) {
        Log.d(TAG, "setHeader");
        if (m_headers != null)
            m_headers.put(key, value);
    }

    /**
     * Returns the response content length or -1 if not available.<br>
     * Note that proper use of this method should be done in the reading from stream part.
     *
     * @return
     */
    protected int getResponseContentLength() {
        Log.d(TAG, "getResponseContentLength");
        return m_responseContentLength;
    }

    /**
     * Returns the response content type or null if not available.<br>
     * Note that proper use of this method should be done in the reading from stream part.
     *
     * @return
     */
    protected String getResponseContentType() {
        Log.d(TAG, "getResponseContentType");
        return m_responseContentType;
    }

    /**
     * Returns the response content encoding or null if not available.<br>
     * Note that proper use of this method should be done in the reading from stream part.
     *
     * @return
     */
    protected String getResponseContentEncoding() {
        Log.d(TAG, "getResponseContentEncoding");
        return m_responseContentEncoding;
    }

    @Override
    protected final T execute() {
        Log.d(TAG, "execute");
        ///
        /// Create URL and open connection
        ///

        URL url = null;
        try {
            String urlString = getURL();
            if (urlString == null || !urlString.startsWith("http://"))
                throw new MalformedURLException("Missing http:// prefix");

            url = new URL(getURL());
        } catch (MalformedURLException e) {
            notifyError(new ServiceErrorArgs(ERR_URL_MALFORMED, e.getMessage()));
            return null;
        }

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            Set<Map.Entry<String, String>> set = m_headers.entrySet();

            for (Map.Entry<String, String> entry : set) {
                connection.addRequestProperty(entry.getKey(), entry.getValue());
            }

            if (isToKeepAlive()) {
                connection.setRequestProperty("Connection", "Keep-Alive");
            }
        } catch (IOException e) {
            notifyError(new ServiceErrorArgs(ERR_OPEN_CONNECTION, e.getMessage()));
            return null;
        }

        ///
        /// Open output stream and handle sending data
        ///

        if (m_method == RequestMethod.POST) {
            OutputStream os = null;
            try {
                try {
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    os = connection.getOutputStream();
                } catch (IOException e) {
                    notifyError(new ServiceErrorArgs(ERR_OPEN_OUTPUT_STREAM, e.getMessage()));
                    return null;
                }

                try {
                    writeToOutStream(os);
                } catch (Exception e) {
                    notifyError(new ServiceErrorArgs(ERR_WRITING_OUTPUT_STREAM, e.getMessage()));
                    return null;
                }
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        Log.e(getClass().getSimpleName(), e.getMessage());
                    }
                }
            }
        }

        ///
        /// Get the response code and content details
        ///

        int responseCode = -1;
        try {
            if (m_timeOut != -1) {
                connection.setConnectTimeout(m_timeOut);
            }

            connection.connect();

            responseCode = connection.getResponseCode();

            if (responseCode != HttpsURLConnection.HTTP_OK) {
                notifyError(new ServiceErrorArgs(ERR_HTTP_RESPONSE_CODE, String.valueOf(responseCode)));
                return null;
            }

            m_responseContentLength = connection.getContentLength();
            m_responseContentEncoding = connection.getContentEncoding();
            m_responseContentType = connection.getContentType();
        } catch (SocketTimeoutException ste) {
            notifyError(new ServiceErrorArgs(ERR_CONNECTION_TIME_OUT, ste.getMessage()));
            return null;
        } catch (IOException e) {
            notifyError(new ServiceErrorArgs(ERR_GETTING_RESPONSE_CODE, String.valueOf(responseCode)));
            return null;
        }

        ///
        /// Open input stream and handle parsing data
        ///

        InputStream is = null;
        try {
            if (m_timeOut != -1) {
                connection.setReadTimeout(m_timeOut);
            }

            is = connection.getInputStream();
        } catch (IOException e) {
            notifyError(new ServiceErrorArgs(ERR_OPEN_INPUT_STREAM, e.getMessage()));
            return null;
        }

        try {
            return parseFromInStream(is);
        } catch (SocketTimeoutException ste) {
            notifyError(new ServiceErrorArgs(ERR_READ_TIME_OUT, ste.getMessage()));
            return null;
        } catch (Exception e) {
            notifyError(new ServiceErrorArgs(ERR_PARSING_INPUT_STREAM, e.getMessage()));
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(getClass().getSimpleName(), e.getMessage());
                }
            }
        }
    }

    protected boolean isToKeepAlive() {
        Log.d(TAG, "isToKeepAlive");
        return true;
    }

    protected abstract String getURL();

    protected abstract T parseFromInStream(InputStream is) throws Exception;

    protected abstract void writeToOutStream(OutputStream os) throws Exception;

    /**
     * Http methods
     */
    protected enum RequestMethod {
        GET, POST
    }

    /**
     * Holds headers constants
     */
    protected static final class Headers {
        public static final String HEADER_KEY_CONTENT_LENGTH = "Content-Length";
        public static final String HEADER_KEY_CONTENT_TYPE = "Content-Type";
        public static final String HEADER_KEY_CONTENT_ENCODING = "Content-Encoding";

        public static final String HEADER_VALUE_CONTENT_TYPE_XML = "text/xml";
        public static final String HEADER_VALUE_CONTENT_TYPE_OCTET = "application/octet-stream";
        public static final String HEADER_VALUE_CONTENT_TYPE_PLAIN_TEXT = "text/plain";
    }

}
