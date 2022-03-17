/*
 *    Copyright 2018-2019 Prebid.org, Inc.
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

package org.prebid.mobile;

/**
 * AdSize class defines the size of the ad slot to be made available for auction.
 */
public class AdSize {
    private int width;
    private int height;

    /**
     * Creates an ad size object with width and height as specified
     *
     * @param width  width of the ad container
     * @param height height of the ad container
     */
    public AdSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Returns the width of the ad container
     *
     * @return width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of the ad container
     *
     * @return height
     */
    public int getHeight() {
        return height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AdSize adSize = (AdSize) o;

        if (width != adSize.width) return false;
        return height == adSize.height;
    }

    @Override
    public int hashCode() {
        String size = width + "x" + height;
        return size.hashCode();
    }
}
