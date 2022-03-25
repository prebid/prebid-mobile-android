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

package org.prebid.mobile.rendering.models;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.loading.AdLoadListener;
import org.prebid.mobile.rendering.loading.VastParserExtractor;
import org.prebid.mobile.test.utils.ResourceUtils;
import org.prebid.mobile.test.utils.WhiteBox;
import org.prebid.mobile.units.configuration.AdUnitConfiguration;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class CreativeModelMakerBidsTest {

    private CreativeModelMakerBids mModelMakerBids;
    @Mock
    private AdLoadListener mMockLoadListener;
    @Mock
    private VastParserExtractor mMockExtractor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mModelMakerBids = new CreativeModelMakerBids(mMockLoadListener);

        WhiteBox.setInternalState(mModelMakerBids, "mParserExtractor", mMockExtractor);
    }

    @Test
    public void whenMakeModelsAndNoAdConfiguration_CallErrorListener() {
        mModelMakerBids.makeModels(null, mock(BidResponse.class));
        verify(mMockLoadListener).onFailedToLoadAd(any(AdException.class), any());
    }

    @Test
    public void whenMakeModelsAndNoBidResponse_CallErrorListener() {
        mModelMakerBids.makeModels(mock(AdUnitConfiguration.class), null);
        verify(mMockLoadListener).onFailedToLoadAd(any(AdException.class), any());
    }

    @Test
    public void whenMakeModelsAndBidResponseWithError_CallErrorListener() {
        BidResponse mockResponse = mock(BidResponse.class);
        when(mockResponse.hasParseError()).thenReturn(true);
        mModelMakerBids.makeModels(null, mockResponse);
        verify(mMockLoadListener).onFailedToLoadAd(any(AdException.class), any());
    }

    @Test
    public void whenMakeModelsAndBidRequestContainsAcjAd_CreateAcjModel() throws IOException {
        AdUnitConfiguration configuration = new AdUnitConfiguration();
        configuration.setAdUnitIdentifierType(AdUnitConfiguration.AdUnitIdentifierType.BANNER);

        String responseString = ResourceUtils.convertResourceToString("bidding_response_obj.json");
        BidResponse bidResponse = new BidResponse(responseString);

        ArgumentCaptor<CreativeModelsMaker.Result> resultArgumentCaptor = ArgumentCaptor.forClass(CreativeModelsMaker.Result.class);

        mModelMakerBids.makeModels(configuration, bidResponse);
        verify(mMockLoadListener).onCreativeModelReady(resultArgumentCaptor.capture());
        CreativeModel creativeModel = resultArgumentCaptor.getValue().creativeModels.get(0);
        Bid bid = bidResponse.getSeatbids().get(0).getBids().get(0);
        assertEquals("HTML", creativeModel.getName());
        assertEquals(bid.getAdm(), creativeModel.getHtml());
        assertEquals(bid.getWidth(), creativeModel.getWidth());
        assertEquals(bid.getHeight(), creativeModel.getHeight());
        assertFalse(creativeModel.isRequireImpressionUrl());
    }

    @Test
    public void makeVideoModels_ExecuteVastParserExtractor() {
        final AdUnitConfiguration mockConfig = mock(AdUnitConfiguration.class);
        final String vast = "1234";

        mModelMakerBids.makeVideoModels(mockConfig, vast);

        verify(mockConfig).setAdUnitIdentifierType(eq(AdUnitConfiguration.AdUnitIdentifierType.VAST));
        verify(mMockExtractor).extract(eq(vast));
    }

    @Test
    public void cancel_CancelExtractor() {
        mModelMakerBids.cancel();

        verify(mMockExtractor).cancel();
    }
}