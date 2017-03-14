package com.em_projects.bouncer.views.controllers;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.cellebrite.ota.socialphonebook.utilities.StringUtilities;
import com.em_projects.bouncer.BouncerApplication;
import com.em_projects.bouncer.BouncerUserSession;
import com.em_projects.bouncer.R;
import com.em_projects.bouncer.utils.Utils;
import com.em_projects.infra.views.controllers.ViewController;

public class ChangePasswordScreenViewController implements ViewController {
    private static final String TAG = "ChangePwdScrnVwCntrlr";

    @Override
    public void attachController(View view) {
        Log.d(TAG, "attachController");
        Button saveButton = (Button) view.findViewById(R.id.button);
        final EditText oldPassword = (EditText) view.findViewById(R.id.editText1);
        final EditText newPassword = (EditText) view.findViewById(R.id.editText2);
        final EditText confirmNewPassword = (EditText) view.findViewById(R.id.editText3);
        final BouncerUserSession userSession = BouncerApplication.getApplication().getUserSession();

        // onClick for the save button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String myPassword = userSession.Password;
                String oldPasswordStr = oldPassword.getText().toString();
                String newPasswordStr = newPassword.getText().toString();
                String confirmNewPasswordStr = confirmNewPassword.getText().toString();

                boolean isOldPasswordValid = oldPasswordStr.equals(myPassword);
                boolean isNewPasswordValid = confirmNewPasswordStr.equals(newPasswordStr);

                // if one of the text-fields is empty
                if (StringUtilities.isNullOrEmpty(oldPasswordStr) ||
                        StringUtilities.isNullOrEmpty(newPasswordStr) ||
                        StringUtilities.isNullOrEmpty(confirmNewPasswordStr)) {
                    // display error message
                    Utils.showMessage(R.string.error, R.string.empty_fields, BouncerApplication.getApplication().getActivityInForeground());

                    return;
                }

                // if one of the passwords is wrong
                if (!isOldPasswordValid || !isNewPasswordValid) {
                    int messageId = 0;

                    if (!isOldPasswordValid) {
                        // select the appropriate message
                        messageId = R.string.incorrect_old_password;

                        // clean the text-field
                        oldPassword.setText("");

                        // focus on oldPassword edit-text
                        oldPassword.requestFocus();
                    } else if (!isNewPasswordValid) {
                        // select the appropriate message
                        messageId = R.string.incorrect_new_password;

                        // clean the text-fields
                        newPassword.setText("");
                        confirmNewPassword.setText("");

                        // focus on newPassword edit-text
                        newPassword.requestFocus();
                    }

                    // display error dialog with the appropriate message
                    Utils.showMessage(R.string.error, messageId, BouncerApplication.getApplication().getActivityInForeground());

                    return;
                }

                // show gauge
                Utils.showGauge(BouncerApplication.getApplication().getActivityInForeground());

                // if entered correct password and verification was successful, save the new password
                userSession.Password = newPasswordStr;
                BouncerApplication.getApplication().storeUserSession();
                BouncerApplication.getApplication().getActivityInForeground().finish();
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
