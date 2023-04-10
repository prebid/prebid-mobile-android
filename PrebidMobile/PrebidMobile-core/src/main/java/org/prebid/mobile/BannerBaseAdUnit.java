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
import java.util.List;

/**
 * Contains Banner and Video parameters.
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

    /**
     * @deprecated use `setBannerParameters()`.
     */
    @Deprecated
    public void setParameters(@Nullable Parameters parameters) {
        if (parameters != null) {
            BannerParameters newParameters = new BannerParameters();
            newParameters.setApi(parameters.getApi());
            configuration.setBannerParameters(newParameters);
        }
    }

    /**
     * @deprecated use `getBannerParameters()`
     */
    @Deprecated
    @Nullable
    public Parameters getParameters() {
        BannerParameters newParameters = configuration.getBannerParameters();
        if (newParameters != null) {
            Parameters oldParameters = new Parameters();
            oldParameters.setApi(newParameters.getApi());
            return oldParameters;
        }

        return null;
    }

    /**
     * Describes an <a href="https://www.iab.com/wp-content/uploads/2016/03/OpenRTB-API-Specification-Version-2-5-FINAL.pdf">OpenRTB</a> banner object
     */
    @Deprecated
    public static class Parameters {

        /**
         * List of supported API frameworks for this impression. If an API is not explicitly listed, it is assumed not to be supported.
         */
        @Nullable
        private List<Signals.Api> api;

        @Nullable
        public List<Signals.Api> getApi() {
            return api;
        }

        public void setApi(@Nullable List<Signals.Api> api) {
            this.api = api;
        }
    }

}
