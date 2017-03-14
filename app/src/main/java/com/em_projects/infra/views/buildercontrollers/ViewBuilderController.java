package com.em_projects.infra.views.buildercontrollers;

import android.util.Log;

import com.em_projects.infra.views.builders.ViewBuilder;
import com.em_projects.infra.views.controllers.ViewController;

public abstract class ViewBuilderController<T> extends ViewBuilder<T> implements ViewController {
    private static final String TAG = "ViewBuilderController";

    public ViewBuilderController(T model) {
        super(model);
        Log.d(TAG, "ViewBuilderController");
    }
}
