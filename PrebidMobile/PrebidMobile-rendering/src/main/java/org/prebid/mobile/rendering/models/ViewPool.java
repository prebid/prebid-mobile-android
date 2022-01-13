/*
 *    Copyright 2018-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile.rendering.models;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;

import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.listeners.VideoCreativeViewListener;
import org.prebid.mobile.rendering.video.ExoPlayerView;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.rendering.views.webview.PrebidWebViewBanner;
import org.prebid.mobile.rendering.views.webview.PrebidWebViewInterstitial;
import org.prebid.mobile.rendering.views.webview.mraid.Views;

import java.util.ArrayList;

public class ViewPool {
    @SuppressLint("StaticFieldLeak")
    private static ViewPool sInstance = null;
    private ArrayList<View> mOccupiedViews = new ArrayList<>();
    private ArrayList<View> mUnoccupiedViews = new ArrayList<>();

    private ViewPool() {

    }

    public static ViewPool getInstance() {
        if (sInstance == null) {
            sInstance = new ViewPool();
        }
        return sInstance;
    }

    protected int sizeOfOccupied() {
        return mOccupiedViews.size();
    }

    protected int sizeOfUnoccupied() {
        return mUnoccupiedViews.size();
    }

    //This will add views into occupied bucket
    public void addToOccupied(View view) {
        if (!mOccupiedViews.contains(view) && !mUnoccupiedViews.contains(view)) {
            mOccupiedViews.add(view);
        }
    }

    public void addToUnoccupied(View view) {
        if (!mUnoccupiedViews.contains(view) && !mOccupiedViews.contains(view)) {
            mUnoccupiedViews.add(view);
        }
    }

    //This will swap from occupied to unoccupied(after windowclose) and removes it from occupied bucket
    public void swapToUnoccupied(View view) {
        if (!mUnoccupiedViews.contains(view)) {
            mUnoccupiedViews.add(view);

            Views.removeFromParent(view);
        }
        mOccupiedViews.remove(view);
    }

    //This will swap from unoccupied to occupied(after showing/displaying) and removes it from unoccupied bucket
    private void swapToOccupied(View view) {
        if (!mOccupiedViews.contains(view)) {
            mOccupiedViews.add(view);
        }

        mUnoccupiedViews.remove(view);
    }

    //This only clears the bucketlist. It does not actually remove the lists. Means (size becomes 0 but list still exists)
    public void clear() {
        mOccupiedViews.clear();
        mUnoccupiedViews.clear();
        plugPlayView = null;
    }

    private View plugPlayView;

    //Q: why are we keeping it in occupied? Should we not put/get from unoccupied directly?
    //A: Because, when a videoCreativeView is created, we will have to, anyways, add the view to the occupied bucket as it is going to be given to adView.
    //So, do that step here itself.(distribution of work!)
    public View getUnoccupiedView(Context context, VideoCreativeViewListener videoCreativeViewListener, AdConfiguration.AdUnitIdentifierType adType, InterstitialManager interstitialManager)
    throws AdException {
        if (context == null) {
            throw new AdException(AdException.INTERNAL_ERROR, "Context is null");
        }
        if (mUnoccupiedViews != null && mUnoccupiedViews.size() > 0) {

            View view = mUnoccupiedViews.get(0);

            Views.removeFromParent(view);

            //get item from unoccupied & add it to occupied
            swapToOccupied(view);
            return mOccupiedViews.get(mOccupiedViews.size() - 1);
        }
        //create a new one

        //add it to occupied

        switch (adType) {
            case BANNER:
                plugPlayView = new PrebidWebViewBanner(context, interstitialManager);
                break;
            case INTERSTITIAL:
                plugPlayView = new PrebidWebViewInterstitial(context, interstitialManager);
                //add it to occupied
                break;
            case VAST:
                plugPlayView = new ExoPlayerView(context, videoCreativeViewListener);
                break;
        }
        addToOccupied(plugPlayView);
        return plugPlayView;
    }
}
