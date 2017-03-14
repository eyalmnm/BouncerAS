package com.em_projects.infra.services;

import android.util.Log;

import com.em_projects.infra.activity.BasicActivity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public abstract class Service<T extends Serializable> implements Runnable {
    private static final String TAG = "Service";

    private OnServiceCompletedListener<T> m_listener = null;
    private ServiceErrorArgs m_errorArgs = null;

    public final void run() {
        Log.d(TAG, "run");
        if (m_listener != null) {
            T args = execute();

            boolean isToRunOnUIThread = m_listener.isToReturnOnUIThread() != null;

            //if error
            if (m_errorArgs != null) {
                if (isToRunOnUIThread)
                    passArgsOnUIThread(m_errorArgs, true);
                else
                    m_listener.onServiceError(m_errorArgs);
            } else if (args != null) {
                if (isToRunOnUIThread)
                    passArgsOnUIThread(args, false);
                else
                    m_listener.onServiceCompleted(args);
            }
        }
    }

    private void passArgsOnUIThread(final Object args, final boolean isError) {
        Log.d(TAG, "passArgsOnUIThread");
        final BasicActivity activity = m_listener.isToReturnOnUIThread();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @SuppressWarnings("unchecked")
                @Override
                public void run() {
                    if (isError)
                        m_listener.onServiceError((ServiceErrorArgs) args);
                    else
                        m_listener.onServiceCompleted((T) args);
                }
            });
        }
    }

    public void startService() {
        Log.d(TAG, "startService");
        run();
    }

    public final void notifyError(ServiceErrorArgs args) {
        Log.d(TAG, "notifyError");
        m_errorArgs = args;
    }

    public final void notifyProgress(T partialArgs) {
        Log.d(TAG, "notifyProgress");
        if (m_listener != null) {
            final T clone = clonePartialArgs(partialArgs);
            final BasicActivity activity = m_listener.isToReturnOnUIThread();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        m_listener.onServiceProgressChanged(clone);
                    }
                });
            } else
                m_listener.onServiceProgressChanged(partialArgs);
        }
    }

    @SuppressWarnings("unchecked")
    private T clonePartialArgs(T partialArgs) {
        Log.d(TAG, "clonePartialArgs");
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;

        try {
            ByteArrayOutputStream bab = new ByteArrayOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream(bab, 8 * 1024);
            oos = new ObjectOutputStream(bos);
            oos.writeObject(partialArgs);
            oos.flush();

            ByteArrayInputStream bais = new ByteArrayInputStream(bab.toByteArray());
            BufferedInputStream bis = new BufferedInputStream(bais, 8 * 1024);
            ois = new ObjectInputStream(bis);

            return (T) ois.readObject();

        } catch (Throwable t) {
            t.printStackTrace();

            return null;
        } finally {
            try {
                oos.close();
            } catch (Throwable t) {
                t.printStackTrace();
            }

            try {
                ois.close();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public final void setListener(OnServiceCompletedListener<T> listener) {
        Log.d(TAG, "setListener");
        m_listener = listener;
    }

    protected abstract T execute();
}
