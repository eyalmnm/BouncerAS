package com.em_projects.bouncer.helpers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.provider.CallLog;
import android.util.Log;

import com.cellebrite.ota.socialphonebook.repositories.handsetdata.DeviceContactsDataRepository;
import com.em_projects.bouncer.BouncerApplication;
import com.em_projects.bouncer.BouncerProperties;
import com.em_projects.bouncer.model.CallLogElement;
import com.em_projects.infra.application.BasicApplication;
import com.em_projects.infra.helpers.DatabaseHelper;

import java.util.UUID;
import java.util.Vector;

public class CallLogsDBHelper extends DatabaseHelper {

    private static final String TAG = "CallLogsDBHelper";

    private static final String CALLLOGS_TABLE_NAME = "calllogs";
    //holds the sql commands for database creation
    private static final String[] DATABASE_CREATE = new String[]{
            new StringBuffer()
                    .append("CREATE TABLE ").append(CALLLOGS_TABLE_NAME)
                    .append(" (")
                    .append(CallLogTable._ID).append(" TEXT PRIMARY KEY,")
                    .append(CallLogTable.DATE_TIME).append(" INTEGER,")
                    .append(CallLogTable.TYPE).append(" INTEGER,")
                    .append(CallLogTable.NAME).append(" TEXT,")
                    .append(CallLogTable.DURATION).append(" INTEGER,")
                    .append(CallLogTable.NUMBER).append(" TEXT,")
                    .append(CallLogTable.CONTACT_ID).append(" TEXT")
                    .append(" )")
                    .toString()};
    private static final String[] HIDDEN_DB_PROJECTION = new String[]{
            CallLogTable._ID,
            CallLogTable.TYPE,
            CallLogTable.CONTACT_ID,
            CallLogTable.NUMBER,
            CallLogTable.NAME,
            CallLogTable.DATE_TIME,
            CallLogTable.DURATION};
    private static CallLogsDBHelper s_instance;

    private CallLogsDBHelper() {
        super(BouncerApplication.getApplication(),
                BouncerProperties.CALLOGS_DATABASE_NAME,
                BouncerProperties.DATABASE_VERSION, DATABASE_CREATE);
        Log.d(TAG, "CallLogsDBHelper");
    }

    public static synchronized CallLogsDBHelper getInstance() {
        Log.d(TAG, "getInstance");
        if (s_instance == null)
            s_instance = new CallLogsDBHelper();

        return s_instance;
    }

    public int deleteCallLogsFromDevice(String phoneNumber) {
        Log.d(TAG, "deleteCallLogsFromDevice");
        try {
            //get the content resolver
            ContentResolver cr = BasicApplication.getApplication().getContentResolver();

            //deletes the call log item, and returns the number of effected rows
            int rows = cr.delete(CallLog.Calls.CONTENT_URI, CallLog.Calls.NUMBER + "=?", new String[]{phoneNumber});

            //clean the phone number
            phoneNumber = DeviceContactsDataRepository.getInstance().cleanPhoneNumber(phoneNumber);

            //deletes the call log item, and returns the number of effected rows
            rows += cr.delete(CallLog.Calls.CONTENT_URI, CallLog.Calls.NUMBER + "=?", new String[]{phoneNumber});

            return rows;
        } catch (Throwable throwable) {
            Log.e(CallLogsDBHelper.class.getSimpleName(), "deleteCallLogsFromDevice()", throwable);

            return 0;
        }
    }

    public int saveCallLogsToDevice(Vector<CallLogElement> callLogs) {
        Log.d(TAG, "saveCallLogsToDevice");
        try {
            //get the content resolver
            ContentResolver cr = BasicApplication.getApplication().getContentResolver();

            ContentValues[] bulk = new ContentValues[callLogs.size()];

            for (int i = 0; i < bulk.length; ++i) {
                bulk[i] = new ContentValues();

                fillContentValuesWithCallLogElement(bulk[i], callLogs.elementAt(i));
            }

            return cr.bulkInsert(CallLog.Calls.CONTENT_URI, bulk);
        } catch (Throwable throwable) {
            Log.e(CallLogsDBHelper.class.getSimpleName(), "insertCallLogsToDevice()", throwable);

            return 0;
        }

    }

    private void fillContentValuesWithCallLogElement(ContentValues contentValues, CallLogElement callLog) {
        Log.d(TAG, "fillContentValuesWithCallLogElement");
        contentValues.put(CallLog.Calls.NUMBER, callLog.CallerNumber);
        contentValues.put(CallLog.Calls.CACHED_NAME, callLog.CallerName);
        contentValues.put(CallLog.Calls.DATE, callLog.CallTime);
        contentValues.put(CallLog.Calls.TYPE, callLog.CallType);
        contentValues.put(CallLog.Calls.DURATION, callLog.CallDuration);
    }

    public int saveCallLogsToHiddenStorage(String contactId, Vector<CallLogElement> callLogs) {
        Log.d(TAG, "saveCallLogsToHiddenStorage");
        int count = 0;

        ContentValues cv = new ContentValues();

        m_db.beginTransaction();

        for (CallLogElement ce : callLogs) {
            cv.clear();

            cv.put(CallLogTable._ID, UUID.randomUUID().toString());
            cv.put(CallLogTable.NAME, ce.CallerName);
            cv.put(CallLogTable.NUMBER, ce.CallerNumber);
            cv.put(CallLogTable.DURATION, ce.CallDuration);
            cv.put(CallLogTable.TYPE, ce.CallType);
            cv.put(CallLogTable.DATE_TIME, ce.CallTime);
            cv.put(CallLogTable.CONTACT_ID, contactId);

            if (m_db.insert(CALLLOGS_TABLE_NAME, null, cv) != -1)
                count++;
        }

        m_db.setTransactionSuccessful();

        m_db.endTransaction();

        return count;
    }

    public int deleteCallLogsFromHiddenStorageByPhone(String phoneNumber) {
        Log.d(TAG, "deleteCallLogsFromHiddenStorageByPhone");
        try {
            m_db.beginTransaction();

            return m_db.delete(CALLLOGS_TABLE_NAME, CallLogTable.NUMBER + "=?", new String[]{phoneNumber});
        } finally {
            m_db.setTransactionSuccessful();

            m_db.endTransaction();
        }
    }

    public Vector<CallLogElement> getFromHiddenStorageByPhoneNum(String phoneNumber) {
        Log.d(TAG, "getFromHiddenStorageByPhoneNum");
        Vector<CallLogElement> callLogs = new Vector<CallLogElement>();

        Cursor c = null;

        try {
            c = m_db.query(CALLLOGS_TABLE_NAME, HIDDEN_DB_PROJECTION, CallLogTable.NUMBER + "=?", new String[]{phoneNumber}, null, null, null);

            if (c != null && c.moveToFirst()) {
                do {
                    callLogs.add(getCallLogElementFromCursor(c));
                }
                while (c.moveToNext());
            }
        } finally {
            if (c != null)
                c.close();
        }

        return callLogs;
    }

    private CallLogElement getCallLogElementFromCursor(Cursor c) {
        Log.d(TAG, "getCallLogElementFromCursor");
        String id = c.getString(c.getColumnIndex(CallLogTable._ID));
        String name = c.getString(c.getColumnIndex(CallLogTable.NAME));
        String number = c.getString(c.getColumnIndex(CallLogTable.NUMBER));
        int type = c.getInt(c.getColumnIndex(CallLogTable.TYPE));
        long duration = c.getLong(c.getColumnIndex(CallLogTable.DURATION));
        long time = c.getLong(c.getColumnIndex(CallLogTable.DATE_TIME));

        CallLogElement cle = new CallLogElement(id, name, number, type, duration, time);

        return cle;
    }

    public Vector<CallLogElement> getFromHiddenStorageByContactId(String contactId) {
        Log.d(TAG, "getFromHiddenStorageByContactId");
        Vector<CallLogElement> callLogs = new Vector<CallLogElement>();

        Cursor c = null;

        try {
            c = m_db.query(CALLLOGS_TABLE_NAME, HIDDEN_DB_PROJECTION, CallLogTable.CONTACT_ID + "=?",
                    new String[]{contactId}, null, null, null);

            if (c != null && c.moveToFirst()) {
                do {
                    callLogs.add(getCallLogElementFromCursor(c));
                }
                while (c.moveToNext());
            }
        } finally {
            if (c != null)
                c.close();
        }

        return callLogs;
    }

    public Vector<CallLogElement> getDeviceLazyCallLogs() {
        Log.d(TAG, "getDeviceLazyCallLogs");
        //holds lazy call-logs
        Vector<CallLogElement> lazyCallLogs = new Vector<CallLogElement>();

        //get the content resolver
        ContentResolver cr = BasicApplication.getApplication().getContentResolver();

        //holds the cursor
        Cursor c = null;

        try {
            //get cursor to all lazy contacts
            c = cr.query(CallLog.Calls.CONTENT_URI,
                    new String[]{CallLogTable._ID, CallLogTable.DATE_TIME, CallLogTable.NAME, CallLogTable.NUMBER},
                    null, null, CallLog.Calls.DEFAULT_SORT_ORDER);

            //in case we have data
            if (c != null && c.moveToFirst()) {
                do {
                    //get call params from cursor
                    String id = c.getString(c.getColumnIndex(CallLogTable._ID));
                    long time = c.getLong(c.getColumnIndex(CallLogTable.DATE_TIME));
                    String name = c.getString(c.getColumnIndex(CallLogTable.NAME));
                    String number = c.getString(c.getColumnIndex(CallLogTable.NUMBER));

                    //create lazy call-log
                    CallLogElement call = new CallLogElement(id, name, number, -1, -1, time);

                    //add the call-log to the collection
                    lazyCallLogs.add(call);
                }
                while (c.moveToNext());
            }
        } finally {
            if (c != null)
                c.close();
        }

        //return log ids
        return lazyCallLogs;
    }

    public Vector<CallLogElement> getHiddenLazyCallLogs() {
        Log.d(TAG, "getHiddenLazyCallLogs");
        //holds lazy call-logs
        Vector<CallLogElement> lazyCallLogs = new Vector<CallLogElement>();

        //holds the cursor
        Cursor c = null;

        try {
            //get cursor to all lazy contacts
            c = m_db.query(CALLLOGS_TABLE_NAME,
                    new String[]{CallLogTable._ID, CallLogTable.DATE_TIME, CallLogTable.NAME, CallLogTable.NUMBER},
                    null, null, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);

            //in case we have data
            if (c != null && c.moveToFirst()) {
                do {
                    //get call params from cursor
                    String id = c.getString(c.getColumnIndex(CallLogTable._ID));
                    long time = c.getLong(c.getColumnIndex(CallLogTable.DATE_TIME));
                    String name = c.getString(c.getColumnIndex(CallLogTable.NAME));
                    String number = c.getString(c.getColumnIndex(CallLogTable.NUMBER));

                    //create lazy call-log
                    CallLogElement call = new CallLogElement(id, name, number, -1, -1, time);

                    //add the call-log to the collection
                    lazyCallLogs.add(call);
                }
                while (c.moveToNext());
            }
        } finally {
            if (c != null)
                c.close();
        }

        //return log ids
        return lazyCallLogs;
    }

    /**
     * Gets CallLogElement by call-log id.
     *
     * @param callLogId (String != null) call-log id to retreive.
     * @return (CallLogElement) The requested call-log element. May be null!
     */
    public CallLogElement getCallLogById(String callLogId) {
        Log.d(TAG, "getCallLogById");
        //holds the call-log element
        CallLogElement callLog = null;

        //get the content resolver
        ContentResolver cr = BasicApplication.getApplication().getContentResolver();

        //holds the cursor
        Cursor c = null;

        try {
            //set query selection string
            String where = new StringBuffer(CallLogTable._ID).append("='").append(callLogId).append("'").toString();

            //get device call-log by given id
            c = cr.query(CallLog.Calls.CONTENT_URI, null, where, null, CallLog.Calls.DEFAULT_SORT_ORDER);

            //in case we have no data, try the hidden database
            if (c == null || c.getCount() <= 0) {
                //get hidden call-log by given id
                c = m_db.query(CALLLOGS_TABLE_NAME, null, where, null, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);
            }

            //in case we have data
            if (c != null && c.moveToFirst()) {
                do {
                    //add the current id to ids collection
                    callLog = getCallLogElementFromCursor(c);
                }
                while (c.moveToNext());
            } else {
                Log.d(getClass().getSimpleName(), "getCallLogById() - couldn't find call-log with id: " + callLogId);
            }
        } finally {
            if (c != null)
                c.close();
        }

        //return the call-log element
        return callLog;
    }

    /**
     * Holds the column names of device's messages table.
     */
    private static class CallLogTable {
        //hold's device's database event record column names
        private final static String _ID = CallLog.Calls._ID;
        private final static String NAME = CallLog.Calls.CACHED_NAME;
        private final static String NUMBER = CallLog.Calls.NUMBER;
        private final static String DURATION = CallLog.Calls.DURATION;
        private final static String DATE_TIME = CallLog.Calls.DATE;
        private final static String TYPE = CallLog.Calls.TYPE;
        private final static String CONTACT_ID = "contact_id";
    }
}
