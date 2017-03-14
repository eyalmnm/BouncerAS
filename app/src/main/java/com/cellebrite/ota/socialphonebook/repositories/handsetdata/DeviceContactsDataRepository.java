package com.cellebrite.ota.socialphonebook.repositories.handsetdata;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.CallLog;

import com.em_projects.infra.application.BasicApplication;
import com.em_projects.utils.Utils;

import java.util.HashSet;
import java.util.Vector;
import java.util.regex.Pattern;

/**
 * Repository for all device data (SMSs, CallLogs, Calendar)
 */
public class DeviceContactsDataRepository {
    //holds the only instance of this class
    private static DeviceContactsDataRepository s_instance = null;

    //clear any illegal characters
    private static Pattern s_pattern = null;

    //holds the list of observers to this repository
    private HashSet<HandsetDataRepositoryObserver> m_repositoryObservers = new HashSet<HandsetDataRepositoryObserver>();

    /**
     * Private CTOR.
     */
    private DeviceContactsDataRepository() {
    }

    /**
     * @return (HandsetDataRepository != null)
     */
    public synchronized static DeviceContactsDataRepository getInstance() {
        if (s_instance == null) {
            s_instance = new DeviceContactsDataRepository();

            //init the pattern
            s_pattern = Pattern.compile("[^*#0-9]+");
        }

        return s_instance;
    }

    /**
     * Returns a vector of call logs with default sort order filtered by the given phone numbers.
     *
     * @param phoneNumbers (Vector<String>) a collection of phone numbers as filter, passing null will return all logs.
     * @return (Vector<CallElement> != null)
     */
    public Vector<CallElement> getCallLogsByPhoneNumbers(Vector<String> phoneNumbers) {
        //holds the cursor to the call log table
        Cursor callLogsCurr = null;

        //holds the results
        Vector<CallElement> results = new Vector<CallElement>();

        //holds the array of phone numbers for the query
        String[] phonesArray = null;

        //if any filter was given clear the phones before sending to the query
        if (phoneNumbers != null) {
            //holds the phones in a form of array
            int phoneNumbersSize = phoneNumbers.size();
            phonesArray = new String[phoneNumbersSize];

            //clear any unnecessary chars
            for (int i = 0; i < phoneNumbersSize; i++) {
//				//get a matcher to the current value
//				Matcher matcher = s_pattern.matcher(phoneNumbers.elementAt(i));
//
//				//replace all the matches with empty string and set the cleared phone number
//				phonesArray[i] = matcher.replaceAll("");
                phonesArray[i] = cleanPhoneNumber(phoneNumbers.elementAt(i));
            }
        }

        try {
            //get the cursor filtered by the given numbers
            callLogsCurr = CallLogRepositoryHelper.performQueryOnCallLogTable(phonesArray);

            //if there is data
            if (callLogsCurr != null && callLogsCurr.moveToFirst()) {
                do {
                    //add the call log element
                    results.add(CallLogRepositoryHelper.getCallElementFromCursor(callLogsCurr));
                }
                while (callLogsCurr.moveToNext());
            } else {
                //#ifdef DEBUG
                Utils.debug("DeviceContactsDataRepository.getCallLogsByPhoneNumbers() - no call logs were found");
                //#endif
            }
        } catch (Throwable t) {
            //#ifdef ERROR
            Utils.error("DeviceContactsDataRepository.getCallLogsByPhoneNumbers() - error occur exception throwen:", t);
            //#endif
        } finally {
            //close the cursor
            if (callLogsCurr != null)
                callLogsCurr.close();
        }

        //return the results
        return results;

    }

    /**
     * @return (Vector<CallElement> != null) all call logs with the default sort order.
     */
    public Vector<CallElement> getAllCallLogs() {
        return getCallLogsByPhoneNumbers(null);
    }

    /**
     * This method deletes Call Log Item
     *
     * @param callLogID (String != null) the ID of the call log entry to delete.
     * @return (boolean) true in case the call log entry was deleted successfully from the device, false otherwise.
     */
    public boolean deleteCallLogItem(String callLogID) {
        try {
            //get the content resolver
            ContentResolver cr = BasicApplication.getApplication().getContentResolver();

            //deletes the call log item, and returns whether it was deleted
            return (cr.delete(CallLog.Calls.CONTENT_URI, CallLog.Calls._ID + "=?", new String[]{callLogID}) == 1);
        } catch (Throwable throwable) {
            //#ifdef ERROR
            Utils.error("CallLogManager.deleteCallLogItem() - an error has occurred - " +
                    throwable + ", deleting call log entry with ID " + callLogID + " failed.");

            //#endif

            return false;
        }
    }

    /**
     * Returns a list of SmsElement filtered by a given collection of phone numbers.
     *
     * @param phoneNumbers (Vector<String>) a collection of phone numbers as filter, passing null will return all SMSs.
     * @return (Vector<SmsElement>) a list of SMS elements.
     */
    public Vector<SmsElement> getSmsListByPhoneNumbers(Vector<String> phoneNumbers) {
        //holds the cursor to the sms table
        Cursor smsCurr = null;

        //holds the results
        Vector<SmsElement> results = new Vector<SmsElement>();

        //holds the array of phone numbers for the query
        String[] phonesArray = null;

        //if any filter was given clear the phones before sending to the query
        if (phoneNumbers != null) {
            //holds the phones in a form of array
            int phoneNumbersSize = phoneNumbers.size();
            phonesArray = new String[phoneNumbersSize * 2];

            //holds a new collection
            Vector<String> newCollection = new Vector<String>(phoneNumbersSize);

            //clear any unnecessary chars
            for (String phoneNumber : phoneNumbers) {
//				//get a matcher to the current value
//				Matcher matcher = s_pattern.matcher(phoneNumber);
//
//				//get the cleaned phone number
//				String cleanPhoneNumber = matcher.replaceAll("");
                String cleanPhoneNumber = cleanPhoneNumber(phoneNumber);

                //get the last eight characters
                if (cleanPhoneNumber.length() > 8)
                    cleanPhoneNumber = cleanPhoneNumber.substring(cleanPhoneNumber.length() - 8);

                //replace all the matches with empty string and set the cleared phone number
                newCollection.add(cleanPhoneNumber);

                //get the last eight characters
                if (phoneNumber.length() > 8)
                    phoneNumber = phoneNumber.substring(phoneNumber.length() - 8);

                //add without clearing the characters
                newCollection.add(phoneNumber);
            }

            //copy all values to the array
            newCollection.toArray(phonesArray);
        }

        try {
            //get the cursor filtered by the given numbers
            smsCurr = SmsRepositoryHelper.performQueryOnSmsTable(phonesArray);

            //if there is data
            if (smsCurr != null && smsCurr.moveToFirst()) {
                do {
                    //add the sms element
                    results.add(SmsRepositoryHelper.getSmsElementFromCursor(smsCurr));
                }
                while (smsCurr.moveToNext());
            } else {
                //#ifdef ERROR
                Utils.error("HandsetDataRepository.getCallLogs() - no call logs were found");
                //#endif
            }
        } catch (Throwable t) {
            //#ifdef ERROR
            Utils.error("HandsetDataRepository.getCallLogs() - error occur exception throwen:", t);
            //#endif
        } finally {
            //close the cursor
            if (smsCurr != null)
                smsCurr.close();
        }

        //return the results
        return results;

    }

    /**
     * @return (Vector<CallElement> != null) all SMSs with the default sort order.
     */
    public Vector<SmsElement> getAllSms() {
        return getSmsListByPhoneNumbers(null);
    }

    /**
     * Register an observer class that gets call backs when device data (SMS, CallLogs) will be change in the repository.
     *
     * @param observer (HandsetDataRepositoryObserver != null)-The object that receives call backs when changes occur.
     */
    public void registerContactObserver(HandsetDataRepositoryObserver observer) {
        m_repositoryObservers.add(observer);
    }

    /**
     * Unregisters a change observer.
     *
     * @param observer (HandsetDataRepositoryObserver != null) - The previously registered observer that is no longer needed.
     */
    public void unregisterContactObserver(HandsetDataRepositoryObserver observer) {
        m_repositoryObservers.remove(observer);
    }

    /**
     * Invoked when a new SMS arrived. The method forwards the change to all this repository observers.
     */
    void onSmsReceived() {
        notifyObservers(HandsetDataKinds.SMS);
    }

    public void notifyObservers(HandsetDataKinds dataKind) {
        for (HandsetDataRepositoryObserver obs : m_repositoryObservers) {
            obs.onHandsetDataChange(dataKind);
        }
    }

    public String cleanPhoneNumber(String phoneNumber) {
        // clear non-number characters
        String num = s_pattern.matcher(phoneNumber).replaceAll("");

        // if the number starts with '1', return it (1-800... etc.)
        if (num.startsWith("1"))
            return num;

        // holds number length
        int length = num.length();

        // if the number is less then 9 chars long, return it.
        if (length < 9)
            return num;

        // if the number is 9 or 10 chars long and starts with 'zero', return it.
        if ((length == 9 || length == 10) && num.startsWith("0"))
            return num;

        // get last 9 chars
        num = num.substring(length - 9);

        // add 'zero' to the beginning if needed
        if (!num.startsWith("0"))
            num = new StringBuffer("0").append(num).toString();

        return num;
    }

    /**
     * Holds the data kinds supported by the HandsetDataRepository observing mechanism.
     */
    public enum HandsetDataKinds {
        SMS, CALLLOG
    }

    /**
     * This class Receives call backs for changes in contacts.
     * Each class that want notifications about change in contacts need to implements it.
     */
    public interface HandsetDataRepositoryObserver {
        /**
         * This method is called when a change occurs in the contacts repository.
         */
        public void onHandsetDataChange(HandsetDataKinds kind);
    }
}
