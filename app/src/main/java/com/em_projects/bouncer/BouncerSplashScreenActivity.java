package com.em_projects.bouncer;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import com.em_projects.bouncer.views.OnBoardPermissionsActivity;
import com.em_projects.infra.activity.BasicActivity;

public class BouncerSplashScreenActivity extends BasicActivity {

    private static final String TAG = "BouncerSplashScreenAct";
    // hold splash constant params
    private static final int STOPSPLASH = 0;
    private static final long SPLASHTIME = 2000;

    // set handler for the splash screen - timer activated, executes the next
    // action after the splash screen.
    private Handler splashHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STOPSPLASH: {
                    Intent intent = new Intent(BouncerSplashScreenActivity.this, OnBoardPermissionsActivity.class);
                    startNewActivity(intent, true);
                }
            }
            super.handleMessage(msg);
        }
    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        // set splash screen layout
        setContentView(R.layout.splash_screen_layout);

        //get application version
        try {
            BouncerProperties.APP_VERSION_NUMBER =
                    this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
            BouncerProperties.APP_VERSION = "v" + BouncerProperties.APP_VERSION_NUMBER;
        } catch (NameNotFoundException e) {
            Log.e(getClass().getSimpleName(), "Failed to get application version. ", e);
        }

        //set application version text
        TextView appVersion = findViewById(R.id.app_version);
        appVersion.setText(BouncerProperties.APP_VERSION);

        // set message to stop the splash screen.
        Message msg = new Message();
        msg.what = STOPSPLASH;
        splashHandler.sendMessageDelayed(msg, SPLASHTIME);
    }
}
