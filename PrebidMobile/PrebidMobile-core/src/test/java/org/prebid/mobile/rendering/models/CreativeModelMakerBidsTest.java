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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.loading.AdLoadListener;
import org.prebid.mobile.rendering.loading.VastParserExtractor;
import org.prebid.mobile.rendering.sdk.JSLibraryManager;
import org.prebid.mobile.test.utils.ResourceUtils;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class CreativeModelMakerBidsTest {

    private CreativeModelMakerBids modelMakerBids;
    @Mock
    private AdLoadListener mockLoadListener;
    @Mock
    private VastParserExtractor mockExtractor;
    @Mock
    private JSLibraryManager mockJsManager;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        modelMakerBids = new CreativeModelMakerBids(mockLoadListener);

        WhiteBox.setInternalState(modelMakerBids, "parserExtractor", mockExtractor);
        WhiteBox.setStaticVariableTo(JSLibraryManager.class, "sInstance", mockJsManager);
    }

    @After
    public void clean() {
        WhiteBox.setStaticVariableTo(JSLibraryManager.class, "sInstance", null);
    }


    @Test
    public void whenMakeModelsAndNoAdConfiguration_CallErrorListener() {
        modelMakerBids.makeModels(null, mock(BidResponse.class));
        verify(mockLoadListener).onFailedToLoadAd(any(AdException.class), any());
    }

    @Test
    public void whenMakeModelsAndNoBidResponse_CallErrorListener() {
        modelMakerBids.makeModels(mock(AdUnitConfiguration.class), null);
        verify(mockLoadListener).onFailedToLoadAd(any(AdException.class), any());
    }

    @Test
    public void whenMakeModelsAndBidResponseWithError_CallErrorListener() {
        BidResponse mockResponse = mock(BidResponse.class);
        when(mockResponse.hasParseError()).thenReturn(true);
        modelMakerBids.makeModels(null, mockResponse);
        verify(mockLoadListener).onFailedToLoadAd(any(AdException.class), any());
    }

    @Test
    public void whenScriptsAreNotDownloadedYet_CallErrorListener() throws IOException {
        AdUnitConfiguration configuration = new AdUnitConfiguration();
        configuration.setAdFormat(AdFormat.BANNER);

        String responseString = ResourceUtils.convertResourceToString("bidding_response_obj.json");
        BidResponse bidResponse = new BidResponse(responseString, new AdUnitConfiguration());

        when(mockJsManager.checkIfScriptsDownloadedAndStartDownloadingIfNot()).thenReturn(false);

        modelMakerBids.makeModels(configuration, bidResponse);

        verify(mockLoadListener).onFailedToLoadAd(any(AdException.class), any());
    }

    @Test
    public void whenMakeModelsAndBidRequestContainsAcjAd_CreateAcjModel() throws IOException {
        AdUnitConfiguration configuration = new AdUnitConfiguration();
        configuration.setAdFormat(AdFormat.BANNER);

        String responseString = ResourceUtils.convertResourceToString("bidding_response_obj.json");
        BidResponse bidResponse = new BidResponse(responseString, new AdUnitConfiguration());

        ArgumentCaptor<CreativeModelsMaker.Result> resultArgumentCaptor = ArgumentCaptor.forClass(CreativeModelsMaker.Result.class);

        when(mockJsManager.checkIfScriptsDownloadedAndStartDownloadingIfNot()).thenReturn(true);

        modelMakerBids.makeModels(configuration, bidResponse);
        verify(mockLoadListener).onCreativeModelReady(resultArgumentCaptor.capture());
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

        modelMakerBids.makeVideoModels(mockConfig, vast);

        verify(mockConfig).setAdFormat(eq(AdFormat.VAST));
        verify(mockExtractor).extract(eq(vast));
    }

    @Test
    public void cancel_CancelExtractor() {
        modelMakerBids.cancel();

        verify(mockExtractor).cancel();
    }
}