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

package org.prebid.mobile.rendering.utils.helpers;

import android.app.Activity;
import android.content.pm.PackageManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowPackageManager;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class MraidUtilsTest {

    @Test
    public void isFeatureSupported_TestMultipleScenarios() {
        Activity robolectricActivity = Robolectric.buildActivity(Activity.class).create().get();
        ShadowPackageManager shadowPackageManager = shadowOf(robolectricActivity.getPackageManager());
        shadowPackageManager.setSystemFeature(PackageManager.FEATURE_TELEPHONY, true);
        shadowPackageManager.setSystemFeature(PackageManager.FEATURE_LOCATION_GPS, true);

        ManagersResolver.getInstance().prepare(robolectricActivity);

        String feature = null;
        assertFalse(MraidUtils.isFeatureSupported(feature));

        feature = "";
        assertFalse(MraidUtils.isFeatureSupported(feature));

        feature = "not_existing_feature";
        assertFalse(MraidUtils.isFeatureSupported(feature));

        feature = "calendar";
        assertTrue(MraidUtils.isFeatureSupported(feature));

        feature = "sms";
        assertTrue(MraidUtils.isFeatureSupported(feature));

        feature = "tel";
        assertTrue(MraidUtils.isFeatureSupported(feature));

        feature = "storePicture";
        assertTrue(MraidUtils.isFeatureSupported(feature));

        feature = "inlineVideo";
        assertTrue(MraidUtils.isFeatureSupported(feature));

        feature = "location";
        assertTrue(MraidUtils.isFeatureSupported(feature));

        feature = "vpaid";
        assertFalse(MraidUtils.isFeatureSupported(feature));
    }
}
