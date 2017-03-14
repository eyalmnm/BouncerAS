package com.em_projects.bouncer.views.controllers;

import android.util.Log;
import android.view.View;

import com.em_projects.infra.views.controllers.ViewController;

public class TextScreenViewController implements ViewController {
    private static final String TAG = "TextScreenViewCntrlr";

    @Override
    public void attachController(View view) {
        //nothing to do here
    }

    @Override
    public boolean onBackKeyPressed() {
        Log.d(TAG, "onBackKeyPressed");
        return false;
    }
}
