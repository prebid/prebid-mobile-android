package org.prebid.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.prebid.mobile.testutils.BaseSetup;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK)
public class SharedIdTest {

    private MockedStatic<TargetingParams> paramsMock;
    private MockedStatic<PreferenceManager> prefsMock;

    @Before
    public void setup() {
        paramsMock = mockStatic(TargetingParams.class);
        prefsMock = mockStatic(PreferenceManager.class);

    }

    @After
    public void tearDown() {
        if (paramsMock != null) {
            paramsMock.close();
        }
        if (prefsMock != null) {
            prefsMock.close();
        }
    }

    @Test
    public void sharedIdReturnsSameIdentifierWithinSession() throws Exception {
        ExternalUserId id1 = SharedId.getIdentifier();
        ExternalUserId id2 = SharedId.getIdentifier();

        assertNotNull(id1);
        assertNotNull(id2);
        assertEquals(id1, id2);
    }

    @Test
    public void sharedIdGeneratesNewIdentifierAfterReset() throws Exception {
        when(TargetingParams.getDeviceAccessConsent()).thenReturn(true);
        ExternalUserId id1 = SharedId.getIdentifier();
        assertNotNull(id1);

        SharedId.resetIdentifier();
        ExternalUserId id2 = SharedId.getIdentifier();
        assertNotNull(id2);

        assertNotEquals(id1, id2);
    }

    @Test
    public void sharedIdUsesStoredIdentifierIfAvailable() throws Exception {
        String storedId = "stored-identifier";
        when(TargetingParams.getDeviceAccessConsent()).thenReturn(true);
        SharedPreferences prefsMock = mockSharedPreferences();
        when(prefsMock.getString(any(), any())).thenReturn(storedId);

        ExternalUserId id = SharedId.getIdentifier();
        ExternalUserId.UniqueId uniqueId = id.getUniqueIds().get(0);
        assertEquals(storedId, uniqueId.getId());
    }

    @Test
    public void sharedIdGeneratesNewIdentifierIfNoStoredId() throws Exception {
        when(SharedId.fetchSharedId()).thenReturn(null);
        when(TargetingParams.getDeviceAccessConsent()).thenReturn(true);

        ExternalUserId id = SharedId.getIdentifier();
        ExternalUserId.UniqueId uniqueId = id.getUniqueIds().get(0);
        assertNotNull(uniqueId.getId());
    }

    private SharedPreferences mockSharedPreferences() {
        SharedPreferences mockPrefs = mock(SharedPreferences.class);
        when(PreferenceManager.getDefaultSharedPreferences(any())).thenReturn(mockPrefs);
        return mockPrefs;
    }
}
