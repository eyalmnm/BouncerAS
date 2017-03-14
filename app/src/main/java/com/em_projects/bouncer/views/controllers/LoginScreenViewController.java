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
import com.em_projects.bouncer.utils.Utils;
import com.em_projects.infra.views.controllers.ViewController;

public class LoginScreenViewController implements ViewController {
    private static final String TAG = "LoginScreenViewCntrlr";

    @Override
    public void attachController(View view) {
        Log.d(TAG, "attachController");
        final Button button = (Button) view.findViewById(R.id.button);
        final EditText password = (EditText) view.findViewById(R.id.editText1);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BouncerUserSession userSession = BouncerApplication.getApplication().getUserSession();

                String passwordStr = password.getText().toString();
                String myPassword = userSession.Password;

                // if the text-field is empty
                if (StringUtilities.isNullOrEmpty(passwordStr)) {
                    // display error message
                    Utils.showMessage(R.string.error, R.string.empty_password, BouncerApplication.getApplication().getActivityInForeground());

                    return;
                }

                //if entered incorrect password
                if (!passwordStr.equals(myPassword)) {
                    // display error message
                    Utils.showMessage(R.string.error, R.string.incorrect_old_password, BouncerApplication.getApplication().getActivityInForeground());

                    // clean the text-field
                    password.setText("");

                    return;
                }

                // show gauge
                Utils.showGauge(BouncerApplication.getApplication().getActivityInForeground());

                // if entered correct password, start application
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
