package com.em_projects.infra.views.builders;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;

import com.em_projects.infra.views.controllers.ViewController;

public abstract class ViewBuilder<T extends Object> {
    private static final String TAG = "ViewBuilder";

    private T m_model;

    public ViewBuilder(T model) {
        Log.d(TAG, "ViewBuilder");
        m_model = model;
    }

    ;

    /**
     * Called on UI thread for building view.
     *
     * @return {@link View}
     */
    public abstract View getMainLayout(LayoutInflater inflater);

    /**
     * Called on UI thread for refreshing view.
     */
    public abstract void refreshView(View mainlayout, Object newModel);

    /**
     * Called on UI thread for attching controller to the returned view by {@link #getMainLayout(LayoutInflater)}.
     *
     * @return {@link ViewController}
     */
    public abstract ViewController getViewController();

    /**
     * @return the model.
     */
    public final T getModel() {
        Log.d(TAG, "getModel");
        return m_model;
    }

    /**
     * Returns whether the view should be stacked.
     *
     * @return false by default.
     */
    public boolean isViewBackable() {
        Log.d(TAG, "isViewBackable");
        return false;
    }

    /**
     * Add menu items relevant to this builder's view.
     *
     * @param menu
     */
    public void addToOptionsMenu(Menu menu) {

    }

}
