package com.em_projects.bouncer.views.model;

import android.content.Context;
import android.util.Log;

import com.em_projects.bouncer.BouncerApplication;
import com.em_projects.bouncer.R;


public class ChangePasswordScreenViewModel {
    private static final String TAG = "ChangePwdScrnVwModel";

    public final String Title;
    public final String CurrentPasswordLabel;
    public final String ChangePasswordLabel;
    public final String ConfirmPasswordLabel;
    public final String ButtonText;
    private Context m_context;

    /**
     * Ctor.
     */
    public ChangePasswordScreenViewModel() {
        Log.d(TAG, "ChangePasswordScreenViewModel");
        m_context = BouncerApplication.getApplication().getActivityInForeground();

        Title = m_context.getString(R.string.change_password);
        CurrentPasswordLabel = m_context.getString(R.string.current_password);
        ChangePasswordLabel = m_context.getString(R.string.change_password);
        ConfirmPasswordLabel = m_context.getString(R.string.confirm_password);
        ButtonText = m_context.getString(R.string.save);
    }
}
