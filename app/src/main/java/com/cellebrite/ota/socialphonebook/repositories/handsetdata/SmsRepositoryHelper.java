package com.cellebrite.ota.socialphonebook.repositories.handsetdata;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;

import com.cellebrite.ota.socialphonebook.repositories.SQLQueryHelper;
import com.em_projects.infra.application.BasicApplication;

class SmsRepositoryHelper {
    /**
     * This method returns a cursor to the SMS table.
     *
     * @param (phonesArray) list of phone numbers to filter by.
     * @return (Cursor) a cursor to the table or null if nothing was found.
     */
    static Cursor performQueryOnSmsTable(String[] phoneNumbers) {
        //creates a URI for the the call logs
        Uri contactURI = SmsTable.CONTENT_URI;

        //holds the content resolver
        ContentResolver cr = BasicApplication.getApplication().getContentResolver();

        //holds the selection
        String selection = null;

        //holds the selection args
        String[] selectionArgs = null;

        //if phones were given direct the query
        if (phoneNumbers != null) {
            //if its only one phone
            selection = SQLQueryHelper.createLikeSelection(SmsTable.ADDRESS, phoneNumbers, true, false);
        }

        return cr.query(contactURI, getSmsTableQueryProjection(), selection, selectionArgs, CallLog.Calls.DEFAULT_SORT_ORDER);
    }

    /**
     * Returns the projection on a SMS table.
     */
    private static String[] getSmsTableQueryProjection() {
        //holds the table columns which need to retrieve by query
        return new String[]{
                SmsTable._ID,
                SmsTable.ADDRESS,
                SmsTable.BODY,
                SmsTable.PERSON_ID,
                SmsTable.SUBJECT,
                SmsTable.TYPE,
                SmsTable.DATE};
    }

    /**
     * Gets current pointed sms element's ID.
     *
     * @return (int) current pointed sms element's ID, or -1 in case all call elements have already
     * been traversed by the manager.
     */
    private static String getMessageID(Cursor smsTableCurr) {
        //gets current pointed sms element's ID
        return smsTableCurr.getString(smsTableCurr.getColumnIndex(SmsTable._ID));
    }

    /**
     * Gets current pointed sms element's caller phone number.
     *
     * @return (String) current pointed sms element's phone number.
     * Method will return null in case all call elements have already been traversed by the manager.
     */
    private static String getMessageNumber(Cursor smsTableCurr) {
        //return the current pointed sms element phone number
        return smsTableCurr.getString(smsTableCurr.getColumnIndex(SmsTable.ADDRESS));
    }

    /**
     * Gets current pointed sms element's sms time.
     *
     * @return (long) current pointed sms element's sms time.
     */
    private static long getMessageDate(Cursor smsTableCurr) {
        //return the current pointed sms element time
        return smsTableCurr.getLong(smsTableCurr.getColumnIndex(SmsTable.DATE));
    }

    /**
     * returns 1  if incoming sms otherwise is outgoing sms
     *
     * @return (byte) sms type.
     */
    private static byte getMessageType(Cursor smsTableCurr) {
        //return the current pointed sms element time
        return (byte) smsTableCurr.getInt(smsTableCurr.getColumnIndex(SmsTable.TYPE));
    }

    /**
     * Gets message associated contact id
     *
     * @return (int) message contact id
     */
    private static String getMessagePersonID(Cursor smsTableCurr) {
        //return the current pointed sms element call's time
        return smsTableCurr.getString(smsTableCurr.getColumnIndex(SmsTable.PERSON_ID));
    }

    /**
     * Gets message body
     *
     * @return (String) message body
     */
    private static String getMessageBody(Cursor smsTableCurr) {
        //return the current pointed sms element body
        return smsTableCurr.getString(smsTableCurr.getColumnIndex(SmsTable.BODY));
    }

    /**
     * Returns a SmsElement object from a given cursor.
     *
     * @param smsTableCurr (Cursor != null) directed to a valid record.
     * @return (SmsElement != null) a SmsElement representing the data in the current record.
     */
    static SmsElement getSmsElementFromCursor(Cursor smsTableCurr) {
        //creates the sms element
        SmsElement smsElement = new SmsElement();

        //set message ID
        smsElement.m_uid = getMessageID(smsTableCurr);

        //set message phone number
        smsElement.m_messageNumber = getMessageNumber(smsTableCurr);

        //set message time
        smsElement.m_messageDate = getMessageDate(smsTableCurr);

        //set message type
        smsElement.m_messageType = getMessageType(smsTableCurr);

        //set message person id
        smsElement.m_messagePersonID = getMessagePersonID(smsTableCurr);

        //set message body
        smsElement.m_messageBody = getMessageBody(smsTableCurr);

        //returns the call element
        return smsElement;
    }

    /**
     * Holds the column names of device's messages table.
     */
    private static class SmsTable {
        //hold's device's database event record column names
        private static Uri CONTENT_URI = Uri.parse("content://sms");
        private static String _ID = "_id";
        private static String ADDRESS = "address";
        private static String PERSON_ID = "person";
        private static String DATE = "date";
        private static String PROTOCOL = "protocol";
        private static String READ = "read";
        private static String STATUS = "status";
        private static String TYPE = "type";
        private static String SUBJECT = "subject";
        private static String BODY = "body";
        private static String SERVICE_CENTER = "service_center";
    }
}
