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
    @NonNull
    private final Context mContext;
    @NonNull
    private final Rect mScreenRect;
    @NonNull
    private final Rect mScreenRectDips;

    @NonNull
    private final Rect mRootViewRect;
    @NonNull
    private final Rect mRootViewRectDips;

    @NonNull
    private final Rect mCurrentAdRect;
    @NonNull
    private final Rect mCurrentAdRectDips;

    @NonNull
    private final Rect mDefaultAdRect;
    @NonNull
    private final Rect mDefaultAdRectDips;

    private Rect mCurrentMaxSizeRect;
    private Rect mDefaultPosition;

    private final float mDensity;

    public MraidScreenMetrics(Context context, float density) {
        mContext = context.getApplicationContext();
        mDensity = density;

        mScreenRect = new Rect();
        mScreenRectDips = new Rect();

        mRootViewRect = new Rect();
        mRootViewRectDips = new Rect();

        mCurrentAdRect = new Rect();
        mCurrentAdRectDips = new Rect();

        mDefaultAdRect = new Rect();
        mDefaultAdRectDips = new Rect();
    }

    private void convertToDips(Rect sourceRect, Rect outRect) {
        outRect.set(
                Dips.pixelsToIntDips(sourceRect.left, mContext),
                Dips.pixelsToIntDips(sourceRect.top, mContext),
                Dips.pixelsToIntDips(sourceRect.right, mContext),
                Dips.pixelsToIntDips(sourceRect.bottom, mContext));
    }

    public float getDensity() {
        return mDensity;
    }

    public void setScreenSize(int width, int height) {
        mScreenRect.set(0, 0, width, height);
        convertToDips(mScreenRect, mScreenRectDips);
    }

    @NonNull
    public Rect getScreenRect() {
        return mScreenRect;
    }

    @NonNull
    public Rect getScreenRectDips() {
        return mScreenRectDips;
    }

    public void setRootViewPosition(int x, int y, int width, int height) {
        mRootViewRect.set(x, y, x + width, y + height);
        convertToDips(mRootViewRect, mRootViewRectDips);
    }

    @NonNull
    public Rect getRootViewRect() {
        return mRootViewRect;
    }

    @NonNull
    public Rect getRootViewRectDips() {
        return mRootViewRectDips;
    }

    public void setCurrentAdPosition(int x, int y, int width, int height) {
        mCurrentAdRect.set(x, y, x + width, y + height);
        convertToDips(mCurrentAdRect, mCurrentAdRectDips);
    }

    @NonNull
    public Rect getCurrentAdRect() {
        return mCurrentAdRect;
    }

    @NonNull
    public Rect getCurrentAdRectDips() {
        return mCurrentAdRectDips;
    }

    public void setDefaultAdPosition(int x, int y, int width, int height) {
        mDefaultAdRect.set(x, y, x + width, y + height);
        convertToDips(mDefaultAdRect, mDefaultAdRectDips);
    }

    @NonNull
    public Rect getDefaultAdRect() {
        return mDefaultAdRect;
    }

    @NonNull
    public Rect getDefaultAdRectDips() {
        return mDefaultAdRectDips;
    }

    public Rect getCurrentMaxSizeRect() {
        return mCurrentMaxSizeRect;
    }

    public void setCurrentMaxSizeRect(Rect currentMaxSizeRect) {
        mCurrentMaxSizeRect = new Rect(0, 0, currentMaxSizeRect.width(), currentMaxSizeRect.height());
    }

    public void setDefaultPosition(Rect defaultPosition) {
        mDefaultPosition = defaultPosition;
    }

    public Rect getDefaultPosition() {
        return mDefaultPosition;
    }
}
