package com.em_projects.bouncer.registration;

/**
 * Created by eyalmuchtar on 14/03/2017.
 */

public interface RegistrationPresenter {

    public void register(String neme, String phoneNumber);

    public void onDestroy();
}
