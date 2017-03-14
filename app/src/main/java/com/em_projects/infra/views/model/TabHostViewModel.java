package com.em_projects.infra.views.model;

import android.util.Log;

import com.em_projects.infra.views.buildercontrollers.TabViewBuilderController;

import java.util.List;

public class TabHostViewModel {
    private static final String TAG = "TabHostViewModel";

    public final int RootLayoutResourceID;
    public final int TabHostResourceID;
    public final List<TabViewBuilderController<? extends TabViewModel>> ViewBuilderControllers;

    public TabHostViewModel(List<TabViewBuilderController<? extends TabViewModel>> viewBuilderControllers, int rootLayoutId, int tabHostId) {
        Log.d(TAG, "TabHostViewModel");
        TabHostResourceID = tabHostId;
        RootLayoutResourceID = rootLayoutId;
        ViewBuilderControllers = viewBuilderControllers;
    }
}
