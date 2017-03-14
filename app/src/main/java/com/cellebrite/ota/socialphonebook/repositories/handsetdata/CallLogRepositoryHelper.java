package com.cellebrite.ota.socialphonebook.repositories.handsetdata;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;

import com.cellebrite.ota.socialphonebook.repositories.SQLQueryHelper;
import com.em_projects.infra.application.BasicApplication;

/**
 * Call log helper for version 1.5 and below.
 */
class CallLogRepositoryHelper {
    /**
     * Performs a query on the contacts addresses table.
     *
     * @param contactId (phoneNumbers) a list of phone numbers as condition or null for all call logs.
     * @return (Cursor) cursor positioned before the first entry or null in case no entry found at all.
     */
    static Cursor performQueryOnCallLogTable(String[] phoneNumbers) {
        //creates a URI for the the call logs
        Uri contactURI = CallLog.Calls.CONTENT_URI;

        //holds the content resolver
        ContentResolver cr = BasicApplication.getApplication().getContentResolver();

        //holds the selection
        String selection = null;

        //holds the selection args
        String[] selectionArgs = null;

        //if phones were given direct the query
        if (phoneNumbers != null) {
            //if its only one phone
            if (phoneNumbers.length == 1) {
                //encode the phone number and build the URI
                contactURI = Uri.withAppendedPath(CallLog.Calls.CONTENT_FILTER_URI, Uri.encode(phoneNumbers[0]));
            } else {
                //else create a IN section
                selection = SQLQueryHelper.createINSelection(CallLog.Calls.NUMBER, phoneNumbers);
            }

        }

        return cr.query(contactURI, getCallLogsTableQueryProjection(), selection, selectionArgs, CallLog.Calls.DEFAULT_SORT_ORDER);
    }

    /**
     * Gets current pointed call element's call time.
     *
     * @return (long) current pointed call element's call time.
     */
    private static long getCallTime(Cursor curr) {
        //return the current pointed call element call's time
        return curr.getLong(curr.getColumnIndex(CallLog.Calls.DATE));
    }

    /**
     * Gets current pointed call element's call type (incoming, outgoing, missed).
     *
     * @return (int) current pointed call element's call type (incoming, outgoing, missed).
     */
    private static int getCallType(Cursor curr) {
        //return the current pointed call element call's time
        return curr.getInt(curr.getColumnIndex(CallLog.Calls.TYPE));
    }

    /**
     * Gets current call's duration in seconds.
     *
     * @return (long) current call's duration in seconds.
     */
    private static long getCallDurationFromCursor(Cursor curr) {
        //return the current pointed call element call's time
        return curr.getLong(curr.getColumnIndex(CallLog.Calls.DURATION));
    }

    /**
     * Gets current pointed call element's ID.
     *
     * @return (int) current pointed call element's ID, or -1 in case all call elements have already
     * been traversed by the manager.
     */
    private static String getCallID(Cursor curr) {
        //gets current pointed call element's ID
        return curr.getString(curr.getColumnIndex(CallLog.Calls._ID));
    }

    /**
     * Gets current pointed call element's caller phone number.
     *
     * @return (String) current pointed call element's caller phone number.
     * Method will return null in case all call elements have already been traversed by the manager.
     */
    private static String getCallerNumber(Cursor curr) {
        //return the current pointed call element caller's phone number
        return curr.getString(curr.getColumnIndex(CallLog.Calls.NUMBER));
    }

    /**
     * Gets current pointed call element's caller name.
     *
     * @return (String) current pointed call element's caller name if exist, method will return null in case
     * caller name doesn't exist or if all call elements have already been traversed by the manager.
     */
    private static String getCallerName(Cursor curr) {
        //return the current pointed call element caller's name
        return curr.getString(curr.getColumnIndex(CallLog.Calls.CACHED_NAME));
    }

    /**
     * Returns the projection on a calllog table.
     */
    private static String[] getCallLogsTableQueryProjection() {
        //holds the table columns which need to retrieve by query
        return new String[]{
                CallLog.Calls._ID,
                CallLog.Calls.DURATION,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.NUMBER,
                CallLog.Calls.CACHED_NAME};
    }

    /**
     * Gets a call element with its details (caller name, time, type etc...)
     *
     * @param callLogCurr (CallLogManager != null) a call-log manager pointing on the call element.
     */
    static CallElement getCallElementFromCursor(Cursor callLogCurr) {
        //creates the call element
        CallElement callElement = new CallElement();

        //set call's ID
        callElement.m_uid = getCallID(callLogCurr);

        //set call's caller phone number
        callElement.m_callerNumber = getCallerNumber(callLogCurr);

        //set call's caller name
        callElement.m_callerName = getCallerName(callLogCurr);

        //set call's time
        callElement.m_callTime = getCallTime(callLogCurr);

        //set call's type
        callElement.m_callType = getCallType(callLogCurr);

        //set call's time duration
        callElement.m_callDuration = getCallDurationFromCursor(callLogCurr);

        //returns the call element
        return callElement;
    }
}