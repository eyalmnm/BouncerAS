package com.em_projects.infra.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.ViewFlipper;

import com.em_projects.bouncer.R;
import com.em_projects.infra.application.BasicApplication;
import com.em_projects.infra.views.builders.ViewBuilder;
import com.em_projects.infra.views.controllers.ViewController;

import java.util.Stack;

public abstract class BasicActivity extends Activity {
    private static final String TAG = "BasicActivity";

    public static ProgressDialog s_gauge = null;
    // handler for dismissing progress dialog
    public static Handler s_gaugeHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (s_gauge != null) {
                s_gauge.dismiss();
                s_gauge = null;
            }
        }
    };
    private final Stack<ViewBuilder<?>> m_stack = new Stack<ViewBuilder<?>>();
    protected ViewFlipper m_viewFlipper = null;
    @SuppressWarnings("rawtypes")
    private ViewBuilder m_viewBuilder = null;

    public static void startGauge(final Context context) {
        Log.d(TAG, "startGauge");

        // runnable for show progress dialog
        Runnable showGaugeRunnable = new Runnable() {
            @Override
            public void run() {
                // prepare the thread to loop
                Looper.prepare();

                //init dialog
                s_gauge = new ProgressDialog(context);

                // set message for dialog
                s_gauge.setMessage(context.getString(R.string.gauge_message));

                // show dialog
                s_gauge.show();

                // start looping
                Looper.loop();

                // finish looping
                Looper.myLooper().quit();
            }
        };

        // start new thread for progress dialog
        Thread gaugeThread = new Thread(showGaugeRunnable);
        gaugeThread.start();
    }

    public static void stopGauge() {
        Log.d(TAG, "stopGauge");

        // dismiss dialog
        s_gaugeHandler.sendEmptyMessage(0);
    }

    public final synchronized void showView(final ViewBuilder<?> builder) {
        Log.d(TAG, "showView");
        Runnable r = new Runnable() {
            @Override
            public void run() {
                LayoutInflater inflater = (LayoutInflater) BasicActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                View mainLayout = builder.getMainLayout(inflater);

                if (mainLayout == null)
                    throw new RuntimeException("BasicActivity.showScreen() view builder " + builder.getClass().getSimpleName() + " returned null!");

                ViewController controller = builder.getViewController();

                if (controller != null)
                    controller.attachController(mainLayout);

                if (m_viewBuilder != null) {
                    if (m_viewBuilder.isViewBackable())
                        m_stack.push(m_viewBuilder);
                    else
                        m_viewFlipper.removeView(m_viewFlipper.getCurrentView());
                }

                m_viewBuilder = builder;

                //add the new layout
                m_viewFlipper.addView(mainLayout);

                //show the new view
                m_viewFlipper.showNext();
            }
        };

        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            r.run();
        } else {
            runOnUiThread(r);
        }
    }

    /**
     * Show the previous view from the viewFlipper
     */
    public final synchronized void showPreviousView() {
        Log.d(TAG, "showPreviousView");
        //get the previous view builder if stacked
        if (m_stack.isEmpty())
            return;

        m_viewBuilder = m_stack.pop();

        //remove the current view
        m_viewFlipper.removeView(m_viewFlipper.getCurrentView());

        //show previous view
        m_viewFlipper.showPrevious();
    }

    public final synchronized <T> void refreshView(final T newModel) {
        Log.d(TAG, "refreshView");
        Runnable r = new Runnable() {
            @Override
            public void run() {
                View view = m_viewFlipper.getChildAt(0);

                if (view == null)
                    throw new RuntimeException("BasicActivity.m_viewFlipper.getChildAt(0)returned null!");

                if (m_viewBuilder == null)
                    throw new RuntimeException("BasicActivity.m_viewBuilder == null!");

                try {
                    m_viewBuilder.refreshView(view, newModel);
                } catch (ClassCastException cce) {
                    Log.e(this.getClass().getSimpleName(), "BasicActivity.refreshView() got diffrent view builder for refresh! " + cce.getMessage());
                }
            }
        };

        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            r.run();
        } else {
            runOnUiThread(r);
        }
    }

    public void startNewActivity(Intent intent, boolean isFinishCurrent) {
        Log.d(TAG, "startNewActivity");
        // finish the current activity, if required
        if (isFinishCurrent)
            this.finish();

        //start the activity with the given intent
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        BasicApplication.getApplication().registerActivity(this);

        m_viewFlipper = new ViewFlipper(this);

        setContentView(m_viewFlipper);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        BasicApplication.getApplication().notifyActivityInForeground(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        BasicApplication.getApplication().removeActivity(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown");
        // override 'back' key functionality
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (m_viewBuilder != null) {
                ViewController controller = m_viewBuilder.getViewController();

                if (controller != null && !controller.onBackKeyPressed())
                    showPreviousView();
            }
            return true;
        }

        //handle 'search' key press
        if (keyCode == KeyEvent.KEYCODE_SEARCH) {
            return true;
        }

        //let the parent handle the event
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "onPrepareOptionsMenu");
        if (m_viewBuilder != null) {
            m_viewBuilder.addToOptionsMenu(menu);
        }

        return super.onPrepareOptionsMenu(menu);
    }
}
