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
 * Video base ad unit with video parameters.
 */
public abstract class VideoBaseAdUnit extends AdUnit {

    VideoBaseAdUnit(@NonNull String configId, @NonNull EnumSet<AdFormat> adType) {
        super(configId, adType);
    }

    @Nullable
    public VideoParameters getVideoParameters() {
        return configuration.getVideoParameters();
    }

    public void setVideoParameters(@Nullable VideoParameters parameters) {
        configuration.setVideoParameters(parameters);
    }

    @Nullable
    public String getImpOrtbConfig() { return configuration.getImpOrtbConfig(); }

    public void setImpOrtbConfig(@Nullable String ortbConfig) { configuration.setImpOrtbConfig(ortbConfig);}

}
