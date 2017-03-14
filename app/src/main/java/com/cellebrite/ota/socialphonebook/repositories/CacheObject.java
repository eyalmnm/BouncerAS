package com.cellebrite.ota.socialphonebook.repositories;

public class CacheObject<T> {
    //holds the cache state
    private CacheState m_cachestate;

    ;
    //holds the last loaded cache time
    private long m_cachetimeStamp;
    //holds the cache object
    private T m_data;

    /**
     * Ctor. set new cache data
     *
     * @param state     (CacheState != null) the cache state
     * @param cacheData (T ) - data to cache
     */
    public CacheObject(CacheState state, T cacheData) {
        setData(state, cacheData);
    }

    /**
     * @param (cachestate != null) the cache state
     */
    public void seState(CacheState cachestate) {
        m_cachestate = cachestate;
    }

    /**
     * @return the m_cachestate
     */
    public CacheState getState() {
        return m_cachestate;
    }

    /**
     * @param (cachestate != null) the cache state
     * @param the         cache data object to save
     */
    public void setData(CacheState cachestate, T cacheData) {
        m_cachestate = cachestate;
        m_data = cacheData;
        m_cachetimeStamp = System.currentTimeMillis();

    }

    /**
     * @return the data
     */
    public T geData() {
        return m_data;
    }

    /**
     * @return the last loaded time of the cache data
     */
    public long getLoadedTime() {
        return m_cachetimeStamp;
    }

    public static enum CacheState {
        Lazy_Loaded,
        Fully_Loaded,
        Invalid
    }
}
