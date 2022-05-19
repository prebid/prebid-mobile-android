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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.rendering.models.CreativeModel;
import org.prebid.mobile.rendering.models.CreativeModelsMaker;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.*;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class TransactionTest {

    private Context mockContext;

    @Before
    public void setUp() throws Exception {
        Activity testActivity = Robolectric.buildActivity(Activity.class).create().get();
        mockContext = testActivity.getApplicationContext();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testTransactionInit() throws Exception {
        List<CreativeModel> creativeModels = new ArrayList<>();
        CreativeModel mockCreativeModel = mock(CreativeModel.class);
        creativeModels.add(mockCreativeModel);
        Transaction.Listener mockOxTransactionListener = mock(Transaction.Listener.class);

        // Valid
        InterstitialManager mockInterstitialManager = mock(InterstitialManager.class);
        Transaction transaction = Transaction.createTransaction(mockContext,
                createModelResult(creativeModels, "ts"),
                mockInterstitialManager,
                mockOxTransactionListener
        );
        assertNotNull(transaction);

        // No context
        boolean hasException = false;
        try {
            Transaction.createTransaction(
                null,
                createModelResult(creativeModels, "ts"),
                mockInterstitialManager,
                mockOxTransactionListener);
        }
        catch (AdException e) {
            hasException = true;
        }
        assertTrue(hasException);

        // No creative models
        hasException = false;
        try {
            Transaction.createTransaction(mockContext,
                    createModelResult(null, "ts"),
                    mockInterstitialManager,
                    mockOxTransactionListener
            );
        }
        catch (AdException e) {
            hasException = true;
        }
        assertTrue(hasException);

        // Empty creative models
        hasException = false;
        try {
            Transaction.createTransaction(mockContext,
                    createModelResult(new ArrayList<>(), "ts"),
                    mockInterstitialManager,
                    mockOxTransactionListener
            );
        }
        catch (AdException e) {
            hasException = true;
        }
        assertTrue(hasException);

        // No listener
        hasException = false;
        try {
            Transaction.createTransaction(mockContext,
                    createModelResult(creativeModels, "ts"),
                    mockInterstitialManager,
                    null
            );
        }
        catch (AdException e) {
            hasException = true;
        }
        assertTrue(hasException);
    }

    // Tests that CreativeFactory is started
    @Test
    public void testStartCreativeFactories() throws Exception {
        CreativeModel mockCreativeModel = mock(CreativeModel.class);
        List<CreativeModel> creativeModels = new ArrayList<>();
        creativeModels.add(mockCreativeModel);
        Transaction.Listener mockOxTransactionListener = mock(Transaction.Listener.class);
        final Transaction transaction = Transaction.createTransaction(
                mockContext,
                createModelResult(creativeModels, "ts"),
                mock(InterstitialManager.class),
                mockOxTransactionListener
        );

        transaction.startCreativeFactories();

        verify(mockOxTransactionListener).onTransactionFailure(any(AdException.class), anyString());
    }

    // Tests when creative factories return
    @Test
    public void testCreativeFactoryListener() throws Exception {
        List<CreativeModel> mockCreativeModels = Collections.singletonList(mock(CreativeModel.class));
        Transaction.Listener mockListener = mock(Transaction.Listener.class);
        Transaction transaction = Transaction.createTransaction(
                mockContext,
                createModelResult(mockCreativeModels, ""),
                mock(InterstitialManager.class),
                mockListener
        );
        Transaction.CreativeFactoryListener creativeFactoryListener = new Transaction.CreativeFactoryListener(
                transaction);

        // No more Creatives to construct
        // Transaction.Listener.onSuccess is called
        creativeFactoryListener.onSuccess();
        verify(mockListener).onTransactionSuccess(transaction);

        // More Creatives to construct
        // Transaction.Listener.onSuccess is not called
        reset(mockListener);
        Iterator<CreativeFactory> mockIterator = mock(Iterator.class);
        when(mockIterator.hasNext()).thenReturn(true);
        when(mockIterator.next()).thenReturn(mock(CreativeFactory.class));
        WhiteBox.setInternalState(transaction, "creativeFactoryIterator", mockIterator);
        creativeFactoryListener.onSuccess();
        verify(mockListener, never()).onTransactionSuccess(transaction);

        // On failure, Transaction.Listener.onFailure should be called
        AdException adException = new AdException("type", "message");
        creativeFactoryListener.onFailure(adException);
        verify(mockListener).onTransactionFailure(eq(adException), anyString());
    }

    @Test
    public void onSuccessWithCreativeTimeout_TransactionListenerSuccessNotCalled()
    throws Exception {
        List<CreativeModel> creativeModels = Arrays.asList(mock(CreativeModel.class), mock(CreativeModel.class));
        Transaction.Listener mockListener = mock(Transaction.Listener.class);
        InterstitialManager mockInterstitialManager = mock(InterstitialManager.class);

        Transaction transaction = Transaction.createTransaction(
                mockContext,
                createModelResult(creativeModels, ""),
                mockInterstitialManager,
                mockListener
        );
        Transaction.CreativeFactoryListener creativeFactoryListener = new Transaction.CreativeFactoryListener(
                transaction);
        Iterator<CreativeFactory> mockIterator = mock(Iterator.class);
        when(mockIterator.hasNext()).thenReturn(true);
        when(mockIterator.next()).thenReturn(mock(CreativeFactory.class));
        WhiteBox.setInternalState(transaction, "creativeFactoryIterator", mockIterator);
        creativeFactoryListener.onSuccess();
        verify(mockListener, never()).onTransactionSuccess(transaction);

        AdException adException = new AdException(AdException.INTERNAL_ERROR, "CreativeFactory Timeout");
        creativeFactoryListener.onFailure(adException);
        verify(mockListener).onTransactionFailure(eq(adException), anyString());
    }

    private CreativeModelsMaker.Result createModelResult(List<CreativeModel> creativeModels, String state) {
        CreativeModelsMaker.Result result = new CreativeModelsMaker.Result();
        result.creativeModels = creativeModels;
        result.transactionState = state;
        result.loaderIdentifier = "123";
        return result;
    }
}