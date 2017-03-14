package com.cellebrite.ota.socialphonebook.repositories.handsetdata;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.CallLog;

import com.cellebrite.ota.socialphonebook.repositories.RepositoriesUtils;
import com.cellebrite.ota.socialphonebook.repositories.RepositoriesUtils.HandlerCreatedObserver;
import com.cellebrite.ota.socialphonebook.repositories.handsetdata.DeviceContactsDataRepository.HandsetDataKinds;
import com.em_projects.infra.application.BasicApplication;
import com.em_projects.utils.Utils;

public class CallsObserver extends Thread implements HandlerCreatedObserver {
    //holds the time to sleep before notify about the change
    private static final int SLEEP_TIME_BEFORE_NOTIFY_CHANGE = 2 * 1000;
    //holds the only instance for this class
    private ContentObserver m_androidContentObserver;
    //holds whether the thread is alive
    private boolean b_isAlive = true;
    //holds the last time that a change occur
    private long m_timeOfLastChange = 0;//holds the time passed since the time the call occur

    /**
     * Default CTOR.
     */
    public CallsObserver() {
        //set to minimum priority
        setPriority(Thread.MIN_PRIORITY);
    }


    /**
     * kill the contact change task thread
     */
    void killTask() {
        b_isAlive = false;
    }

    @Override
    public synchronized void start() {
        RepositoriesUtils.getHandlerForContentObservers(this);
        super.start();
    }


    @Override
    public void run() {
        //as long as the thread is alive
        while (b_isAlive) {
            try {
                synchronized (this) {
                    //check if last change time is not valid need to wait
                    if (m_timeOfLastChange == 0) {
                        //#ifdef DEBUG
                        Utils.debug("CallLogChangeTask.run() - wait().");
                        //#endif

                        wait();

                        //#ifdef DEBUG
                        Utils.debug("CallLogChangeTask.run() - notify().");
                        //#endif
                    }
                }

                //check the time from last change
                long passedTime = System.currentTimeMillis() - m_timeOfLastChange > SLEEP_TIME_BEFORE_NOTIFY_CHANGE ? 0 : System.currentTimeMillis() - m_timeOfLastChange;

                //sleep before start handle the current change
                Thread.sleep(SLEEP_TIME_BEFORE_NOTIFY_CHANGE - passedTime);

                //check the new time after sleep
                passedTime = System.currentTimeMillis() - m_timeOfLastChange;

                //if the time from last change is less than the minimum sleep time
                if (passedTime >= SLEEP_TIME_BEFORE_NOTIFY_CHANGE) {
                    //notify for change in calls table
                    notifyChageInCalls();

                    //initialize the time after notify
                    m_timeOfLastChange = 0;
                }
            } catch (Exception e) {
                //#ifdef ERROR
                Utils.error("CallLogObserver.onChange error: " + e);
                //#endif
            }
        }
    }

    /**
     * Creates the Android's content observer.
     */
    private void createAndroidContentObserver(Handler handler) {
        //create a new content observer
        m_androidContentObserver = new ContentObserver(handler) {
            @Override
            public boolean deliverSelfNotifications() {
                return false;
            }

            /**
             * Called when a change occurs to the cursor that is being observed.
             */
            @Override
            public void onChange(boolean selfChange) {
                //#ifdef DEBUG
                Utils.debug("CallLogObserver.onChange : change in ALL_CALLS. isSelf = " + selfChange);
                //#endif

                //notify for new change in calls table
                addChange(selfChange);

            }
        };

        //holds the content resolver
        ContentResolver cr = BasicApplication.getApplication().getContentResolver();

        //register the created Android observer
        cr.registerContentObserver(CallLog.Calls.CONTENT_URI, false, m_androidContentObserver);

    }

    private void notifyChageInCalls() {
        DeviceContactsDataRepository.getInstance().notifyObservers(HandsetDataKinds.CALLLOG);
    }

    synchronized void addChange(boolean isSelfChange) {
        //set the time of the change
        m_timeOfLastChange = System.currentTimeMillis();

        //notify for the new change
        notify();

        //#ifdef DEBUG
        Utils.debug("CallLogObserver.addChange() time =  " + m_timeOfLastChange);
        //#endif
    }


    @Override
    public void onCreateHandlerCompleted(Handler handler) {
        createAndroidContentObserver(handler);
    }
}