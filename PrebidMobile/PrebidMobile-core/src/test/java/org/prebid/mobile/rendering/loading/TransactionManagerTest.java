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

package org.prebid.mobile.rendering.loading;

import android.app.Activity;
import android.content.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.models.CreativeModel;
import org.prebid.mobile.rendering.models.CreativeModelsMaker;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class TransactionManagerTest {
    @Mock private TransactionManagerListener mockListener;
    @Mock private Transaction mockTransaction;

    private TransactionManager transactionManager;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Context context = Robolectric.buildActivity(Activity.class).create().get();
        transactionManager = new TransactionManager(context, mockListener, mock(InterstitialManager.class));
    }

    @Test
    public void whenGetCurrentTransaction_NullResult() {
        Transaction transaction = transactionManager.getCurrentTransaction();
        assertNull(transaction);
        transactionManager.onTransactionSuccess(mockTransaction);
        transaction = transactionManager.getCurrentTransaction();
        assertNotNull(transaction);
        assertEquals(mockTransaction, transaction);
    }

    @Test
    public void whenGetCurrentTransaction_NotNullResult() {
        transactionManager.onTransactionSuccess(mockTransaction);
        Transaction transaction = transactionManager.getCurrentTransaction();
        assertNotNull(transaction);
        assertEquals(mockTransaction, transaction);
    }

    @Test
    public void whenDismissTransaction_NullResult() {
        Transaction transaction = transactionManager.dismissCurrentTransaction();
        assertNull(transaction);
    }

    @Test
    public void whenDismissTransaction_NotNullResult() {
        transactionManager.onTransactionSuccess(mockTransaction);
        Transaction transaction = mock(Transaction.class);
        transactionManager.onTransactionSuccess(transaction);
        Transaction secondTransaction = transactionManager.dismissCurrentTransaction();
        assertEquals(transaction, secondTransaction);
    }

    @Test
    public void whenResetState_TriggerTransactionDestroy() {
        transactionManager.onTransactionSuccess(mockTransaction);
        transactionManager.resetState();
        verify(mockTransaction).destroy();
    }

    @Test
    public void whenHasTransaction_ResultFalse() {
        assertFalse(transactionManager.hasTransaction());
        transactionManager.onTransactionSuccess(mockTransaction);
        assertTrue(transactionManager.hasTransaction());
    }

    @Test
    public void whenDestroy_TriggerTransactionAndLoadManager() {
        transactionManager.onTransactionSuccess(mockTransaction);
        transactionManager.destroy();
        verify(mockTransaction).destroy();
    }

    @Test
    public void whenOneTransactionInList_HasNextCreativeFalse() {
        Transaction mockTransaction = mock(Transaction.class);
        List<CreativeFactory> creativeFactories = new ArrayList<>();
        creativeFactories.add(mock(CreativeFactory.class));
        when(mockTransaction.getCreativeFactories()).thenReturn(creativeFactories);

        transactionManager.onTransactionSuccess(mockTransaction);
        assertFalse(transactionManager.hasNextCreative());
    }

    @Test
    public void whenTwoTransactionInList_HasNextCreativeTrue() {
        Transaction mockTransaction = mock(Transaction.class);
        List<CreativeFactory> creativeFactories = new ArrayList<>();
        creativeFactories.add(mock(CreativeFactory.class));
        creativeFactories.add(mock(CreativeFactory.class));
        when(mockTransaction.getCreativeFactories()).thenReturn(creativeFactories);

        transactionManager.onTransactionSuccess(mockTransaction);
        assertTrue(transactionManager.hasNextCreative());
    }

    @Test
    public void whenCurrentTransactionIsNull_ReturnFalse() {
        assertFalse(transactionManager.hasNextCreative());
    }

    @Test
    public void whenOnCreativeModelReady_TransactionCreated() {
        CreativeModelsMaker.Result mockResult = mock(CreativeModelsMaker.Result.class);
        ArrayList<CreativeModel> models = new ArrayList<>();
        models.add(mock(CreativeModel.class));
        mockResult.creativeModels = models;
        transactionManager.onCreativeModelReady(mockResult);
        assertNotNull(WhiteBox.getInternalState(transactionManager, "latestTransaction"));
    }

    @Test
    public void whenOnFailedToLoad_NotifyListener() {
        transactionManager.onFailedToLoadAd(new AdException("", ""), "");
        verify(mockListener).onFetchingFailed(any(AdException.class));
    }

    @Test
    public void whenOnTransactionSuccess_TransactionAddedAndListenerCalled() {
        transactionManager.onTransactionSuccess(mockTransaction);
        verify(mockListener).onFetchingCompleted(mockTransaction);
        assertNotNull(transactionManager.getCurrentTransaction());
    }

    @Test
    public void whenOnTransactionFailed_CallListener() {
        transactionManager.onTransactionFailure(new AdException("", ""), "");
        verify(mockListener).onFetchingFailed(any(AdException.class));
    }

    @Test
    public void whenFetchBidTransaction_AttemptToMakeModels() {
        BidResponse mockBidResponse = mock(BidResponse.class);
        when(mockBidResponse.getWinningBid()).thenReturn(mock(Bid.class));
        when(mockBidResponse.getWinningBid().getAdm()).thenReturn("adm");
        transactionManager.fetchBidTransaction(mock(AdUnitConfiguration.class), mockBidResponse);
        verify(mockListener).onFetchingFailed(any(AdException.class));
    }
}