package com.openx.apollo.utils.exposure;

import android.graphics.Rect;

import java.util.List;

public class ViewExposure {

    private float mExposurePercentage;
    private Rect mVisibleRectangle;
    private List<Rect> mOcclusionRectangleList;

    public ViewExposure(float exposurePercentage, Rect visibleRectangle, List<Rect> occlusionRectangleList) {
        mExposurePercentage = exposurePercentage;
        mVisibleRectangle = visibleRectangle;
        mOcclusionRectangleList = occlusionRectangleList;
    }

    public ViewExposure() {
        mExposurePercentage = 0.0f;
        mVisibleRectangle = new Rect();
        mOcclusionRectangleList = null;
    }

    public float getExposurePercentage() {
        return mExposurePercentage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ViewExposure that = (ViewExposure) o;

        if (Float.compare(that.mExposurePercentage, mExposurePercentage) != 0) {
            return false;
        }
        if (mVisibleRectangle != null
            ? !mVisibleRectangle.equals(that.mVisibleRectangle)
            : that.mVisibleRectangle != null) {
            return false;
        }
        return mOcclusionRectangleList != null
               ? mOcclusionRectangleList.equals(that.mOcclusionRectangleList)
               : that.mOcclusionRectangleList == null;
    }

    @Override
    public int hashCode() {
        int result = (mExposurePercentage != +0.0f ? Float.floatToIntBits(mExposurePercentage) : 0);
        result = 31 * result + (mVisibleRectangle != null ? mVisibleRectangle.hashCode() : 0);
        result = 31 * result + (mOcclusionRectangleList != null
                                ? mOcclusionRectangleList.hashCode()
                                : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{")
               .append("\"exposedPercentage\":").append(mExposurePercentage * 100).append(",");
        builder.append("\"visibleRectangle\":{")
               .append("\"x\":").append(mVisibleRectangle.left).append(",")
               .append("\"y\":").append(mVisibleRectangle.top).append(",")
               .append("\"width\":").append(mVisibleRectangle.width()).append(",")
               .append("\"height\":").append(mVisibleRectangle.height()).append("}");
        if (mOcclusionRectangleList != null && !mOcclusionRectangleList.isEmpty()) {
            builder.append(", \"occlusionRectangles\":[");
            for (int i = 0; i < mOcclusionRectangleList.size(); i++) {
                Rect currentRect = mOcclusionRectangleList.get(i);
                builder.append("{")
                       .append("\"x\":").append(currentRect.left).append(",")
                       .append("\"y\":").append(currentRect.top).append(",")
                       .append("\"width\":").append(currentRect.width()).append(",")
                       .append("\"height\":").append(currentRect.height()).append("}");
                if (i < mOcclusionRectangleList.size() - 1) {
                    builder.append(",");
                }
            }
            builder.append("]");
        }
        builder.append("}");

        return builder.toString();
    }
}
