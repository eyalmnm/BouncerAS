package com.cellebrite.ota.socialphonebook.repositories;

import android.os.Handler;
import android.os.Looper;

import java.util.Vector;

/**
 * Provides utilities for repositories.
 */
public class RepositoriesUtils {
    //collection of observers for notifying when a handler object has been created
    private static Vector<HandlerCreatedObserver> s_observerColl = new Vector<HandlerCreatedObserver>();

    //hold a handler
    private static Handler s_handler = null;

    //holds a handler builder
    private static AndroidBackgroundObserverBuilder s_androidBackgroundBuilder = null;

    /**
     * @param observer (HandlerCreatedObserver != null) observer that will get notify when a handler has been built
     */
    public static synchronized void getHandlerForContentObservers(HandlerCreatedObserver observer) {
        if (s_handler != null) {
            observer.onCreateHandlerCompleted(s_handler);
        } else {
            s_observerColl.add(observer);

            if (s_androidBackgroundBuilder == null) {
                s_androidBackgroundBuilder = new AndroidBackgroundObserverBuilder();
                s_androidBackgroundBuilder.setPriority(Thread.MIN_PRIORITY);
                s_androidBackgroundBuilder.start();
            }
        }
    }

    /**
     * Notify all observers that the handler has been created.
     */
    static synchronized void notifyAllObservers() {
        while (!s_observerColl.isEmpty()) {
            HandlerCreatedObserver observer = s_observerColl.firstElement();
            s_observerColl.remove(0);

            observer.onCreateHandlerCompleted(s_handler);
        }
    }

    /**
     * Interface for getting a handler for repositories usage.
     */
    public interface HandlerCreatedObserver {
        void onCreateHandlerCompleted(Handler handler);
    }

    /**
     * Builds an Android content observer in the background and associates it to this thread.
     */
    static class AndroidBackgroundObserverBuilder extends Thread {
        @Override
        public void run() {
            //?
            Looper.prepare();

            //create the handler associated to this thread
            s_handler = new Handler();

            //notify all awaiting observers that the handler has been created
            notifyAllObservers();

            //?
            Looper.loop();
        }
    }
}
