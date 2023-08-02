package org.prebid.mobile.eventhandlers.global;

import static org.junit.Assert.assertEquals;

import com.google.android.gms.ads.MobileAds;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.PrebidMobile;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 29)
public class GoogleAdVersionTest {

    @Test
    public void checkIfLastVersionUsed() {
        String currentVersion = MobileAds.getVersion().toString();
        assertEquals(
                "Google Ad SDK was updated to " + currentVersion + "! " +
                        "Please test Prebid SDK with the new version, resolve compilation problems, rewrite deprecated code. " +
                        "After testing you must update PrebidMobile.TESTED_GOOGLE_SDK_VERSION" +
                        "and also must update version in publishing XML files (scripts/Maven/*.xml.",
                currentVersion,
                PrebidMobile.TESTED_GOOGLE_SDK_VERSION
        );
    }

}
