package com.em_projects.infra.repositories;

import android.util.Log;

import com.em_projects.infra.model.Entity;

import java.util.Hashtable;

public abstract class EntityRepositoryWithCache<T extends Entity<UID>, UID> {
    private static final String TAG = "EntityRepositoryWithCch";

    private Hashtable<UID, T> m_cache = new Hashtable<UID, T>();

    protected final void cache(T entity) {
        Log.d(TAG, "cache");
        m_cache.put(entity.getUID(), entity);
    }

    public final void removeFromCache(UID uid) {
        Log.d(TAG, "removeFromCache");
        m_cache.remove(uid);
    }

    protected final void clearCache() {
        Log.d(TAG, "clearCache");
        m_cache.clear();
    }

    protected final T getFromCache(UID uid) {
        Log.d(TAG, "getFromCache");
        return m_cache.get(uid);
    }
}
