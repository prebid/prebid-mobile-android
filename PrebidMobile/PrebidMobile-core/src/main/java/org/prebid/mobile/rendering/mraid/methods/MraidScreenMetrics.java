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

package org.prebid.mobile.rendering.mraid.methods;

import android.content.Context;
import android.graphics.Rect;
import androidx.annotation.NonNull;
import org.prebid.mobile.rendering.utils.helpers.Dips;

/**
 * Screen metrics needed by the MRAID container.
 *
 * Each rectangle is stored using both it's original and scaled coordinates to avoid allocating
 * extra memory that would otherwise be needed to do these conversions.
 */
public class MraidScreenMetrics {
    @NonNull private final Context context;
    @NonNull private final Rect screenRect;
    @NonNull private final Rect screenRectDips;

    @NonNull private final Rect rootViewRect;
    @NonNull private final Rect rootViewRectDips;

    @NonNull private final Rect currentAdRect;
    @NonNull private final Rect currentAdRectDips;

    @NonNull private final Rect defaultAdRect;
    @NonNull private final Rect defaultAdRectDips;

    private Rect currentMaxSizeRect;
    private Rect defaultPosition;

    private final float density;

    public MraidScreenMetrics(
            Context context,
            float density
    ) {
        this.context = context.getApplicationContext();
        this.density = density;

        screenRect = new Rect();
        screenRectDips = new Rect();

        rootViewRect = new Rect();
        rootViewRectDips = new Rect();

        currentAdRect = new Rect();
        currentAdRectDips = new Rect();

        defaultAdRect = new Rect();
        defaultAdRectDips = new Rect();
    }

    private void convertToDips(Rect sourceRect, Rect outRect) {
        outRect.set(Dips.pixelsToIntDips(sourceRect.left, context),
                Dips.pixelsToIntDips(sourceRect.top, context),
                Dips.pixelsToIntDips(sourceRect.right, context),
                Dips.pixelsToIntDips(sourceRect.bottom, context)
        );
    }

    public float getDensity() {
        return density;
    }

    public void setScreenSize(int width, int height) {
        screenRect.set(0, 0, width, height);
        convertToDips(screenRect, screenRectDips);
    }

    @NonNull
    public Rect getScreenRect() {
        return screenRect;
    }

    @NonNull
    public Rect getScreenRectDips() {
        return screenRectDips;
    }

    public void setRootViewPosition(int x, int y, int width, int height) {
        rootViewRect.set(x, y, x + width, y + height);
        convertToDips(rootViewRect, rootViewRectDips);
    }

    @NonNull
    public Rect getRootViewRect() {
        return rootViewRect;
    }

    @NonNull
    public Rect getRootViewRectDips() {
        return rootViewRectDips;
    }

    public void setCurrentAdPosition(int x, int y, int width, int height) {
        currentAdRect.set(x, y, x + width, y + height);
        convertToDips(currentAdRect, currentAdRectDips);
    }

    @NonNull
    public Rect getCurrentAdRect() {
        return currentAdRect;
    }

    @NonNull
    public Rect getCurrentAdRectDips() {
        return currentAdRectDips;
    }

    public void setDefaultAdPosition(int x, int y, int width, int height) {
        defaultAdRect.set(x, y, x + width, y + height);
        convertToDips(defaultAdRect, defaultAdRectDips);
    }

    @NonNull
    public Rect getDefaultAdRect() {
        return defaultAdRect;
    }

    @NonNull
    public Rect getDefaultAdRectDips() {
        return defaultAdRectDips;
    }

    public Rect getCurrentMaxSizeRect() {
        return currentMaxSizeRect;
    }

    public void setCurrentMaxSizeRect(Rect currentMaxSizeRect) {
        this.currentMaxSizeRect = new Rect(0, 0, currentMaxSizeRect.width(), currentMaxSizeRect.height());
    }

    public void setDefaultPosition(Rect defaultPosition) {
        this.defaultPosition = defaultPosition;
    }

    public Rect getDefaultPosition() {
        return defaultPosition;
    }
}
