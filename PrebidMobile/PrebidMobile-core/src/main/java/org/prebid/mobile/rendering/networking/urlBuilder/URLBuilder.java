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

package org.prebid.mobile.rendering.networking.urlBuilder;

import org.prebid.mobile.rendering.networking.parameters.AdRequestInput;
import org.prebid.mobile.rendering.networking.parameters.ParameterBuilder;

import java.util.ArrayList;

public class URLBuilder {

    private final URLPathBuilder pathBuilder;
    private final ArrayList<ParameterBuilder> paramBuilders;
    private final AdRequestInput adRequestInput;

    public URLBuilder(
            URLPathBuilder pathBuilder,
            ArrayList<ParameterBuilder> parameterBuilders,
            AdRequestInput adRequestInput
    ) {
        this.pathBuilder = pathBuilder;
        paramBuilders = parameterBuilders;
        this.adRequestInput = adRequestInput;
    }

    public BidUrlComponents buildUrl() {
        AdRequestInput adRequestInput = buildParameters(paramBuilders, this.adRequestInput);
        String initialPath = pathBuilder.buildURLPath("");
        return new BidUrlComponents(initialPath, adRequestInput);
    }

    static AdRequestInput buildParameters(ArrayList<ParameterBuilder> paramBuilders, AdRequestInput adRequestInput) {
        if (adRequestInput == null) {
            return new AdRequestInput();
        }

        AdRequestInput newAdRequestInput = adRequestInput.getDeepCopy();

        for (ParameterBuilder builder : paramBuilders) {
            builder.appendBuilderParameters(newAdRequestInput);
        }

        return newAdRequestInput;
    }
}
