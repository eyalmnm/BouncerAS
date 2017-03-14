package com.em_projects.bouncer.views.model;

import android.content.Context;
import android.util.Log;

import com.em_projects.bouncer.BouncerApplication;
import com.em_projects.bouncer.R;

public class FirstLoginScreenViewModel {
    private static final String TAG = "FirstLoginScreenVwMdl";
    public final String Title;
    public final String CreatePasswordLabel;
    public final String ConfirmPasswordLabel;
    public final String ButtonText;
    private Context m_context;

    public FirstLoginScreenViewModel() {
        Log.d(TAG, "FirstLoginScreenViewModel");
        m_context = BouncerApplication.getApplication().getActivityInForeground();

        Title = m_context.getString(R.string.first_login);
        CreatePasswordLabel = m_context.getString(R.string.create_password);
        ConfirmPasswordLabel = m_context.getString(R.string.confirm_password);
        ButtonText = m_context.getString(R.string.login);
    }
}
