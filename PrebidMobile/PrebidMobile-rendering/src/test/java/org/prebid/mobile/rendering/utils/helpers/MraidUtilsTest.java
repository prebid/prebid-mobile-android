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
