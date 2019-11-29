package com.em_projects.bouncer.views.controllers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.cellebrite.ota.socialphonebook.utilities.StringUtilities;
import com.em_projects.bouncer.BouncerActivity;
import com.em_projects.bouncer.BouncerApplication;
import com.em_projects.bouncer.BouncerUserSession;
import com.em_projects.bouncer.R;
import com.em_projects.infra.views.controllers.ViewController;
import com.em_projects.utils.Utils;

public class FirstLoginScreenViewController implements ViewController {
    private static final String TAG = "FirstLoginScrnVwCntrlr";

    @Override
    public void attachController(View view) {
        Log.d(TAG, "attachController");
        final Button loginButton = view.findViewById(R.id.button);
        final EditText password = view.findViewById(R.id.editText1);
        final EditText confirmPassword = view.findViewById(R.id.editText2);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String passwordStr = password.getText().toString();
                String confirmPasswordStr = confirmPassword.getText().toString();

                // if one of the text-fields is empty
                if (StringUtilities.isNullOrEmpty(passwordStr) ||
                        StringUtilities.isNullOrEmpty(confirmPasswordStr)) {
                    // display error message
                    Utils.showMessage(R.string.error, R.string.empty_fields, BouncerApplication.getApplication().getActivityInForeground());

                    return;
                }

                // if verification was not successful
                if (!passwordStr.equals(confirmPasswordStr)) {
                    // display error message
                    Utils.showMessage(R.string.error, R.string.incorrect_new_password, BouncerApplication.getApplication().getActivityInForeground());

                    // clean the text-fields
                    password.setText("");
                    confirmPassword.setText("");

                    // focus on password text-field
                    password.requestFocus();

                    return;
                }

                // show gauge
                Utils.showGauge(BouncerApplication.getApplication().getActivityInForeground());

                // if verification was successful, save password and start application
                BouncerUserSession userSession = BouncerApplication.getApplication().getUserSession();
                userSession.Password = passwordStr;
                BouncerApplication.getApplication().storeUserSession();

                Context context = BouncerApplication.getApplication().getActivityInForeground().getBaseContext();
                Intent intent = new Intent(context, BouncerActivity.class);
                BouncerApplication.getApplication().getActivityInForeground().startNewActivity(intent, true);
            }
        });
    }

    @Override
    public boolean onBackKeyPressed() {
        Log.d(TAG, "onBackKeyPressed");
        BouncerApplication.getApplication().getActivityInForeground().finish();

        return true;
    }
}
