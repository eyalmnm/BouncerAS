package com.em_projects.bouncer.registration;

import android.app.Activity;

/**
 * Created by eyalmuchtar on 14/03/2017.
 */

public class RegistrationActivity extends Activity implements RegistrationView {
    public static final String TAG = "RegistrationActivity";

    private RegistrationPresenter presenter;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != presenter) {
            presenter.onDestroy();
        }
    }
}
