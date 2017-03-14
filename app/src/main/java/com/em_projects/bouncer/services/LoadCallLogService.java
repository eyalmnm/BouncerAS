package com.em_projects.bouncer.services;

import android.util.Log;

import com.em_projects.bouncer.model.CallLogElement;
import com.em_projects.bouncer.repositories.CallLogsRepository;
import com.em_projects.bouncer.views.model.CallogListViewModel;
import com.em_projects.infra.services.AsyncService;

import java.util.Collections;
import java.util.List;

public class LoadCallLogService extends AsyncService<CallogListViewModel> {
    private static final String TAG = "LoadCallLogService";

    @Override
    protected CallogListViewModel execute() {
        Log.d(TAG, "execute");
        //get all callog ids
        List<CallLogElement> lazyCallLogs = CallLogsRepository.getInstance().getLazyCallLogs();

        //sort the call-logs
        Collections.sort(lazyCallLogs, CallLogElement.COMPARE_BY_CALL_TIME);

        //construct new call-logs list model with the collected call-logs and return it.
        return new CallogListViewModel(lazyCallLogs);
    }
}