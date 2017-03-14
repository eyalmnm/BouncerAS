package com.em_projects.infra.services.http;

import android.util.Log;

import org.kxml2.io.KXmlParser;
import org.kxml2.io.KXmlSerializer;
import org.kxml2.kdom.Document;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

public abstract class XMLWebRequest<T extends Serializable> extends BasicWebRequest<T> {
    private static final String TAG = "XMLWebRequest";

    public XMLWebRequest() {
        Log.d(TAG, "XMLWebRequest");
        setHeader(Headers.HEADER_KEY_CONTENT_TYPE, Headers.HEADER_VALUE_CONTENT_TYPE_XML);
    }

    @Override
    protected final T parseFromInStream(InputStream is) throws Exception {
        Log.d(TAG, "parseFromInStream");
        KXmlParser parser = new KXmlParser();

        try {
            parser.setInput(is, getResponseContentEncoding());
        } catch (Exception e) {
            parser.setInput(is, null);
        }

        Document doc = new Document();
        doc.parse(parser);

        return parseXMLDoc(doc);
    }

    @Override
    protected void writeToOutStream(OutputStream os) throws IOException {
        Log.d(TAG, "writeToOutStream");
        Document doc = getXMLToSend();
        KXmlSerializer serializer = new KXmlSerializer();
        serializer.setOutput(os, getXMLEncoding());
        doc.write(serializer);
        serializer.flush();
    }

    /**
     * Returns UTF-8 by default.
     */
    protected String getXMLEncoding() {
        Log.d(TAG, "getXMLEncoding");
        return "UTF-8";
    }

    ;

    /**
     * Returns the document to send.
     */
    protected abstract Document getXMLToSend();

    /**
     * Parses the document received from server.
     */
    protected abstract T parseXMLDoc(Document doc);

}
