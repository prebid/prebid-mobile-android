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

package org.prebid.mobile.rendering.interstitial;

import android.text.TextUtils;

import java.util.HashSet;

/**
 * Contains enums for InterstitialVideo sizes and methods to determine if video should be portrait,
 * landscape or aspect ratio
 */
public class InterstitialSizes {

    public enum InterstitialSize {
        LANDSCAPE_480x320("480x320"),
        LANDSCAPE_480x360("480x360"),
        LANDSCAPE_768x1024("768x1024"),
        LANDSCAPE_1024x768("1024x768"),

        VERTICAL_270x480("270x480"),
        VERTICAL_300x1050("300x1050"),
        VERTICAL_320x480("320x480"),
        VERTICAL_360x480("360x480"),
        VERTICAL_360x540("360x540"),
        VERTICAL_480x640("480x640"),
        VERTICAL_576x1024("576x1024"),
        VERTICAL_720x1280("720x1280"),
        VERTICAL_768x1024("768x1024"),
        VERTICAL_960x1280("960x1280"),
        VERTICAL_1080x1920("1080x1920"),
        VERTICAL_1440x1920("1440x1920"),

        ASPECT_RATIO_300x200("300x200"),
        ASPECT_RATIO_320x240("320x240"),
        ASPECT_RATIO_400x225("400x225"),
        ASPECT_RATIO_400x300("400x300"),
        ASPECT_RATIO_480x270("480x270"),
        ASPECT_RATIO_480x320("480x320"),
        ASPECT_RATIO_640x360("640x360"),
        ASPECT_RATIO_640x480("640x480"),
        ASPECT_RATIO_1024x576("1024x576"),
        ASPECT_RATIO_1280x720("1280x720"),
        ASPECT_RATIO_1280x960("1280x960"),
        ASPECT_RATIO_1920x800("1920x800"),
        ASPECT_RATIO_1920x1080("1920x1080"),
        ASPECT_RATIO_1920x1440("1920x1440");

        private String size;

        InterstitialSize(String size) {
            this.size = size;
        }

        public String getSize() {
            return size;
        }
    }

    /**
     * @param size - String with video resolution
     * @return true if the given size is defined in Vertical enums
     */
    public static boolean isPortrait(String size) {
        if (TextUtils.isEmpty(size)) {
            return false;
        }
        HashSet<String> sizes = new HashSet<>();
        for (InterstitialSize enumSize : InterstitialSize.values()) {
            if (enumSize.name().contains("VERTICAL")) {
                sizes.add(enumSize.getSize());
            }
        }
        return sizes.contains(size);
    }

    /**
     *
     * @param size - String with video resolution
     * @return true if the given size is defined in Landscape enums
     */
    public static boolean isLandscape(String size) {
        if (TextUtils.isEmpty(size)) {
            return false;
        }
        HashSet<String> sizes = new HashSet<>();
        for (InterstitialSize enumSize : InterstitialSize.values()) {
            if (enumSize.name().contains("LANDSCAPE")) {
                sizes.add(enumSize.getSize());
            }
        }
        return sizes.contains(size);
    }
}
