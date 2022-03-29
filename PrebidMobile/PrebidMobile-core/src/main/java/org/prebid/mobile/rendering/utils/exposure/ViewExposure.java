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

    private float exposurePercentage;
    private Rect visibleRectangle;
    private List<Rect> occlusionRectangleList;

    public ViewExposure(
            float exposurePercentage,
            Rect visibleRectangle,
            List<Rect> occlusionRectangleList
    ) {
        this.exposurePercentage = exposurePercentage;
        this.visibleRectangle = visibleRectangle;
        this.occlusionRectangleList = occlusionRectangleList;
    }

    public ViewExposure() {
        exposurePercentage = 0.0f;
        visibleRectangle = new Rect();
        occlusionRectangleList = null;
    }

    public float getExposurePercentage() {
        return exposurePercentage;
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

        if (Float.compare(that.exposurePercentage, exposurePercentage) != 0) {
            return false;
        }
        if (visibleRectangle != null ? !visibleRectangle.equals(that.visibleRectangle) : that.visibleRectangle != null) {
            return false;
        }
        return occlusionRectangleList != null ? occlusionRectangleList.equals(that.occlusionRectangleList) : that.occlusionRectangleList == null;
    }

    @Override
    public int hashCode() {
        int result = (exposurePercentage != +0.0f ? Float.floatToIntBits(exposurePercentage) : 0);
        result = 31 * result + (visibleRectangle != null ? visibleRectangle.hashCode() : 0);
        result = 31 * result + (occlusionRectangleList != null ? occlusionRectangleList.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{").append("\"exposedPercentage\":").append(exposurePercentage * 100).append(",");
        builder.append("\"visibleRectangle\":{")
               .append("\"x\":")
               .append(visibleRectangle.left)
               .append(",")
               .append("\"y\":")
               .append(visibleRectangle.top)
               .append(",")
               .append("\"width\":")
               .append(visibleRectangle.width())
               .append(",")
               .append("\"height\":")
               .append(visibleRectangle.height())
               .append("}");
        if (occlusionRectangleList != null && !occlusionRectangleList.isEmpty()) {
            builder.append(", \"occlusionRectangles\":[");
            for (int i = 0; i < occlusionRectangleList.size(); i++) {
                Rect currentRect = occlusionRectangleList.get(i);
                builder.append("{")
                       .append("\"x\":")
                       .append(currentRect.left)
                       .append(",")
                       .append("\"y\":")
                       .append(currentRect.top)
                       .append(",")
                       .append("\"width\":")
                       .append(currentRect.width())
                       .append(",")
                       .append("\"height\":")
                       .append(currentRect.height())
                       .append("}");
                if (i < occlusionRectangleList.size() - 1) {
                    builder.append(",");
                }
            }
            builder.append("]");
        }
        builder.append("}");

        return builder.toString();
    }
}
