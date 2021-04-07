package com.openx.apollo.sdk;

import android.content.Context;
import android.content.res.Resources;

import com.openx.apollo.R;
import com.openx.apollo.utils.helpers.Utils;

/**
 * Manages the JS files in SDK
 * Provides JS scripts extracted from bundled resource
 */
public class JSLibraryManager {
    private static JSLibraryManager sInstance;

    private Context mContext;
    private String mMRAIDscript;
    private String mOMSDKscirpt;

    private JSLibraryManager(Context context) {
        mContext = context.getApplicationContext();
        initScriptStrings();
    }

    public static JSLibraryManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (JSLibraryManager.class) {
                if (sInstance == null) {
                    sInstance = new JSLibraryManager(context);
                }
            }
        }
        return sInstance;
    }

    public String getMRAIDScript() {
        return mMRAIDscript;
    }

    public String getOMSDKScript() {
        return mOMSDKscirpt;
    }

    private void initScriptStrings() {
        Resources resources = mContext.getResources();
        mMRAIDscript = Utils.loadStringFromFile(resources, R.raw.mraid);
        mOMSDKscirpt = Utils.loadStringFromFile(resources, R.raw.omsdk_v1);
    }
}
