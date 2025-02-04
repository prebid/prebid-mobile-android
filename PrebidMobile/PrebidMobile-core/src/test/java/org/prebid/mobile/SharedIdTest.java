package org.prebid.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearAllCaches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.testutils.BaseSetup;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK)
public class SharedIdTest extends BaseSetup {

    @Before
    public void setup() {
        super.setup();
        PrebidMobile.initializeSdk(activity, null);
        mockStatic(TargetingParams.class);
    }

    @After
    public void tearDown() {
        super.tearDown();
        clearAllCaches();
    }

    @Test
    public void sharedIdReturnsSameIdentifierWithinSession() throws Exception {
        ExternalUserId id1 = SharedId.getIdentifier();
        ExternalUserId id2 = SharedId.getIdentifier();
        assertEquals(id1, id2);
    }

    @Test
    public void sharedIdGeneratesNewIdentifierAfterReset() throws Exception {
        ExternalUserId id1 = SharedId.getIdentifier();
        SharedId.resetIdentifier();
        ExternalUserId id2 = SharedId.getIdentifier();
        assertNotEquals(id1, id2);
    }

    @Test
    public void sharedIdUsesStoredIdentifierIfAvailable() throws Exception {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = pref.edit();

        String storedId = "stored-identifier";
        editor.putString(SharedId.PB_SharedIdKey, storedId).apply();
        when(TargetingParams.getDeviceAccessConsent()).thenReturn(true);
        ExternalUserId id = SharedId.getIdentifier();
        assertEquals(storedId, id.getIdentifier());
    }

    @Test
    public void sharedIdGeneratesNewIdentifierIfNoStoredId() throws Exception {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = pref.edit();

        editor.remove(SharedId.PB_SharedIdKey).apply();
        when(TargetingParams.getDeviceAccessConsent()).thenReturn(true);
        ExternalUserId id = SharedId.getIdentifier();
        assertNotNull(id.getIdentifier());
    }
}
