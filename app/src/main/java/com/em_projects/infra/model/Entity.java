package com.em_projects.infra.model;

import android.util.Log;

import java.io.Serializable;

@SuppressWarnings("serial")
public abstract class Entity<T> implements Serializable {
    private static final String TAG = "Entity";

    private T m_uid = null;

    public Entity(T uid) {
        Log.d(TAG, "Entity");
        m_uid = uid;
    }

    public final T getUID() {
        Log.d(TAG, "getUID");
        return m_uid;
    }
}
