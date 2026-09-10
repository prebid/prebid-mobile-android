package org.prebid.mobile.rendering.utils.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Insets;
import android.os.Build;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.os.ConfigurationCompat;
import androidx.core.text.TextUtilsCompat;
import androidx.core.view.ViewCompat;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.core.R;

import java.util.Locale;

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

        Context context = view.getContext();
        WindowInsets windowInsets = getWindowInsets(context);
        CustomInsets navigationInsets = getNavigationInsets(context, windowInsets);
        CustomInsets cutoutInsets = getCutoutInsets(context, windowInsets);
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

            boolean applyTop;
            boolean applyBottom;
            boolean applyRight;
            boolean applyLeft;
            if (gravity == FrameLayout.LayoutParams.UNSPECIFIED_GRAVITY) {
                // Unspecified gravity is stored as -1 (all bits set). Without this branch the
                // bitmask checks below would spuriously match every side and insets would be
                // applied to all four margins instead of none.
                applyTop = applyBottom = applyRight = applyLeft = false;
            } else {
                gravity = Gravity.getAbsoluteGravity(gravity, resolveLayoutDirection(view));
                applyTop = (gravity & Gravity.TOP) == Gravity.TOP;
                applyBottom = (gravity & Gravity.BOTTOM) == Gravity.BOTTOM;
                applyRight = (gravity & Gravity.RIGHT) == Gravity.RIGHT;
                applyLeft = (gravity & Gravity.LEFT) == Gravity.LEFT;
            }

            frameParams.topMargin = baseMargins.getTop() + (applyTop ? insets.getTop() : 0);
            frameParams.bottomMargin = baseMargins.getBottom() + (applyBottom ? insets.getBottom() : 0);
            frameParams.rightMargin = baseMargins.getRight() + (applyRight ? insets.getRight() : 0);
            frameParams.leftMargin = baseMargins.getLeft() + (applyLeft ? insets.getLeft() : 0);

            view.setLayoutParams(frameParams);
            return true;
        } else if (params instanceof RelativeLayout.LayoutParams) {
            RelativeLayout.LayoutParams relativeParams = (RelativeLayout.LayoutParams) params;
            boolean isRtl = resolveLayoutDirection(view) == View.LAYOUT_DIRECTION_RTL;

            // RelativeLayout.LayoutParams#getRule(int) is API 23. This SDK's min is API 16,
            // so rules must be read through getRules(), which is API 1 and backed by the
            // same underlying array. On a real API 16 device that array is sized for the rules
            // that existed at API 16 (no RTL START/END rules yet), so any index at or beyond
            // ALIGN_PARENT_START/ALIGN_PARENT_END (added at API 17) must be bounds-checked
            // instead of assumed present, or this throws ArrayIndexOutOfBoundsException.
            int[] rules = relativeParams.getRules();
            boolean ruleTop = isRuleSet(rules, RelativeLayout.ALIGN_PARENT_TOP);
            boolean ruleBottom = isRuleSet(rules, RelativeLayout.ALIGN_PARENT_BOTTOM);
            boolean ruleRight = isRuleSet(rules, RelativeLayout.ALIGN_PARENT_RIGHT);
            boolean ruleLeft = isRuleSet(rules, RelativeLayout.ALIGN_PARENT_LEFT);
            boolean ruleEnd = isRuleSet(rules, RelativeLayout.ALIGN_PARENT_END);
            boolean ruleStart = isRuleSet(rules, RelativeLayout.ALIGN_PARENT_START);

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
        return getCutoutInsets(context, getWindowInsets(context));
    }

    private static CustomInsets getCutoutInsets(Context context, @Nullable WindowInsets windowInsets) {
        if (context != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            DisplayCutout cutout = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                cutout = context.getDisplay().getCutout();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (context instanceof Activity) {
                    Activity activity = (Activity) context;
                    cutout = activity.getWindowManager().getDefaultDisplay().getCutout();
                }
            } else if (windowInsets != null) {
                cutout = windowInsets.getDisplayCutout();
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
        return getNavigationInsets(context, getWindowInsets(context));
    }

    private static CustomInsets getNavigationInsets(Context context, @Nullable WindowInsets windowInsets) {
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

    /**
     * Whether {@code rule} is set to {@link RelativeLayout#TRUE} in {@code rules}. Bounds-checked
     * because on a real API 16 device the array {@link RelativeLayout.LayoutParams#getRules()}
     * returns is sized for the rules that existed at API 16 and does not have slots for the RTL
     * START/END rules added at API 17 (e.g. {@link RelativeLayout#ALIGN_PARENT_START}); indexing
     * it with one of those rule constants without a bounds check throws
     * {@link ArrayIndexOutOfBoundsException}.
     */
    private static boolean isRuleSet(int[] rules, int rule) {
        return rule >= 0 && rule < rules.length && rules[rule] == RelativeLayout.TRUE;
    }

    /**
     * Resolves {@code view}'s layout direction from its context's locale rather than from
     * {@link ViewCompat#getLayoutDirection(View)}, which only reports the real resolved
     * direction once a view is attached to a window; on an unattached view (e.g. right after
     * inflate, before it's added to its parent) it otherwise always reports LTR regardless of
     * the actual locale.
     */
    private static int resolveLayoutDirection(View view) {
        Context context = view.getContext();
        if (context != null && context.getResources() != null) {
            Configuration configuration = context.getResources().getConfiguration();
            Locale locale = ConfigurationCompat.getLocales(configuration).get(0);
            if (locale != null) {
                return TextUtilsCompat.getLayoutDirectionFromLocale(locale);
            }
        }
        return ViewCompat.getLayoutDirection(view);
    }

}
