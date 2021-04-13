package org.prebid.mobile.rendering.loading;

import android.app.Activity;
import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.models.AdConfiguration;
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class TransactionManagerTest {
    @Mock
    private TransactionManagerListener mMockListener;
    @Mock
    private Transaction mMockTransaction;

    private TransactionManager mTransactionManager;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Context context = Robolectric.buildActivity(Activity.class).create().get();
        mTransactionManager = new TransactionManager(context, mMockListener, mock(InterstitialManager.class));
    }

    @Test
    public void whenGetCurrentTransaction_NullResult() {
        Transaction transaction = mTransactionManager.getCurrentTransaction();
        assertNull(transaction);
        mTransactionManager.onTransactionSuccess(mMockTransaction);
        transaction = mTransactionManager.getCurrentTransaction();
        assertNotNull(transaction);
        assertEquals(mMockTransaction, transaction);
    }

    @Test
    public void whenGetCurrentTransaction_NotNullResult() {
        mTransactionManager.onTransactionSuccess(mMockTransaction);
        Transaction transaction = mTransactionManager.getCurrentTransaction();
        assertNotNull(transaction);
        assertEquals(mMockTransaction, transaction);
    }

    @Test
    public void whenDismissTransaction_NullResult() {
        Transaction transaction = mTransactionManager.dismissCurrentTransaction();
        assertNull(transaction);
    }

    @Test
    public void whenDismissTransaction_NotNullResult() {
        mTransactionManager.onTransactionSuccess(mMockTransaction);
        Transaction transaction = mock(Transaction.class);
        mTransactionManager.onTransactionSuccess(transaction);
        Transaction secondTransaction = mTransactionManager.dismissCurrentTransaction();
        assertEquals(transaction, secondTransaction);
    }

    @Test
    public void whenResetState_TriggerTransactionDestroy() {
        mTransactionManager.onTransactionSuccess(mMockTransaction);
        mTransactionManager.resetState();
        verify(mMockTransaction).destroy();
    }

    @Test
    public void whenHasTransaction_ResultFalse() {
        assertFalse(mTransactionManager.hasTransaction());
        mTransactionManager.onTransactionSuccess(mMockTransaction);
        assertTrue(mTransactionManager.hasTransaction());
    }

    @Test
    public void whenDestroy_TriggerTransactionAndLoadManager() {
        mTransactionManager.onTransactionSuccess(mMockTransaction);
        mTransactionManager.destroy();
        verify(mMockTransaction).destroy();
    }

    @Test
    public void whenOneTransactionInList_HasNextCreativeFalse() {
        Transaction mockTransaction = mock(Transaction.class);
        List<CreativeFactory> creativeFactories = new ArrayList<>();
        creativeFactories.add(mock(CreativeFactory.class));
        when(mockTransaction.getCreativeFactories()).thenReturn(creativeFactories);

        mTransactionManager.onTransactionSuccess(mockTransaction);
        assertFalse(mTransactionManager.hasNextCreative());
    }

    @Test
    public void whenTwoTransactionInList_HasNextCreativeTrue() {
        Transaction mockTransaction = mock(Transaction.class);
        List<CreativeFactory> creativeFactories = new ArrayList<>();
        creativeFactories.add(mock(CreativeFactory.class));
        creativeFactories.add(mock(CreativeFactory.class));
        when(mockTransaction.getCreativeFactories()).thenReturn(creativeFactories);

        mTransactionManager.onTransactionSuccess(mockTransaction);
        assertTrue(mTransactionManager.hasNextCreative());
    }

    @Test
    public void whenCurrentTransactionIsNull_ReturnFalse() {
        assertFalse(mTransactionManager.hasNextCreative());
    }

    @Test
    public void whenOnCreativeModelReady_TransactionCreated() {
        CreativeModelsMaker.Result mockResult = mock(CreativeModelsMaker.Result.class);
        ArrayList<CreativeModel> models = new ArrayList<>();
        models.add(mock(CreativeModel.class));
        mockResult.creativeModels = models;
        mTransactionManager.onCreativeModelReady(mockResult);
        assertNotNull(WhiteBox.getInternalState(mTransactionManager, "mLatestTransaction"));
    }

    @Test
    public void whenOnFailedToLoad_NotifyListener() {
        mTransactionManager.onFailedToLoadAd(new AdException("", ""), "");
        verify(mMockListener).onFetchingFailed(any(AdException.class));
    }

    @Test
    public void whenOnTransactionSuccess_TransactionAddedAndListenerCalled() {
        mTransactionManager.onTransactionSuccess(mMockTransaction);
        verify(mMockListener).onFetchingCompleted(mMockTransaction);
        assertNotNull(mTransactionManager.getCurrentTransaction());
    }

    @Test
    public void whenOnTransactionFailed_CallListener() {
        mTransactionManager.onTransactionFailure(new AdException("", ""), "");
        verify(mMockListener).onFetchingFailed(any(AdException.class));
    }

    @Test
    public void whenFetchBidTransaction_AttemptToMakeModels() {
        BidResponse mockBidResponse = mock(BidResponse.class);
        when(mockBidResponse.getWinningBid()).thenReturn(mock(Bid.class));
        when(mockBidResponse.getWinningBid().getAdm()).thenReturn("adm");
        mTransactionManager.fetchBidTransaction(mock(AdConfiguration.class), mockBidResponse);
        verify(mMockListener).onFetchingFailed(any(AdException.class));
    }
}