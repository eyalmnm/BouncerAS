package com.em_projects.infra.views.buildercontrollers;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost.TabContentFactory;
import android.widget.TextView;

import com.em_projects.infra.application.BasicApplication;
import com.em_projects.infra.views.builders.ViewBuilder;
import com.em_projects.infra.views.controllers.ViewController;
import com.em_projects.infra.views.model.TabViewModel;

public abstract class TabViewBuilderController<T extends TabViewModel> extends ViewBuilder<T> implements TabContentFactory, ViewController {
    private static final String TAG = "TabViewBuilderCntrlr";

    public TabViewBuilderController(T model) {
        super(model);
        Log.d(TAG, "TabViewBuilderController");
    }

    @Override
    public final View createTabContent(String tag) {
        Log.d(TAG, "createTabContent");
        LayoutInflater inflater = (LayoutInflater) BasicApplication.getApplication().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View tabContent = getMainLayout(inflater);
        getViewController().attachController(tabContent);

        return tabContent;
    }

    public View getTabIndicator(LayoutInflater inflater) {
        Log.d(TAG, "getTabIndicator");
        TextView tabIndicator = new TextView(inflater.getContext());
        tabIndicator.setTextColor(Color.BLACK);
        tabIndicator.setText(getModel().TabText);
        tabIndicator.setBackgroundResource(getModel().TabIndicatorResourceId);
        tabIndicator.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

        return tabIndicator;
    }
}
