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

package org.prebid.mobile.rendering.views.webview.mraid;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import org.prebid.mobile.LogUtil;

public class Views {

    public static final String TAG = Views.class.getSimpleName();

    public static void removeFromParent(@Nullable View view) {
        if (view == null || view.getParent() == null) {
            return;
        }

        if (view.getParent() instanceof ViewGroup) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
    }

    /**
     * Depth first search of the view hierarchy below the given view for the first view of the
     * given type. The view itself is not considered a match.
     *
     * @param view root of the hierarchy to search. A null view, or one that is not a container,
     *             yields no match.
     * @param type the class to look for
     * @return the first matching descendant, or null when there is none
     */
    @Nullable
    public static <T extends View> T findFirstDescendantOfType(
            @Nullable View view,
            Class<T> type
    ) {
        if (!(view instanceof ViewGroup)) {
            return null;
        }

        final ViewGroup viewGroup = (ViewGroup) view;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            final View child = viewGroup.getChildAt(i);
            if (type.isInstance(child)) {
                return type.cast(child);
            }

            final T match = findFirstDescendantOfType(child, type);
            if (match != null) {
                return match;
            }
        }

        return null;
    }

    /**
     * Finds the topmost view in the current Activity or current view hierarchy.
     *
     * @param context If an Activity Context, used to obtain the Activity's DecorView. This is
     *                ignored if it is a non-Activity Context.
     * @param view A View in the currently displayed view hierarchy. If a null or non-Activity
     *             Context is provided, this View's topmost parent is used to determine the
     *             rootView.
     * @return The topmost View in the currency Activity or current view hierarchy. Null if no
     * applicable View can be found.
     */
    @Nullable
    public static View getTopmostView(@Nullable final Context context, @Nullable final View view) {
        final View rootViewFromActivity = getRootViewFromActivity(context);
        final View rootViewFromView = getRootViewFromView(view);

        // Prefer to use the rootView derived from the Activity's DecorView since it provides a
        // consistent value when the View is not attached to the Window. Fall back to the passed-in
        // View's hierarchy if necessary.
        return rootViewFromActivity != null
               ? rootViewFromActivity
               : rootViewFromView;
    }

    @Nullable
    private static View getRootViewFromActivity(@Nullable final Context context) {
        if (!(context instanceof Activity)) {
            return null;
        }

        return ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content);
    }

    @Nullable
    private static View getRootViewFromView(@Nullable final View view) {
        if (view == null) {
            return null;
        }

        if (!ViewCompat.isAttachedToWindow(view)) {
            LogUtil.debug(TAG, "Attempting to call View.getRootView() on an unattached View.");
        }

        final View rootView = view.getRootView();

        if (rootView == null) {
            return null;
        }

        final View rootContentView = rootView.findViewById(android.R.id.content);
        return rootContentView != null
               ? rootContentView
               : rootView;
    }
}
