package org.prebid.mobile.eventhandlers.global;

import com.google.android.gms.ads.MobileAds;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 29)
public class GoogleAdVersionTest {

    private static final String LAST_TESTED_VERSION = "21.3.0";

    @Test
    public void checkIfLastVersionUsed() {
        String currentVersion = MobileAds.getVersion().toString();
        assertEquals(
                "Google Ad SDK was updated to " + currentVersion + "! Please test Prebid SDK with the new version, resolve compilation problems, rewrite deprecated code. After testing you can update LAST_TESTED_VERSION.",
                currentVersion,
                LAST_TESTED_VERSION
        );
    }

}
