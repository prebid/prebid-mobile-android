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
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.rendering.listeners.VideoCreativeViewListener;
import org.prebid.mobile.rendering.video.ExoPlayerView;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.rendering.views.webview.PrebidWebViewBanner;
import org.prebid.mobile.rendering.views.webview.PrebidWebViewInterstitial;
import org.prebid.mobile.rendering.views.webview.mraid.Views;

import java.util.ArrayList;

public class ViewPool {

    @SuppressLint("StaticFieldLeak") private static ViewPool sInstance = null;
    private ArrayList<View> occupiedViews = new ArrayList<>();
    private ArrayList<View> unoccupiedViews = new ArrayList<>();

    private ViewPool() {

    }

    public static ViewPool getInstance() {
        if (sInstance == null) {
            sInstance = new ViewPool();
        }
        return sInstance;
    }

    protected int sizeOfOccupied() {
        return occupiedViews.size();
    }

    protected int sizeOfUnoccupied() {
        return unoccupiedViews.size();
    }

    //This will add views into occupied bucket
    public void addToOccupied(View view) {
        if (!occupiedViews.contains(view) && !unoccupiedViews.contains(view)) {
            occupiedViews.add(view);
        }
    }

    public void addToUnoccupied(View view) {
        if (!unoccupiedViews.contains(view) && !occupiedViews.contains(view)) {
            unoccupiedViews.add(view);
        }
    }

    //This will swap from occupied to unoccupied(after windowclose) and removes it from occupied bucket
    public void swapToUnoccupied(View view) {
        if (!unoccupiedViews.contains(view)) {
            unoccupiedViews.add(view);

            Views.removeFromParent(view);
        }
        occupiedViews.remove(view);
    }

    //This will swap from unoccupied to occupied(after showing/displaying) and removes it from unoccupied bucket
    private void swapToOccupied(View view) {
        if (!occupiedViews.contains(view)) {
            occupiedViews.add(view);
        }

        unoccupiedViews.remove(view);
    }

    //This only clears the bucketlist. It does not actually remove the lists. Means (size becomes 0 but list still exists)
    public void clear() {
        occupiedViews.clear();
        unoccupiedViews.clear();
        plugPlayView = null;
    }

    private View plugPlayView;

    //Q: why are we keeping it in occupied? Should we not put/get from unoccupied directly?
    //A: Because, when a videoCreativeView is created, we will have to, anyways, add the view to the occupied bucket as it is going to be given to adView.
    //So, do that step here itself.(distribution of work!)
    public View getUnoccupiedView(Context context, VideoCreativeViewListener videoCreativeViewListener, AdFormat adType, InterstitialManager interstitialManager)
            throws AdException {
        if (context == null) {
            throw new AdException(AdException.INTERNAL_ERROR, "Context is null");
        }
        if (unoccupiedViews != null && unoccupiedViews.size() > 0) {

            View view = unoccupiedViews.get(0);

            Views.removeFromParent(view);

            //get item from unoccupied & add it to occupied
            swapToOccupied(view);
            return occupiedViews.get(occupiedViews.size() - 1);
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
