package com.em_projects.infra.application;

import android.app.Application;
import android.os.Handler;
import android.util.Log;

import com.em_projects.infra.activity.BasicActivity;

import java.util.Hashtable;

public abstract class BasicApplication<T extends UserSession> extends Application {

    private static final String TAG = "BasicApplication";

    private static BasicApplication<?> s_instance;
    private final Hashtable<Class<? extends BasicActivity>, BasicActivity> m_activities = new Hashtable<Class<? extends BasicActivity>, BasicActivity>();
    private T m_userSession;
    private Handler m_mainThreadHandler = null;
    private BasicActivity m_foregroundActivity;

    public static BasicApplication<?> getApplication() {
        if (s_instance == null)
            throw new RuntimeException("vario infrastructure - Missing Application instance!");

        return s_instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        s_instance = this;
        m_mainThreadHandler = new Handler();
        m_userSession = createUserSession();
    }

    @SuppressWarnings({"hiding", "unchecked"})
    public <T> T getUserSession() {
        Log.d(TAG, "getUserSession");
        return (T) m_userSession;
    }

    public final synchronized void storeUserSession() {
        Log.d(TAG, "storeUserSession");
        m_userSession.persist();
    }

    public final synchronized void restoreUserSession() {
        Log.d(TAG, "restoreUserSession");
        m_userSession.restore();
    }

    public final void runOnMainThread(Runnable r) {
        Log.d(TAG, "runOnMainThread");
        m_mainThreadHandler.post(r);
    }

    public final void notifyActivityInForeground(BasicActivity ba) {
        Log.d(TAG, "notifyActivityInForeground");
        m_foregroundActivity = ba;
    }

    public final BasicActivity getActivityInForeground() {
        Log.d(TAG, "getActivityInForeground");
        return m_foregroundActivity;
    }

    public final void registerActivity(BasicActivity basicActivity) {
        Log.d(TAG, "registerActivity");
        m_activities.put(basicActivity.getClass(), basicActivity);
    }

    public final BasicActivity getStackedActivity(Class<? extends BasicActivity> clazz) {
        Log.d(TAG, "BasicActivity");
        return m_activities.get(clazz);
    }

    public final void removeActivity(BasicActivity activity) {
        Log.d(TAG, "removeActivity");
        m_activities.remove(activity.getClass());
    }

    protected abstract T createUserSession();
}
