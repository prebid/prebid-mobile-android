package com.openx.apollo.utils.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.WindowManager;

import com.openx.apollo.utils.logger.OXLog;

public class OrientationBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = OrientationBroadcastReceiver.class.getSimpleName();

    private Context mApplicationContext;

    // -1 until this gets set at least once
    private int mLastRotation = -1;
    private boolean orientationChanged;

    @Override
    public void onReceive(Context context, Intent intent) {
        OXLog.debug(TAG, "onReceive");
        if (Intent.ACTION_CONFIGURATION_CHANGED.equals(intent.getAction())) {
            int orientation = getDisplayRotation();
            if (orientation != mLastRotation) {
                mLastRotation = orientation;
                setOrientationChanged(true);
                handleOrientationChange(mLastRotation);
            } else {
                setOrientationChanged(false);
            }
        }
    }

    public boolean isOrientationChanged() {
        OXLog.debug(TAG, "isOrientationChanged: " + orientationChanged);
        return orientationChanged;
    }

    public void setOrientationChanged(boolean orientationChanged) {
        OXLog.debug(TAG, "setOrientationChanged: " + orientationChanged);
        this.orientationChanged = orientationChanged;
    }

    public void handleOrientationChange(int currentRotation){
        OXLog.debug(TAG, "handleOrientationChange currentRotation = " + currentRotation);
    }

    private int getDisplayRotation() {
        WindowManager wm = (WindowManager) mApplicationContext.getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getRotation();
    }

    public void register(final Context context) {
        if (context != null) {
            OXLog.debug(TAG, "register");
            mApplicationContext = context.getApplicationContext();
            if (mApplicationContext != null) {
                mApplicationContext.registerReceiver(this,
                                                         new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED));
            }
        }
    }

    public void unregister() {
        if (mApplicationContext != null) {
            OXLog.debug(TAG, "unregister");
            mApplicationContext.unregisterReceiver(this);
            mApplicationContext = null;
        }
    }
}
