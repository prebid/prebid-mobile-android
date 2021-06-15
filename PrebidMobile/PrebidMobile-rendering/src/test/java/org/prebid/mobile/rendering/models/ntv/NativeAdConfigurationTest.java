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

package org.prebid.mobile.rendering.models.ntv;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.assets.NativeAsset;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class NativeAdConfigurationTest {
    private NativeAdConfiguration mNativeAdConfiguration;

    @Before
    public void setUp() throws Exception {
        mNativeAdConfiguration = new NativeAdConfiguration();
        NativeAdConfiguration.ContextType.CUSTOM.setId(500);
        NativeAdConfiguration.ContextSubType.CUSTOM.setId(500);
        NativeAdConfiguration.PlacementType.CUSTOM.setId(500);
    }

    @Test
    public void whenAddAsset_AssetWasAddedToTheList() {
        assertEquals(0, mNativeAdConfiguration.getAssets().size());
        mNativeAdConfiguration.addAsset(mock(NativeAsset.class));
        assertEquals(1, mNativeAdConfiguration.getAssets().size());
    }

    @Test
    public void whenAddTracker_TrackerWasAddedToTheList() {
        assertEquals(0, mNativeAdConfiguration.getTrackers().size());
        mNativeAdConfiguration.addTracker(mock(NativeEventTracker.class));
        assertEquals(1, mNativeAdConfiguration.getTrackers().size());
    }

    @Test
    public void whenSetContextType_ContextTypeWasSet() {
        assertNull(mNativeAdConfiguration.getContextType());
        mNativeAdConfiguration.setContextType(NativeAdConfiguration.ContextType.CONTENT_CENTRIC);
        assertEquals(NativeAdConfiguration.ContextType.CONTENT_CENTRIC, mNativeAdConfiguration.getContextType());
    }

    @Test
    public void whenSetContextSubType_ContextSubTypeWasSet() {
        assertNull(mNativeAdConfiguration.getContextSubType());
        mNativeAdConfiguration.setContextSubType(NativeAdConfiguration.ContextSubType.GENERAL);
        assertEquals(NativeAdConfiguration.ContextSubType.GENERAL, mNativeAdConfiguration.getContextSubType());
    }

    @Test
    public void whenSetPlacementType_PlacementTypeWasSet() {
        assertNull(mNativeAdConfiguration.getPlacementType());
        mNativeAdConfiguration.setPlacementType(NativeAdConfiguration.PlacementType.CONTENT_FEED);
        assertEquals(NativeAdConfiguration.PlacementType.CONTENT_FEED, mNativeAdConfiguration.getPlacementType());
    }

    @Test
    public void whenSetExt_ExtWasSet() {
        assertNull(mNativeAdConfiguration.getExt());
        mNativeAdConfiguration.setExt(mock(Ext.class));
        assertNotNull(mNativeAdConfiguration.getExt());
    }

    @Test
    public void whenSetPrivacy_PrivacyWasSet() {
        assertFalse(mNativeAdConfiguration.getPrivacy());
        mNativeAdConfiguration.setPrivacy(true);
        assertTrue(mNativeAdConfiguration.getPrivacy());
    }

    @Test
    public void whenSetSeq_SeqWasSet() {
        assertNull(mNativeAdConfiguration.getSeq());
        mNativeAdConfiguration.setSeq(1);
        assertEquals(1, mNativeAdConfiguration.getSeq().intValue());
    }

    @Test
    public void whenSetSeqLessThanZero_SeqNotChanged() {
        assertNull(mNativeAdConfiguration.getSeq());
        mNativeAdConfiguration.setSeq(-1);
        assertNull(mNativeAdConfiguration.getSeq());
    }

    /// Enums test

    @Test
    public void whenContextTypeSetIdAndTypeNotCustom_IdWasNotChanged() {
        NativeAdConfiguration.ContextType contextType = NativeAdConfiguration.ContextType.CONTENT_CENTRIC;
        assertEquals(1, contextType.getId());
        contextType.setId(501);
        assertEquals(1, contextType.getId());
    }

    @Test
    public void whenContextTypeSetIdAndTypeCustomAndInExistingValue_IdWasNotChanged() {
        NativeAdConfiguration.ContextType contextType = NativeAdConfiguration.ContextType.CUSTOM;
        assertEquals(500, contextType.getId());
        contextType.setId(1);
        assertEquals(500, contextType.getId());
    }

    @Test
    public void whenContextTypeSetIdAndTypeCustomAndNotInExistingValue_IdWasChanged() {
        NativeAdConfiguration.ContextType contextType = NativeAdConfiguration.ContextType.CUSTOM;
        assertEquals(500, contextType.getId());
        contextType.setId(501);
        assertEquals(501, contextType.getId());
    }

    @Test
    public void whenContextSubTypeSetIdAndTypeNotCustom_IdWasNotChanged() {
        NativeAdConfiguration.ContextSubType contextSubType = NativeAdConfiguration.ContextSubType.GENERAL;
        assertEquals(10, contextSubType.getId());
        contextSubType.setId(501);
        assertEquals(10, contextSubType.getId());
    }

    @Test
    public void whenContextSubTypeSetIdAndTypeCustomAndInExistingValue_IdWasNotChanged() {
        NativeAdConfiguration.ContextSubType contextSubType = NativeAdConfiguration.ContextSubType.CUSTOM;
        assertEquals(500, contextSubType.getId());
        contextSubType.setId(10);
        assertEquals(500, contextSubType.getId());
    }

    @Test
    public void whenContextSubTypeSetIdAndTypeCustomAndNotInExistingValue_IdWasChanged() {
        NativeAdConfiguration.ContextSubType contextSubType = NativeAdConfiguration.ContextSubType.CUSTOM;
        assertEquals(500, contextSubType.getId());
        contextSubType.setId(501);
        assertEquals(501, contextSubType.getId());
    }

    @Test
    public void whenPlacementTypeSetIdAndTypeNotCustom_IdWasNotChanged() {
        NativeAdConfiguration.PlacementType placementType = NativeAdConfiguration.PlacementType.CONTENT_FEED;
        assertEquals(1, placementType.getId());
        placementType.setId(501);
        assertEquals(1, placementType.getId());
    }

    @Test
    public void whenPlacementTypeSetIdAndTypeCustomAndInExistingValue_IdWasNotChanged() {
        NativeAdConfiguration.PlacementType placementType = NativeAdConfiguration.PlacementType.CUSTOM;
        assertEquals(500, placementType.getId());
        placementType.setId(1);
        assertEquals(500, placementType.getId());
    }

    @Test
    public void whenPlacementTypeSetIdAndTypeCustomAndNotInExistingValue_IdWasChanged() {
        NativeAdConfiguration.PlacementType placementType = NativeAdConfiguration.PlacementType.CUSTOM;
        assertEquals(500, placementType.getId());
        placementType.setId(501);
        assertEquals(501, placementType.getId());
    }
}