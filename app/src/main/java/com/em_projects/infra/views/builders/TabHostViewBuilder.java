package com.em_projects.infra.views.builders;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

import com.em_projects.infra.application.BasicApplication;
import com.em_projects.infra.views.buildercontrollers.TabViewBuilderController;
import com.em_projects.infra.views.model.TabHostViewModel;
import com.em_projects.infra.views.model.TabViewModel;

import java.util.Hashtable;

public abstract class TabHostViewBuilder<T extends TabHostViewModel> extends ViewBuilder<TabHostViewModel> {
    private static final String TAG = "TabHostViewBuilder";

    //holds a map between a tag to a tab screen
    protected Hashtable<String, TabViewBuilderController<? extends TabViewModel>> m_tabsMap = new Hashtable<String, TabViewBuilderController<? extends TabViewModel>>();

    public TabHostViewBuilder(TabHostViewModel model) {
        super(model);
        Log.d(TAG, "TabHostViewBuilder");
    }

    @Override
    public final View getMainLayout(LayoutInflater inflater) {
        Log.d(TAG, "getMainLayout");
        TabHostViewModel model = getModel();

        View rootLayout = inflater.inflate(model.RootLayoutResourceID, null);

        //get the tab host from an external layout
        final TabHost tabsHost = (TabHost) rootLayout.findViewById(model.TabHostResourceID);
        tabsHost.setup();

        //set a tab change listener
        tabsHost.setOnTabChangedListener(new OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                //hide soft keyboard if active
                InputMethodManager imm = (InputMethodManager) BasicApplication.getApplication().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(tabsHost.getApplicationWindowToken(), 0);
            }
        });

        //for each tab in the array
        for (TabViewBuilderController<? extends TabViewModel> tab : model.ViewBuilderControllers) {
            //create a tag
            String tag = tab.getModel().TabType;

            //create a new tab specifications
            TabHost.TabSpec spec = tabsHost.newTabSpec(tag);

            //set the tab indicator (get it from the actual tab)
            spec.setIndicator(tab.getTabIndicator(inflater));

            //set the tabs content factory
            spec.setContent(tab);

            //map between the tag and it's screen
            m_tabsMap.put(tag, tab);

            //add the tab to the host
            tabsHost.addTab(spec);
        }

        return rootLayout;
    }

    @Override
    public void refreshView(View mainlayout, Object newModel) throws ClassCastException {
        Log.d(TAG, "refreshView");
        //holds the given model
        TabViewModel newTabViewModel = null;

        //try parsing the model to TabViewModel, this is what we should get here.
        try {
            newTabViewModel = (TabViewModel) newModel;
        } catch (ClassCastException cce) {
            throw cce;
        }

        //get the tab host from an external layout
        TabHost tabsHost = (TabHost) mainlayout.findViewById(getModel().TabHostResourceID);

        //get the inner content view
        View tabContent = tabsHost.getTabContentView();

        //refresh the view that the new model belongs to
        (m_tabsMap.get(newTabViewModel.TabType)).refreshView(tabContent, newModel);
    }
}
