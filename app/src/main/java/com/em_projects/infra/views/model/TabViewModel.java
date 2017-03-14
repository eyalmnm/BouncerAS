package com.em_projects.infra.views.model;

import android.util.Log;

import java.io.Serializable;

@SuppressWarnings("serial")
public class TabViewModel implements Serializable {
    private static final String TAG = "TabViewModel";

    public final String TabType;
    public final int TabIndicatorResourceId;
    public final String TabText;
    public final boolean IsSelected;

    public TabViewModel(String tabType, int tabIndicatorResourceId, String text, boolean isSelected) {
        Log.d(TAG, "TabViewModel");
        TabType = tabType;
        TabIndicatorResourceId = tabIndicatorResourceId;
        TabText = text;
        IsSelected = isSelected;
    }


}
