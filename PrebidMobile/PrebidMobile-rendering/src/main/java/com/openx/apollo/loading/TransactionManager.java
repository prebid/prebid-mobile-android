package com.openx.apollo.loading;

import android.content.Context;

import com.openx.apollo.bidding.data.bid.BidResponse;
import com.openx.apollo.errors.AdException;
import com.openx.apollo.models.AbstractCreative;
import com.openx.apollo.models.AdConfiguration;
import com.openx.apollo.models.CreativeModelMakerBids;
import com.openx.apollo.models.CreativeModelsMaker;
import com.openx.apollo.utils.logger.OXLog;
import com.openx.apollo.views.interstitial.InterstitialManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class TransactionManager implements AdLoadListener, Transaction.Listener {
    private static final String TAG = "TransactionManager";

    private final WeakReference<Context> mWeakContextReference;
    private final List<Transaction> mTransactions = new ArrayList<>();
    private final InterstitialManager mInterstitialManager;
    private final CreativeModelMakerBids mCreativeModelMakerBids = new CreativeModelMakerBids(this);

    private Transaction mLatestTransaction;
    private int mCurrentTransactionCreativeIndex;
    private TransactionManagerListener mListener;

    public TransactionManager(Context context, TransactionManagerListener listener, InterstitialManager interstitialManager) {
        mWeakContextReference = new WeakReference<>(context);
        mListener = listener;
        mInterstitialManager = interstitialManager;
    }

    //// AdLoadManager.Listener implementation
    @Override
    public void onCreativeModelReady(CreativeModelsMaker.Result result) {
        try {
            // Assign transaction to a field to prevent from being destroyed
            mLatestTransaction = Transaction.createTransaction(mWeakContextReference.get(),
                                                               result,
                                                               mInterstitialManager,
                                                               this);
            mLatestTransaction.startCreativeFactories();
        }
        catch (AdException e) {
            notifyListenerError(e);
        }
    }

    @Override
    public void onFailedToLoadAd(AdException e, String vastLoaderIdentifier) {
        notifyListenerError(e);
    }

    //// Transaction.Listener implementation
    @Override
    public void onTransactionSuccess(Transaction transaction) {
        mLatestTransaction = null;
        if (mListener == null) {
            OXLog.warn(TAG, "Unable to notify listener. Listener is null");
            return;
        }
        mTransactions.add(transaction);
        mListener.onFetchingCompleted(transaction);
    }

    @Override
    public void onTransactionFailure(AdException e, String identifier) {
        notifyListenerError(e);
    }

    /**
     * Initiates the process of creating creative model and transaction from parsed bid response
     *
     * @param adConfiguration - OXBAdConfiguration
     * @param bidResponse     - parsed bid response
     */
    public void fetchBidTransaction(AdConfiguration adConfiguration, BidResponse bidResponse) {
        mCreativeModelMakerBids.makeModels(adConfiguration, bidResponse);
    }

    public void fetchVideoTransaction(AdConfiguration adConfiguration, String vastXml) {
        mCreativeModelMakerBids.makeVideoModels(adConfiguration, vastXml);
    }

    /**
     * Returns the transaction that should be displayed right now.
     *
     * @return first transaction
     */
    public Transaction getCurrentTransaction() {
        if (hasTransaction()) {
            return mTransactions.get(0);
        }
        return null;
    }

    /**
     * Removes the current transaction from internal cache
     *
     * @return the next transactions in the list
     */
    public Transaction dismissCurrentTransaction() {
        if (hasTransaction()) {
            mTransactions.remove(0);
        }
        return getCurrentTransaction();
    }

    public AbstractCreative getCurrentCreative() {
        Transaction transaction = getCurrentTransaction();
        if (transaction == null) {
            OXLog.error(TAG, "Get Current creative called with no ad");
            return null;
        }
        return transaction.getCreativeFactories().get(mCurrentTransactionCreativeIndex).getCreative();
    }

    public boolean hasNextCreative() {
        Transaction currentTransaction = getCurrentTransaction();
        if (currentTransaction == null) {
            return false;
        }
        int creativeFactoriesSize = currentTransaction.getCreativeFactories().size();
        return mCurrentTransactionCreativeIndex < creativeFactoriesSize - 1;
    }

    public void resetState() {
        Transaction transaction = getCurrentTransaction();
        if (transaction != null) {
            transaction.destroy();
            mTransactions.remove(0);
        }
        mCurrentTransactionCreativeIndex = 0;
        cancelBidModelMaker();
    }

    public boolean hasTransaction() {
        return !mTransactions.isEmpty();
    }

    public void destroy() {
        for (Transaction transaction : mTransactions) {
            transaction.destroy();
        }
        if (mLatestTransaction != null) {
            mLatestTransaction.destroy();
            mLatestTransaction = null;
        }

        cancelBidModelMaker();
        mListener = null;
    }

    public void incrementCreativesCounter() {
        mCurrentTransactionCreativeIndex++;
    }

    private void cancelBidModelMaker() {
        if (mCreativeModelMakerBids != null) {
            mCreativeModelMakerBids.cancel();
        }
    }

    private void notifyListenerError(AdException e) {
        if (mListener == null) {
            OXLog.warn(TAG, "Unable to notify listener. Listener is null");
            return;
        }
        mListener.onFetchingFailed(e);
    }
}
