package org.prebid.mobile;

import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class VisibilityDetector {
    static final long VISIBILITY_THROTTLE_MILLIS = 250;
    private boolean scheduled = false;
    private View mView; // not null
    private ArrayList<VisibilityListener> listeners;
    private Runnable visibilityCheck;
    private ScheduledExecutorService tasker;
    private static final String TAG = "VisibilityDetector";
    private final int MIN_PERCENTAGE_VIEWED = 50;


    static VisibilityDetector create(View view) {
        if (view == null) {
            Log.d(TAG, "Unable to check visibility");
            return null;
        }

        return new VisibilityDetector(view);
    }

    private VisibilityDetector(View view) {
        this.mView = view;
        this.listeners = new ArrayList<VisibilityListener>();
        scheduleVisibilityCheck();
    }

    void addVisibilityListener(VisibilityListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    boolean removeVisibilityListener(VisibilityListener listener) {
        return listeners.remove(listener);
    }

    void scheduleVisibilityCheck(){
        if(scheduled) return;
        scheduled = true;
        this.visibilityCheck = new Runnable() {
            @Override
            public void run() {
                if (listeners != null) {
                    // copy listeners to a new array to avoid concurrentmodificationexception
                    ArrayList<VisibilityListener> tempList = new ArrayList<VisibilityListener>();
                    for (VisibilityListener listener : listeners) {
                        tempList.add(listener);
                    }
                    if (isVisible()) {
                        for (VisibilityListener listener : tempList) {
                            listener.onVisibilityChanged(true);
                            removeVisibilityListener(listener);
                        }
                    } else {
                        for (VisibilityListener listener : tempList) {
                            listener.onVisibilityChanged(false);
                        }
                    }
                }
            }
        };
        tasker = Executors.newSingleThreadScheduledExecutor();
        tasker.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                mView.post(visibilityCheck);
            }
        }, 0, VISIBILITY_THROTTLE_MILLIS, TimeUnit.MILLISECONDS);
    }

    boolean isVisible() {
        if (mView == null || mView.getVisibility() != View.VISIBLE || mView.getParent() == null) {
            return false;
        }

        // holds the visible part of a view
        Rect clippedArea = new Rect();

        if (!mView.getGlobalVisibleRect(clippedArea)) {
            return false;
        }

        final int visibleViewArea = clippedArea.height() * clippedArea.width();
        final int totalArea = mView.getHeight() * mView.getWidth();

        if (totalArea <= 0) {
            return false;
        }
        return 100 * visibleViewArea >= MIN_PERCENTAGE_VIEWED * totalArea;
    }

    void destroy() {
        if (tasker != null) {
            tasker.shutdownNow();
        }
        mView.removeCallbacks(visibilityCheck);
        mView = null;
        listeners = null;
    }

    interface VisibilityListener {
        void onVisibilityChanged(boolean visible);
    }

}