package com.em_projects.bouncer.views.model;


import android.util.Log;

public class TextScreenViewModel {
    private static final String TAG = "TextScreenViewModel";
    private final String m_title;
    private final String m_text;

    public TextScreenViewModel(String title, String text) {
        Log.d(TAG, "TextScreenViewModel");
        m_title = title;
        m_text = text;
    }

    public String getTitle() {
        Log.d(TAG, "getTitle");
        return m_title;
    }

    public String getText() {
        Log.d(TAG, "getText");
        return m_text;
    }
}
