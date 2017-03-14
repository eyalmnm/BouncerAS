package com.em_projects.bouncer.views.model;

import android.util.Log;

import com.em_projects.bouncer.R;
import com.em_projects.infra.views.buildercontrollers.TabViewBuilderController;
import com.em_projects.infra.views.model.TabHostViewModel;
import com.em_projects.infra.views.model.TabViewModel;

import java.util.Vector;

public class MainScreenViewModel extends TabHostViewModel {
    private static final String TAG = "MainScreenViewModel";

    public MainScreenViewModel(Vector<TabViewBuilderController<? extends TabViewModel>> tabs) {
        super(tabs, R.layout.tab_host_layout, R.id.tabhost);
        Log.d(TAG, "MainScreenViewModel");
    }
}
