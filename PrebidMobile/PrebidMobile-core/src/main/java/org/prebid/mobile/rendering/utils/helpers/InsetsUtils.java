package org.prebid.mobile.rendering.utils.helpers;

import android.app.Activity;
import android.content.Context;
import android.graphics.Insets;
import android.os.Build;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.view.ViewCompat;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.core.R;

public class InsetsUtils {

    private static final String TAG = InsetsUtils.class.getSimpleName();

    /**
     * Adds to the view insets from navigation bar and cutout.
     * Insets must be calculated as we use translucent status and navigation bar in
     * interstitial ad. Must be applied to every view in interstitial ad.
     * <p>
     * It supports view where parents are RelativeLayout or FrameLayout.
     * <p>
     * The margins {@code view} had before any inset was ever applied to it are captured
     * once (as a view tag) and reused as the baseline on every subsequent call. This makes
     * repeated calls (e.g. on every rotation) idempotent: insets never accumulate, and if the
     * resolved gravity/rule changes between calls (RTL flip, position change, etc.) the side
     * that no longer applies is reset back to its original margin instead of staying stuck.
     */
    public static void addCutoutAndNavigationInsets(@Nullable View view) {
        if (view == null) return;

        CustomInsets navigationInsets = getNavigationInsets(view.getContext());
        CustomInsets cutoutInsets = getCutoutInsets(view.getContext());
        CustomInsets insets = new CustomInsets(
            navigationInsets.getTop() + cutoutInsets.getTop(),
            navigationInsets.getRight() + cutoutInsets.getRight(),
            navigationInsets.getBottom() + cutoutInsets.getBottom(),
            navigationInsets.getLeft() + cutoutInsets.getLeft()
        );

        applyInsetsToMargins(view, getOrCaptureBaseMargins(view), insets);
    }

    /**
     * Returns the margins {@code view} had before any inset was ever applied to it. Captured
     * from its current {@link ViewGroup.LayoutParams} on the first call and remembered as a
     * view tag for every call after that.
     */
    private static CustomInsets getOrCaptureBaseMargins(View view) {
        Object tag = view.getTag(R.id.prebid_base_margins);
        if (tag instanceof CustomInsets) {
            return (CustomInsets) tag;
        }

        CustomInsets baseMargins = new CustomInsets(0, 0, 0, 0);
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) params;
            baseMargins = new CustomInsets(
                marginParams.topMargin,
                marginParams.rightMargin,
                marginParams.bottomMargin,
                marginParams.leftMargin
            );
        }
        view.setTag(R.id.prebid_base_margins, baseMargins);
        return baseMargins;
    }

    @VisibleForTesting
    static boolean applyInsetsToMargins(
        View view,
        CustomInsets baseMargins,
        CustomInsets insets
    ) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params instanceof FrameLayout.LayoutParams) {
            FrameLayout.LayoutParams frameParams = (FrameLayout.LayoutParams) params;
            int gravity = frameParams.gravity;
            if (gravity != FrameLayout.LayoutParams.UNSPECIFIED_GRAVITY) {
                gravity = Gravity.getAbsoluteGravity(gravity, ViewCompat.getLayoutDirection(view));
            }

            boolean applyTop = (gravity & Gravity.TOP) == Gravity.TOP;
            boolean applyBottom = (gravity & Gravity.BOTTOM) == Gravity.BOTTOM;
            boolean applyRight = (gravity & Gravity.RIGHT) == Gravity.RIGHT;
            boolean applyLeft = (gravity & Gravity.LEFT) == Gravity.LEFT;

            frameParams.topMargin = baseMargins.getTop() + (applyTop ? insets.getTop() : 0);
            frameParams.bottomMargin = baseMargins.getBottom() + (applyBottom ? insets.getBottom() : 0);
            frameParams.rightMargin = baseMargins.getRight() + (applyRight ? insets.getRight() : 0);
            frameParams.leftMargin = baseMargins.getLeft() + (applyLeft ? insets.getLeft() : 0);

            view.setLayoutParams(frameParams);
            return true;
        } else if (params instanceof RelativeLayout.LayoutParams) {
            RelativeLayout.LayoutParams relativeParams = (RelativeLayout.LayoutParams) params;
            boolean isRtl = ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL;

            boolean ruleTop = relativeParams.getRule(RelativeLayout.ALIGN_PARENT_TOP) == RelativeLayout.TRUE;
            boolean ruleBottom = relativeParams.getRule(RelativeLayout.ALIGN_PARENT_BOTTOM) == RelativeLayout.TRUE;
            boolean ruleRight = relativeParams.getRule(RelativeLayout.ALIGN_PARENT_RIGHT) == RelativeLayout.TRUE;
            boolean ruleLeft = relativeParams.getRule(RelativeLayout.ALIGN_PARENT_LEFT) == RelativeLayout.TRUE;
            boolean ruleEnd = relativeParams.getRule(RelativeLayout.ALIGN_PARENT_END) == RelativeLayout.TRUE;
            boolean ruleStart = relativeParams.getRule(RelativeLayout.ALIGN_PARENT_START) == RelativeLayout.TRUE;

            // END/START are direction-relative: resolve them against the view's resolved
            // layout direction instead of always mapping END->right and START->left, which
            // is wrong under RTL.
            boolean applyRight = ruleRight || (ruleEnd && !isRtl) || (ruleStart && isRtl);
            boolean applyLeft = ruleLeft || (ruleStart && !isRtl) || (ruleEnd && isRtl);

            relativeParams.topMargin = baseMargins.getTop() + (ruleTop ? insets.getTop() : 0);
            relativeParams.bottomMargin = baseMargins.getBottom() + (ruleBottom ? insets.getBottom() : 0);
            relativeParams.rightMargin = baseMargins.getRight() + (applyRight ? insets.getRight() : 0);
            relativeParams.leftMargin = baseMargins.getLeft() + (applyLeft ? insets.getLeft() : 0);

            view.setLayoutParams(relativeParams);
            return true;
        } else {
            LogUtil.error(TAG, "Can't set insets, unsupported LayoutParams type.");
            return false;
        }
    }

    public static CustomInsets getCutoutInsets(Context context) {
        if (context != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            DisplayCutout cutout = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                cutout = context.getDisplay().getCutout();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (context instanceof Activity) {
                    Activity activity = (Activity) context;
                    cutout = activity.getWindowManager().getDefaultDisplay().getCutout();
                }
            } else {
                WindowInsets windowInsets = getWindowInsets(context);
                if (windowInsets != null) {
                    cutout = windowInsets.getDisplayCutout();
                }
            }
            if (cutout != null) {
                return new CustomInsets(
                    cutout.getSafeInsetTop(),
                    cutout.getSafeInsetRight(),
                    cutout.getSafeInsetBottom(),
                    cutout.getSafeInsetLeft()
                );
            }
        }
        return new CustomInsets(0, 0, 0, 0);
    }

    public static CustomInsets getNavigationInsets(Context context) {
        WindowInsets windowInsets = getWindowInsets(context);
        if (windowInsets != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Insets insets = windowInsets.getInsets(WindowInsets.Type.navigationBars());
                return new CustomInsets(insets.top, insets.right, insets.bottom, insets.left);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // noinspection deprecation
                return new CustomInsets(
                    windowInsets.getStableInsetTop(),
                    windowInsets.getStableInsetRight(),
                    windowInsets.getStableInsetBottom(),
                    windowInsets.getStableInsetLeft()
                );
            }
        }
        return new CustomInsets(0, 0, 0, 0);
    }

    @Nullable
    private static WindowInsets getWindowInsets(@Nullable Context context) {
        if (context != null) {
            if (context instanceof Activity) {
                Activity activity = (Activity) context;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return activity.getWindow().getDecorView().getRootWindowInsets();
                }
            } else {
                LogUtil.debug(TAG, "Can't get window insets, Context is not Activity type.");
            }
        }
        return null;
    }

}
