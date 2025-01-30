package org.prebid.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.clearAllCaches;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

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
        mockStatic(StorageUtils.class);
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
        String storedId = "stored-identifier";
        when(StorageUtils.fetchSharedId()).thenReturn(storedId);
        when(TargetingParams.getDeviceAccessConsent()).thenReturn(true);
        ExternalUserId id = SharedId.getIdentifier();
        assertEquals(storedId, id.getIdentifier());
    }

    @Test
    public void sharedIdGeneratesNewIdentifierIfNoStoredId() throws Exception {
        when(StorageUtils.fetchSharedId()).thenReturn(null);
        when(TargetingParams.getDeviceAccessConsent()).thenReturn(true);
        ExternalUserId id = SharedId.getIdentifier();
        assertNotNull(id.getIdentifier());
    }
}
