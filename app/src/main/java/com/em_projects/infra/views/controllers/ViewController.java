package com.em_projects.infra.views.controllers;

import android.view.View;

public interface ViewController {
    void attachController(View view);

    boolean onBackKeyPressed();
}
