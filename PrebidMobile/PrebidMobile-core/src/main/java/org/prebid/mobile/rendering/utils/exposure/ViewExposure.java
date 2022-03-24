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
