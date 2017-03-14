package com.em_projects.bouncer.views.model;

import android.content.Context;
import android.util.Log;

import com.em_projects.bouncer.BouncerApplication;
import com.em_projects.bouncer.R;

public class LoginScreenViewModel {
    private static final String TAG = "LoginScreenViewModel";

    public final String Title;
    public final String EnterPasswordLabel;
    public final String ButtonText;
    private Context m_context;

    public LoginScreenViewModel() {
        Log.d(TAG, "LoginScreenViewModel");
        m_context = BouncerApplication.getApplication().getActivityInForeground();

        Title = m_context.getString(R.string.login);
        EnterPasswordLabel = m_context.getString(R.string.enter_password);
        ButtonText = m_context.getString(R.string.login);
    }
}
