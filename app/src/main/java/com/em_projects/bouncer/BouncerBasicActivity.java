package com.em_projects.bouncer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.em_projects.infra.activity.BasicActivity;

public class BouncerBasicActivity extends BasicActivity {

    private static final String TAG = "BouncerBasicActivity";

    private BouncerUserSession m_userSession;
    private int m_passwordTimer;
    private long m_lastActivityPauseTime, m_newActivityResumeTime;
    private double m_deltaBetweenActivities;

    /**
     * Called when the activity is first created.
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");

        // if user want to request password timer when application in background
        if (m_userSession.IsAutomaticLock || m_userSession.IsRequestPasswordTimer) {
            // hold the time now
            BouncerProperties.LAST_ON_PAUSE_TIME = System.currentTimeMillis();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        // hold the BouncerUserSession
        m_userSession = BouncerApplication.getApplication().getUserSession();

        // if user want to request password timer when application in background
        if (m_userSession.IsAutomaticLock || m_userSession.IsRequestPasswordTimer) {
            m_passwordTimer = m_userSession.RequestPasswordTime;
            m_lastActivityPauseTime = BouncerProperties.LAST_ON_PAUSE_TIME;
            m_newActivityResumeTime = System.currentTimeMillis();

            //delta between activities in seconds
            m_deltaBetweenActivities = (m_newActivityResumeTime - m_lastActivityPauseTime) / 60000;

            // in case of automatic lock:
            // if the delta between activities is longer than 0.5 minute,
            // or in case of timer lock:
            // if the delta between activities is longer than timer,
            // start login screen!
            if ((m_userSession.IsAutomaticLock && m_deltaBetweenActivities > 0.5) ||
                    (m_userSession.IsRequestPasswordTimer && m_deltaBetweenActivities > m_passwordTimer)) {
                BasicActivity lastActivity = BouncerApplication.getApplication().getActivityInForeground();
                Intent intent = new Intent(BouncerApplication.getApplication().getApplicationContext(), BouncerLoginActivity.class);
                lastActivity.startNewActivity(intent, true);
            }
        }
    }
}
