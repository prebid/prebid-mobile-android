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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.prebid.mobile.api.data.AdFormat;

import java.util.EnumSet;

/**
 * Base banner ad unit with banner and video parameters.
 */
public abstract class BannerBaseAdUnit extends AdUnit {

    BannerBaseAdUnit(@NonNull String configId, @NonNull EnumSet<AdFormat> adType) {
        super(configId, adType);
    }

    @Nullable
    public BannerParameters getBannerParameters() {
        return configuration.getBannerParameters();
    }

    public void setBannerParameters(@Nullable BannerParameters parameters) {
        configuration.setBannerParameters(parameters);
    }

    @Nullable
    public VideoParameters getVideoParameters() {
        return configuration.getVideoParameters();
    }

    public void setVideoParameters(@Nullable VideoParameters parameters) {
        configuration.setVideoParameters(parameters);
    }

}
