package com.em_projects.bouncer;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.cellebrite.ota.socialphonebook.utilities.StringUtilities;
import com.em_projects.bouncer.repositories.ContactsRepository;
import com.em_projects.bouncer.utils.Utils;
import com.em_projects.infra.activity.BasicActivity;

import java.util.Vector;

public class BouncerSettingsActivity extends PreferenceActivity {

    private static final String TAG = "BouncerSettingsActivity";

    private BouncerUserSession m_userSession;
    private boolean b_isPrivacyOn;
    private int m_passwordTimer;
    private long m_lastActivityPauseTime, m_newActivityResumeTime;
    private double m_deltaBetweenActivities;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        final Context context = this;

        // hold the BouncerUserSession
        m_userSession = BouncerApplication.getApplication().getUserSession();

        // get the layout
        addPreferencesFromResource(R.layout.settings);

        // get preferences
        CheckBoxPreference privacyStatus = (CheckBoxPreference) findPreference("privacy_mode_check_box");
        final CheckBoxPreference requestPasswordEveryLaunch = (CheckBoxPreference) findPreference("request_password_every_launch");
        final CheckBoxPreference lockingTimer = (CheckBoxPreference) findPreference("locking_timer");
        final Preference changeLockingTimerButton = (Preference) findPreference("change_locking_timer");
        Preference changePasswordButton = (Preference) findPreference("change_password");

        // get privacy ststus from user session
        b_isPrivacyOn = m_userSession.IsPrivacyOn;

        // set checked to privacy mode
        privacyStatus.setChecked(b_isPrivacyOn);

        //set listener for privacy checkbox
        privacyStatus.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // start gauge
                BouncerActivity.startGauge(context);

                //get new checkbox check value
                b_isPrivacyOn = ((Boolean) newValue).booleanValue();

                //hold marked-as-hidden contact ids in a temp vector
                Vector<String> markedContactIds = new Vector<String>(m_userSession.MarkedAsHiddenContactIds);

                //go over 'marked-as-hidden' contacts
                for (String id : markedContactIds) {
                    //remove the current id from marked-as-hidden collection
                    m_userSession.MarkedAsHiddenContactIds.remove(id);

                    //holds new contact's id
                    String newId;

                    //lock all observers
                    BouncerActivity.lockObservers();

                    //in case we switch the privacy ON, hide all marked contacts
                    if (b_isPrivacyOn)
                        newId = ContactsRepository.getInstance().hide(id);
                        //in case we switch the privacy OFF, reveal all marked contacts
                    else
                        newId = ContactsRepository.getInstance().reveal(id);

                    //unlock observers
                    BouncerActivity.unlockObservers();

                    //add new contact's id to marked-as-hidden collection
                    if (newId != null)
                        m_userSession.MarkedAsHiddenContactIds.add(newId);

                    //start all services
                    BouncerActivity.startAllServices();
                }

                //update new value in user session
                m_userSession.IsPrivacyOn = b_isPrivacyOn;

                // save user session
                BouncerApplication.getApplication().storeUserSession();
                m_userSession.storeMarkedContactIds();

                //stop gauge in case there are no contacts to hide/reveal
                if (markedContactIds == null || markedContactIds.isEmpty())
                    BasicActivity.stopGauge();

                return true;
            }
        });

        // if 'requestPasswordEveryLaunch' is checked 'lockingTimer' is disabled
        lockingTimer.setEnabled(!requestPasswordEveryLaunch.isChecked());
        // if 'lockingTimer' is not checked 'changeLockingTimer' is disabled
        changeLockingTimerButton.setEnabled(lockingTimer.isChecked());

        // set summary to lockingTimer checkBox
        final StringBuffer summary = new StringBuffer();
        int minutes = m_userSession.RequestPasswordTime;
        String minutesStr = String.valueOf(minutes);
        summary.append(getString(R.string.request_password_time)).append(" ").
                append(minutesStr).append(" ").append(getString(R.string.minutes));
        lockingTimer.setSummary(summary.toString());

        // set the changes when requestPasswordEveryLaunch checkBox is clicked
        requestPasswordEveryLaunch.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // bNewValue is true if checkBox is checked, else false
                boolean bNewValue = ((Boolean) newValue).booleanValue();

                // if 'requestPasswordEveryLaunch' is checked
                // 'lockingTimer' and 'changeLockingTimer' are disable
                lockingTimer.setEnabled(!bNewValue);
                lockingTimer.setChecked(!bNewValue);
                changeLockingTimerButton.setEnabled(!bNewValue);

                // update user session
                m_userSession.IsAutomaticLock = bNewValue;
                m_userSession.IsRequestPasswordTimer = lockingTimer.isChecked() ? true : false;
                BouncerApplication.getApplication().storeUserSession();

                return true;
            }
        });

        // set the changes when lockingTimer checkBox is clicked
        lockingTimer.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // bNewValue is true if checkBox is checked, else false
                boolean bNewValue = ((Boolean) newValue).booleanValue();

                // if 'lockingTimer' is not checked 'changeLockingTimer' is disable
                changeLockingTimerButton.setEnabled(bNewValue);

                // update user session
                m_userSession.IsRequestPasswordTimer = bNewValue;
                BouncerApplication.getApplication().storeUserSession();

                return true;
            }
        });

        // onClickListener to change the during of locking timer
        changeLockingTimerButton.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // create dialog
                final Dialog changeTimeDialog = new Dialog(BouncerSettingsActivity.this);

                // get layout for the dialog
                changeTimeDialog.setContentView(R.layout.change_time_dialog_layout);

                // set title to dialog
                changeTimeDialog.setTitle(R.string.change_time_dialog_title);

                // add hint to the edit-text in the dialog
                final EditText editTime = (EditText) changeTimeDialog.findViewById(R.id.editTime);
                String minutesStr = String.valueOf(m_userSession.RequestPasswordTime);
                editTime.setHint(minutesStr);

                // get the 'save' and 'cancel' buttons
                Button saveButton = (Button) changeTimeDialog.findViewById(R.id.saveButton);
                Button cancelButton = (Button) changeTimeDialog.findViewById(R.id.cancelButton);

                // set onClickListener to the save button
                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get the text from the edit-text
                        String newTimeStr = editTime.getText().toString();

                        // if not entered new time, close dialog
                        if (StringUtilities.isNullOrEmpty(newTimeStr)) {
                            changeTimeDialog.dismiss();

                            return;
                        }

                        int newTime = Integer.valueOf(newTimeStr).intValue();

                        // if the new time is 0 or 1
                        if (newTime < 2) {
                            // display error message
                            Utils.showMessage(R.string.error, R.string.timer_two_minutes, BouncerSettingsActivity.this);

                            // clean edit-text and show the old hint
                            editTime.setText("");

                            return;
                        }

                        // save the new time in BouncerUserSession
                        m_userSession.RequestPasswordTime = newTime;
                        BouncerApplication.getApplication().storeUserSession();

                        // change the lockingTimer summary to the new time
                        summary.delete(0, summary.length());
                        summary.append(getString(R.string.request_password_time)).append(" ").
                                append(newTimeStr).append(" ").append(getString(R.string.minutes));
                        lockingTimer.setSummary(summary.toString());

                        // close the dialog
                        changeTimeDialog.dismiss();
                    }
                });

                // set onClickListener to the cancel button
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // close the dialog
                        changeTimeDialog.dismiss();
                    }
                });

                // show the dialog
                changeTimeDialog.show();

                return true;
            }
        });

        // onClickListener to change password button
        changePasswordButton.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getBaseContext(), BouncerLoginActivity.class);
                intent.putExtra(getString(R.string.change_password), true);

                // open change password screen
                BouncerApplication.getApplication().getActivityInForeground().startNewActivity(intent, false);
                return true;
            }
        });
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