package com.em_projects.bouncer.views.buildercontrollers;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.em_projects.bouncer.BouncerApplication;
import com.em_projects.bouncer.views.model.MainScreenViewModel;
import com.em_projects.infra.views.builders.TabHostViewBuilder;
import com.em_projects.infra.views.controllers.ViewController;
import com.em_projects.utils.Utils;

public class MainScreenViewBuilderController extends TabHostViewBuilder<MainScreenViewModel> implements ViewController {
    private static final String TAG = "MainScreenVwBldrCntrlr";

    public MainScreenViewBuilderController(MainScreenViewModel model) {
        super(model);
        Log.d(TAG, "MainScreenViewBuilderController");
    }

    @Override
    public void refreshView(View mainlayout, Object newModel) {
        Log.d(TAG, "refreshView");
        //hide keyboard if active
        InputMethodManager imm = (InputMethodManager) BouncerApplication.getApplication().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mainlayout.getApplicationWindowToken(), 0);

        super.refreshView(mainlayout, newModel);
    }

    @Override
    public ViewController getViewController() {
        Log.d(TAG, "getViewController");
        return this;
    }

    @Override
    public boolean isViewBackable() {
        Log.d(TAG, "isViewBackable");
        return true;
    }


    @Override
    public void attachController(View view) {
        Log.d(TAG, "attachController");
        //nothing to do here!
    }


    @Override
    public boolean onBackKeyPressed() {
        Log.d(TAG, "onBackKeyPressed");
        Utils.showExitMessage(BouncerApplication.getApplication().getActivityInForeground());

        return true;
    }
}
