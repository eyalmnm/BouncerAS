package com.em_projects.infra.services.http;

import android.util.Log;

import org.kxml2.io.KXmlSerializer;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class TestXMLRequest extends XMLWebRequest<TestXMLResponse> {
    private static final String TAG = "TestXMLRequest";

    public TestXMLRequest() {
        Log.d(TAG, "TestXMLRequest");
        setMethod(RequestMethod.POST);
    }

    @Override
    protected Document getXMLToSend() {
        Log.d(TAG, "getXMLToSend");
        Document doc = new Document();
        Element elements = new Element();
        elements.setName("Elements");
        for (int i = 0; i < 10; i++) {
            Element element = new Element();
            element.setName("Element");
            element.setAttribute(null, "Attribute", String.valueOf(i));
            elements.addChild(Node.ELEMENT, element);
        }

        doc.addChild(Node.ELEMENT, elements);

        return doc;
    }

    @Override
    protected TestXMLResponse parseXMLDoc(Document doc) {
        Log.d(TAG, "parseXMLDoc");
        KXmlSerializer serializer = new KXmlSerializer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            serializer.setOutput(baos, getResponseContentEncoding());
            doc.write(serializer);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new TestXMLResponse(new String(baos.toByteArray()));
    }

    @Override
    protected String getURL() {
        Log.d(TAG, "getURL");
//		return "http://www.celleshare.com/General/MobileClient/Action/Init?Imei="+UUID.randomUUID().toString()+"&Username=123456&Password=123456&OS=Android";
//		return "http://www.google.com";
        return "http://www.vario.co.il/DeviceLogCellular/Default.aspx";
    }

}
