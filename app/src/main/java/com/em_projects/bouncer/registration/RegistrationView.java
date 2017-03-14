package com.em_projects.bouncer.registration;

/**
 * Created by eyalmuchtar on 14/03/2017.
 */

public interface RegistrationView {

    void showProgress();

    void hideProgress();

    void showError(String error);

    void navigateToOTP();
}
