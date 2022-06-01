package org.prebid.mobile.rendering.utils.helpers;

import android.app.Activity;
import android.content.Context;
import android.graphics.Insets;
import android.os.Build;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import androidx.annotation.Nullable;
import org.prebid.mobile.LogUtil;

public class InsetsUtils {

    private static final String TAG = InsetsUtils.class.getSimpleName();

    /**
     * Adds to the view insets from navigation bar and cutout.
     * Insets must be calculated as we use translucent status and navigation bar in
     * interstitial ad. Must be applied to every view in interstitial ad.
     * <p>
     * It supports view where parents are RelativeLayout or FrameLayout.
     */
    public static void addCutoutAndNavigationInsets(@Nullable View view) {
        if (view == null) return;
        ViewGroup.LayoutParams params = view.getLayoutParams();
        CustomInsets navigationInsets = getNavigationInsets(view.getContext());
        CustomInsets cutoutInsets = getCutoutInsets(view.getContext());
        CustomInsets insets = new CustomInsets(
            navigationInsets.getTop() + cutoutInsets.getTop(),
            navigationInsets.getRight() + cutoutInsets.getRight(),
            navigationInsets.getBottom() + cutoutInsets.getBottom(),
            navigationInsets.getLeft() + cutoutInsets.getLeft()
        );

        if (params instanceof FrameLayout.LayoutParams) {
            FrameLayout.LayoutParams frameParams = (FrameLayout.LayoutParams) params;
            int gravity = frameParams.gravity;
            if ((gravity & Gravity.TOP) == Gravity.TOP) {
                frameParams.topMargin += insets.getTop();
            }
            if ((gravity & Gravity.BOTTOM) == Gravity.BOTTOM) {
                frameParams.bottomMargin += insets.getBottom();
            }
            if ((gravity & Gravity.RIGHT) == Gravity.RIGHT) {
                frameParams.rightMargin += insets.getRight();
            }
            if ((gravity & Gravity.LEFT) == Gravity.LEFT) {
                frameParams.leftMargin += insets.getLeft();
            }
            view.setLayoutParams(frameParams);
        } else if (params instanceof RelativeLayout.LayoutParams) {
            RelativeLayout.LayoutParams relativeParams = (RelativeLayout.LayoutParams) params;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (relativeParams.getRule(RelativeLayout.ALIGN_PARENT_TOP) == RelativeLayout.TRUE) {
                    relativeParams.topMargin += insets.getTop();
                }
                if (relativeParams.getRule(RelativeLayout.ALIGN_PARENT_BOTTOM) == RelativeLayout.TRUE) {
                    relativeParams.bottomMargin += insets.getBottom();
                }
                if (relativeParams.getRule(RelativeLayout.ALIGN_PARENT_RIGHT) == RelativeLayout.TRUE
                    || relativeParams.getRule(RelativeLayout.ALIGN_PARENT_END) == RelativeLayout.TRUE
                ) {
                    relativeParams.rightMargin += insets.getRight();
                }
                if (relativeParams.getRule(RelativeLayout.ALIGN_PARENT_LEFT) == RelativeLayout.TRUE
                    || relativeParams.getRule(RelativeLayout.ALIGN_PARENT_START) == RelativeLayout.TRUE
                ) {
                    relativeParams.leftMargin += insets.getLeft();
                }
            }
            view.setLayoutParams(relativeParams);
        } else {
            LogUtil.error(TAG, "Can't set insets, unsupported LayoutParams type.");
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

    public static void resetMargins(@Nullable View view) {
        int defaultMargin = 16;
        if (view != null) {
            ViewGroup.LayoutParams params = view.getLayoutParams();
            if (params instanceof FrameLayout.LayoutParams) {
                FrameLayout.LayoutParams frameParams = (FrameLayout.LayoutParams) params;
                frameParams.setMargins(defaultMargin, defaultMargin, defaultMargin, defaultMargin);
                view.setLayoutParams(frameParams);
            } else if (params instanceof RelativeLayout.LayoutParams) {
                RelativeLayout.LayoutParams relativeParams = (RelativeLayout.LayoutParams) params;
                relativeParams.setMargins(defaultMargin, defaultMargin, defaultMargin, defaultMargin);
                view.setLayoutParams(relativeParams);
            } else {
                LogUtil.debug(TAG, "Can't reset margins.");
            }
        }
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
