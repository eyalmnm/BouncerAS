package com.em_projects.bouncer.views.model;

import android.util.Log;

import com.em_projects.bouncer.BouncerApplication;
import com.em_projects.bouncer.BouncerProperties;
import com.em_projects.bouncer.R;
import com.em_projects.bouncer.model.CallLogElement;
import com.em_projects.infra.views.model.TabViewModel;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

@SuppressWarnings("serial")
public class CallogListViewModel extends TabViewModel implements Serializable {
    private static final String TAG = "CallogListViewModel";

    private Vector<CallogViewModel> m_callLogsVMs;

    public CallogListViewModel(List<CallLogElement> calls) {
        super(BouncerProperties.TAB_TYPE.CALL_LOGS, R.drawable.tab_item, BouncerApplication.getApplication().getString(R.string.calls_tab_label), false);
        Log.d(TAG, "CallogListViewModel");

        m_callLogsVMs = new Vector<CallogViewModel>();

        for (CallLogElement call : calls) {
            //protect from null entries in the collection
            if (call == null)
                continue;

            m_callLogsVMs.add(new CallogViewModel(call));
        }
    }

    public Vector<CallogViewModel> getCallLogsViewModels() {
        Log.d(TAG, "getCallLogsViewModels");
        return m_callLogsVMs;
    }
}
