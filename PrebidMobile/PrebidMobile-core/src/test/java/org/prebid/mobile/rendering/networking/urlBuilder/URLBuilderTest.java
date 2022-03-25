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

import android.app.Activity;
import android.content.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.networking.parameters.AdRequestInput;
import org.prebid.mobile.rendering.networking.parameters.ParameterBuilder;
import org.prebid.mobile.units.configuration.AdUnitConfiguration;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class URLBuilderTest {

    private AdUnitConfiguration mMockConfig;
    private Context mContext;
    private final boolean mBrowserActivityAvailable = true;

    @Before
    public void setUp() throws Exception {
        mMockConfig = mock(AdUnitConfiguration.class);
        mContext = Robolectric.buildActivity(Activity.class).create().get();
    }

    @Test
    public void testBuildURLObject() {
        URLPathBuilder mockPathBuilder = mock(URLPathBuilder.class);
        ArrayList<ParameterBuilder> parameterBuilders = new ArrayList<>();
        URLBuilder urlBuilder = new URLBuilder(mockPathBuilder, parameterBuilders, new AdRequestInput());
        assertNotNull(urlBuilder);
    }

    @Test
    public void testBuildParametersNull() throws Exception {
        AdRequestInput adRequestInput = URLBuilder.buildParameters(null, null);
        assertEquals("{}", adRequestInput.getBidRequest().getJsonObject().toString());
    }
}