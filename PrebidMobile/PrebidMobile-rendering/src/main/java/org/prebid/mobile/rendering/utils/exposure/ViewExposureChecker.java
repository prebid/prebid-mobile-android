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

package org.prebid.mobile.rendering.utils.exposure;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.VisibleForTesting;

import org.prebid.mobile.rendering.R;
import org.prebid.mobile.rendering.utils.logger.OXLog;
import org.prebid.mobile.rendering.views.indicator.AdIndicatorView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ViewExposureChecker {
    private static final String TAG = ViewExposureChecker.class.getSimpleName();

    private WeakReference<View> mTestedViewWeakReference;
    private Rect mClippedRect;
    private List<Rect> mObstructionList;

    public ViewExposureChecker() {
        mObstructionList = new ArrayList<>();
        mClippedRect = new Rect();
    }

    public ViewExposure exposure(View view) {
        if (view == null) {
            OXLog.debug(TAG, "exposure: Returning zeroExposure. Test View is null.");
            return null;
        }

        mTestedViewWeakReference = new WeakReference<>(view);
        ViewExposure zeroExposure = new ViewExposure();
        view.getDrawingRect(mClippedRect);
        mObstructionList.clear();

        if (!view.isShown() || !view.hasWindowFocus() || isViewTransparent(view)) { // Also checks if view has parent (is attached)
            return zeroExposure;
        }

        boolean visitParent = visitParent(((ViewGroup) view.getParent()), view);
        boolean collapseBoundingBox = collapseBoundingBox();

        OXLog.debug(TAG, "exposure: visitParent " + visitParent + " collapseBox " + collapseBoundingBox);
        boolean potentiallyExposed = visitParent && collapseBoundingBox;
        if (!potentiallyExposed) {
            return zeroExposure;
        }

        final List<Rect> obstructionsList = buildObstructionsRectList();
        final float fullArea = view.getWidth() * view.getHeight();
        final float clipArea = mClippedRect.width() * mClippedRect.height();
        float obstructedArea = 0;

        for (Rect obstruction : obstructionsList) {
            obstructedArea += obstruction.width() * obstruction.height();
        }

        float exposurePercentage = (clipArea - obstructedArea) / fullArea;
        return new ViewExposure(exposurePercentage,
                                mClippedRect,
                                obstructionsList);
    }

    private List<Rect> buildObstructionsRectList() {
        if (mObstructionList.isEmpty()) {
            return new ArrayList<>();
        }

        List<Rect> currentObstructionList = new ArrayList<>(mObstructionList);
        List<Rect> remainingObstructionList = new ArrayList<>();
        List<Rect> pickedObstructionList = new ArrayList<>();

        Comparator<Rect> areaComparator = (rect1, rect2) -> {
            float area1 = rect1.width() * rect1.height();
            float area2 = rect2.width() * rect2.height();

            return -Float.compare(area1, area2);
        };

        while (currentObstructionList.size() > 0) {
            Collections.sort(currentObstructionList, areaComparator);

            Rect pickedObstruction = currentObstructionList.get(0);
            pickedObstructionList.add(pickedObstruction);

            removeRect(pickedObstruction, currentObstructionList, remainingObstructionList, 1);

            List<Rect> temp = new ArrayList<>(currentObstructionList);
            currentObstructionList = remainingObstructionList;
            remainingObstructionList = temp;
            remainingObstructionList.clear();
        }

        return pickedObstructionList;
    }

    private boolean visitParent(ViewGroup parentView, View childView) {
        if (parentView.getVisibility() != View.VISIBLE || isViewTransparent(parentView)) {
            return false;
        }

        boolean clip = isClippedToBounds(parentView);

        if (clip) {
            Rect bounds = new Rect();
            parentView.getDrawingRect(bounds);
            Rect convertRect = convertRect(bounds, parentView, mTestedViewWeakReference.get());
            boolean intersect = mClippedRect.intersect(convertRect);
            if (!intersect) {
                return false;
            }
        }

        if (parentView.getParent() instanceof ViewGroup) {
            boolean notOverclipped = visitParent(((ViewGroup) parentView.getParent()), parentView);
            if (!notOverclipped) {
                return false;
            }
        }

        for (int i = parentView.indexOfChild(childView) + 1, n = parentView.getChildCount(); i < n; i++) {
            View child = parentView.getChildAt(i);
            if (isFriendlyObstruction(child)) {
                continue;
            }
            collectObstructionsFrom(child);
        }

        return true;
    }

    // don't test child if it is viewGroup and transparent
    private boolean isFriendlyObstruction(View child) {
        return child instanceof AdIndicatorView
               || (child instanceof ImageView && child.getId() == R.id.iv_close_interstitial)
               || child.getId() == R.id.rl_count_down;
    }

    private void collectObstructionsFrom(View child) {
        if (!child.isShown() || isViewTransparent(child)) { // not obstructing
            return;
        }
        if (shouldCollectObstruction(child)) {
            testForObstructing(child);
        }

        if (!(child instanceof ViewGroup)) {
            return;
        }

        ViewGroup viewGroup = ((ViewGroup) child);

        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            collectObstructionsFrom(viewGroup.getChildAt(i));
        }
    }

    private boolean collapseBoundingBox() {
        final Rect oldRect = new Rect(mClippedRect);
        if (oldRect.isEmpty()) {
            return false;
        }

        List<Rect> currentRectList = new ArrayList<>();
        List<Rect> nextRectList = new ArrayList<>();

        currentRectList.add(mClippedRect);
        for (Rect obstruction : mObstructionList) {
            removeRect(obstruction, currentRectList, nextRectList, 0);

            List<Rect> temp = currentRectList;
            currentRectList = nextRectList;
            nextRectList = temp;
            nextRectList.clear();

            if (currentRectList.isEmpty()) {
                mClippedRect = new Rect();
                return false;
            }
        }

        Rect result = new Rect();
        for (int i = 0; i < currentRectList.size(); i++) {
            Rect nextFragment = currentRectList.get(i);

            if (i == 0) {
                result = nextFragment;
            }
            else {
                result.union(nextFragment);
            }
        }

        if (oldRect.equals(result)) {
            return true;
        }

        mClippedRect = result;

        int removedCount = 0;
        final int fullCount = mObstructionList.size();

        for (int i = 0; i < fullCount; i++) {
            Rect nextObstruction = mObstructionList.get(i);
            Rect resultIntersectedRect = new Rect(result);

            if (resultIntersectedRect.intersect(nextObstruction)) {
                if (!result.contains(nextObstruction)) {
                    mObstructionList.set(i - removedCount, resultIntersectedRect);
                }
                else if (removedCount > 0) {
                    mObstructionList.set(i - removedCount, nextObstruction);
                }
            }
            else {
                removedCount++;
            }
        }
        if (removedCount > 0) {
            int fromIndex = fullCount - removedCount;
            mObstructionList.subList(fromIndex, fromIndex + removedCount).clear();
        }

        return true;
    }

    private void removeRect(Rect aroundRect, List<Rect> srcList, List<Rect> destList, int firstIndex) {
        for (int i = firstIndex, n = srcList.size(); i < n; i++) {
            fragmentize(srcList.get(i), aroundRect, destList);
        }
    }

    private void fragmentize(Rect valueRect, Rect aroundRect, List<Rect> destList) {
        if (!Rect.intersects(valueRect, aroundRect)) {
            destList.add(valueRect);
            return;
        }

        if (aroundRect.contains(valueRect)) {
            return;
        }

        Rect trimmedRect = new Rect(aroundRect);
        boolean isRectTrimmed = trimmedRect.intersect(valueRect);

        if (!isRectTrimmed) {
            OXLog.debug(TAG, "fragmentize: Error. Rect is not trimmed");
            return;
        }

        Rect[] subRectArray = {
            // left
            new Rect(valueRect.left,
                     valueRect.top,
                     valueRect.left + (trimmedRect.left - valueRect.left),
                     valueRect.top + valueRect.height()),

            // mid / top
            new Rect(trimmedRect.left,
                     valueRect.top,
                     trimmedRect.right,
                     valueRect.top + (trimmedRect.top - valueRect.top)),

            // mid / bottom
            new Rect(trimmedRect.left,
                     trimmedRect.bottom,
                     trimmedRect.right,
                     valueRect.bottom),

            // right
            new Rect(trimmedRect.right,
                     valueRect.top,
                     valueRect.right,
                     valueRect.top + valueRect.height())
        };

        for (Rect rect : subRectArray) {
            if (!rect.isEmpty()) {
                destList.add(rect);
            }
        }
    }

    private boolean isClippedToBounds(ViewGroup viewGroup) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return viewGroup.getClipChildren();
        }
        return false;
    }

    private Rect convertRect(Rect fromRect, View fromView, View toView) {
        if (fromRect == null || fromView == null || toView == null) {
            OXLog.debug(TAG, "convertRect: Failed. One of the provided param is null. Returning empty rect.");
            return new Rect();
        }

        int[] fromCoord = new int[2];
        int[] toCoord = new int[2];
        fromView.getLocationOnScreen(fromCoord);
        toView.getLocationOnScreen(toCoord);

        int xShift = fromCoord[0] - toCoord[0] - fromView.getScrollX();
        int yShift = fromCoord[1] - toCoord[1] - fromView.getScrollY();

        return new Rect(fromRect.left + xShift,
                        fromRect.top + yShift,
                        fromRect.right + xShift,
                        fromRect.bottom + yShift);
    }

    private void testForObstructing(View view) {
        Rect viewBounds = new Rect();
        view.getDrawingRect(viewBounds);

        Rect testRect = convertRect(viewBounds, view, mTestedViewWeakReference.get());

        Rect obstructionRect = new Rect(mClippedRect);
        boolean isObstruction = obstructionRect.intersect(testRect);
        if (isObstruction) {
            mObstructionList.add(obstructionRect);
        }
    }

    private boolean isViewTransparent(View view) {
        return view.getAlpha() == 0;
    }

    /**
     * @return true if child is not instance of ViewGroup or if child is ViewGroup and foreground and background is transparent (or null).
     */
    @VisibleForTesting
    boolean shouldCollectObstruction(View child) {
        if (!(child instanceof ViewGroup)) {
            return true;
        }

        final Drawable foreground = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                                    ? child.getForeground()
                                    : null;
        final Drawable background = child.getBackground();

        final boolean isForegroundTransparent = foreground == null
                                                || foreground.getAlpha() == 0;

        final boolean isBackgroundTransparent = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                                                && (background == null || background.getAlpha() == 0);

        return !isBackgroundTransparent || !isForegroundTransparent;
    }
}
