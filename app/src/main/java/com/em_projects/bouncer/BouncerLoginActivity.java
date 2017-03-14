package com.em_projects.bouncer;

import android.os.Bundle;
import android.util.Log;

import com.cellebrite.ota.socialphonebook.utilities.StringUtilities;
import com.em_projects.bouncer.views.builders.ChangePasswordScreenViewBuilder;
import com.em_projects.bouncer.views.builders.FirstLoginScreenViewBuilder;
import com.em_projects.bouncer.views.builders.LoginScreenViewBuilder;
import com.em_projects.bouncer.views.model.ChangePasswordScreenViewModel;
import com.em_projects.bouncer.views.model.FirstLoginScreenViewModel;
import com.em_projects.bouncer.views.model.LoginScreenViewModel;

public class BouncerLoginActivity extends BouncerBasicActivity {

    private static final String TAG = "BouncerLoginActivity";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        // get the password from user session
        BouncerUserSession session = BouncerApplication.getApplication().getUserSession();
        String password = session.Password;

        // if the password is empty, set model and builder for first login screen
        if (StringUtilities.isNullOrEmpty(password)) {
            FirstLoginScreenViewModel model = new FirstLoginScreenViewModel();
            FirstLoginScreenViewBuilder builder = new FirstLoginScreenViewBuilder(model);
            showView(builder);
        }

        // if it is a request for change password, set model and builder for change-password screen
        else if (getIntent().getBooleanExtra(getString(R.string.change_password), false)) {
            ChangePasswordScreenViewModel model = new ChangePasswordScreenViewModel();
            ChangePasswordScreenViewBuilder builder = new ChangePasswordScreenViewBuilder(model);
            showView(builder);
        }

        // set model and builder for regular login screen
        else {
            LoginScreenViewModel model = new LoginScreenViewModel();
            LoginScreenViewBuilder builder = new LoginScreenViewBuilder(model);
            showView(builder);
        }
    }
}
