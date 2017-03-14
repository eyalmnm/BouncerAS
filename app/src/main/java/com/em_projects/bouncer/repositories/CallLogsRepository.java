package com.em_projects.bouncer.repositories;

import android.util.Log;

import com.cellebrite.ota.socialphonebook.repositories.handsetdata.CallElement;
import com.cellebrite.ota.socialphonebook.repositories.handsetdata.CallsObserver;
import com.cellebrite.ota.socialphonebook.repositories.handsetdata.DeviceContactsDataRepository;
import com.cellebrite.ota.socialphonebook.repositories.handsetdata.DeviceContactsDataRepository.HandsetDataKinds;
import com.cellebrite.ota.socialphonebook.repositories.handsetdata.DeviceContactsDataRepository.HandsetDataRepositoryObserver;
import com.em_projects.bouncer.BouncerApplication;
import com.em_projects.bouncer.BouncerUserSession;
import com.em_projects.bouncer.helpers.CallLogsDBHelper;
import com.em_projects.bouncer.model.CallLogElement;
import com.em_projects.infra.repositories.EntityRepositoryWithCache;
import com.em_projects.utils.StringUtil;

import java.util.HashSet;
import java.util.Vector;

public class CallLogsRepository extends EntityRepositoryWithCache<CallLogElement, String> {
    private static final String TAG = "CallLogsRepository";

    private static CallLogsRepository s_instance = null;

    private CallLogsRepository() {
        Log.d(TAG, "CallLogsRepository");
        DeviceContactsDataRepository.getInstance().registerContactObserver(new HandsetDataRepositoryObserver() {
            @Override
            public void onHandsetDataChange(HandsetDataKinds kind) {
                //data has changed - clear cache
                if (kind == HandsetDataKinds.CALLLOG)
                    clearCache();
            }
        });
    }

    public static synchronized CallLogsRepository getInstance() {
        Log.d(TAG, "getInstance");
        if (s_instance == null) {
            s_instance = new CallLogsRepository();

            //create new calls observer and start it
            CallsObserver observer = new CallsObserver();
            observer.start();
        }

        return s_instance;
    }

    public Vector<CallLogElement> getLazyCallLogs() {
        Log.d(TAG, "getLazyCallLogs");
        //get lazy call-logs from device
        Vector<CallLogElement> lazyCallLogs = CallLogsDBHelper.getInstance().getDeviceLazyCallLogs();

        //get lazy call-logs from hidden database
        lazyCallLogs.addAll(CallLogsDBHelper.getInstance().getHiddenLazyCallLogs());

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
        //get the contact from cache
        CallLogElement call = getFromCache(callLogId);

        //in case the contact is not in cache
        if (call == null) {
            //get call-log from database
            call = CallLogsDBHelper.getInstance().getCallLogById(callLogId);

            //cache the call-log
            if (call != null)
                cache(call);
        }

        //return call-log element
        return call;
    }

    public boolean hideCallLogs(String hiddenContactId, String phoneNumber) {
        Log.d(TAG, "hideCallLogs");
        //insert the phone number into a collection
        Vector<String> phoneNumbers = new Vector<String>();
        phoneNumbers.add(phoneNumber);

        //get call elements by given phone number from device
        Vector<CallElement> callElements = DeviceContactsDataRepository.getInstance().getCallLogsByPhoneNumbers(phoneNumbers);

        //get contact's display name
        String displayName = ContactsRepository.getInstance().getDisplayNameByPhoneNumber(phoneNumber);

        //get call elements collection size
        int sizeOfElemenets = callElements.size();

        //in case we have no call elements
        if (sizeOfElemenets <= 0) {
            Log.d(getClass().getSimpleName(), "hideCallLogs() - no calls found for number: " + phoneNumber);

            //indicate current phone number was handled
            return true;
        }

        //holds application's call-log elements
        Vector<CallLogElement> callLogs = new Vector<CallLogElement>();

        //convert device call-log elements to application's call-log elements
        for (CallElement ce : callElements) {
            //get the current call log element
            CallLogElement call = new CallLogElement(ce);

            //set caller name in case it doesn't exist
            if (StringUtil.isNullOrEmpty(call.CallerName))
                call.CallerName = displayName;

            //add the call to call-logs collection
            callLogs.add(call);
        }

        //save calls in hidden database
        int saved = CallLogsDBHelper.getInstance().saveCallLogsToHiddenStorage(hiddenContactId, callLogs);

        //delete calls from device's database
        int deleted = CallLogsDBHelper.getInstance().deleteCallLogsFromDevice(phoneNumber);

        //reflect success in case all call-logs were hidden successfully
        return deleted == sizeOfElemenets && saved == sizeOfElemenets;
    }

    public boolean revealCallLogsByPhoneNumber(String phoneNumber) {
        Log.d(TAG, "revealCallLogsByPhoneNumber");
        Vector<CallLogElement> callLogs = CallLogsDBHelper.getInstance().getFromHiddenStorageByPhoneNum(phoneNumber);

        //in case we have no call-logs for the given number, do nothing.
        if (callLogs.size() <= 0) {
            Log.d(getClass().getSimpleName(), "revealCallLogsByPhoneNumber() - no call-logs for phone number: " + phoneNumber);

            //indicate that revealing call-logs has finished successfully
            return true;
        }

        CallLogsDBHelper.getInstance().saveCallLogsToDevice(callLogs);
        CallLogsDBHelper.getInstance().deleteCallLogsFromHiddenStorageByPhone(phoneNumber);

        return true;
    }

    public Vector<CallLogElement> getHiddenCallLogsBy(String contactId) {
        Log.d(TAG, "getHiddenCallLogsBy");
        Vector<CallLogElement> calls = CallLogsDBHelper.getInstance().getFromHiddenStorageByContactId(contactId);

        return calls;
    }

    public HashSet<String> getHiddenPhoneNumbersBy(String contactId) {
        Log.d(TAG, "getHiddenPhoneNumbersBy");
        HashSet<String> set = new HashSet<String>();

        Vector<CallLogElement> callsLogs = CallLogsDBHelper.getInstance().getFromHiddenStorageByContactId(contactId);

        for (CallLogElement cle : callsLogs) {
            set.add(cle.CallerNumber);
        }

        return set;
    }

    /**
     * @see DeviceContactsDataRepository#registerContactObserver(HandsetDataRepositoryObserver);
     */
    public void setCallogObserver(HandsetDataRepositoryObserver observer) {
        Log.d(TAG, "setCallogObserver");
        DeviceContactsDataRepository.getInstance().registerContactObserver(observer);
    }

    /**
     * Returns a collection of call-logs that match the filter criteria.
     *
     * @param filterStr (String) The string by which to filter the call-logs.
     * @return (Vector<Conversations>) a collection of CallLogElement objects that match the filter criteria.
     */
    public Vector<CallLogElement> getFilteredCallLogs(String filterStr) {
        Log.d(TAG, "getFilteredCallLogs");
        //get all call-logs
        Vector<CallLogElement> callLogs = getLazyCallLogs();

        //holds the filtered call-logs
        Vector<CallLogElement> filteredCallLogs = new Vector<CallLogElement>();

        //find call-logs that match the filter criteria
        for (CallLogElement call : callLogs) {
            //in case call-log's caller name or caller number contain the filter string
            if ((call.CallerName != null && call.CallerName.toLowerCase().contains(filterStr.trim().toLowerCase())) ||
                    (call.CallerNumber != null && call.CallerNumber.toLowerCase().contains(filterStr.trim().toLowerCase())))
                //add the conversation to filtered conversations collection
                filteredCallLogs.add(call);
        }

        return filteredCallLogs;
    }

    /**
     * Rehides call-logs from hidden contacts that are registered on device's database
     * (for example, after making a call from the application).
     */
    public void rehideCalls() {
        Log.d(TAG, "rehideCalls");
        //for each contact id in hidden contact ids collection
        BouncerUserSession session = BouncerApplication.getApplication().getUserSession();
        for (String hiddenId : session.MarkedAsHiddenContactIds) {
            //get hidden contact's phones
            Vector<String> phones = new Vector<String>(getHiddenPhoneNumbersBy(hiddenId));

            //get call-logs by phones
            Vector<CallElement> calls = DeviceContactsDataRepository.getInstance().getCallLogsByPhoneNumbers(phones);

            //in case we have no calls to re-hide, there is nothing to do.
            if (calls == null || calls.size() <= 0)
                return;

            //for each call
            for (CallElement call : calls) {
                //hide the call
                hideCallLogs(hiddenId, call.getCallerNumber());
            }
        }
    }
}
