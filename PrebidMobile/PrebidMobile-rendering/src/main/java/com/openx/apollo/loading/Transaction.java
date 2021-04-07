package com.openx.apollo.loading;

import android.content.Context;

import com.openx.apollo.errors.AdException;
import com.openx.apollo.models.CreativeModel;
import com.openx.apollo.models.CreativeModelsMaker;
import com.openx.apollo.sdk.JSLibraryManager;
import com.openx.apollo.session.manager.OmAdSessionManager;
import com.openx.apollo.utils.logger.OXLog;
import com.openx.apollo.views.interstitial.InterstitialManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Transaction {

    public static final String TAG = Transaction.class.getSimpleName();

    private List<CreativeFactory> mCreativeFactories;
    private Iterator<CreativeFactory> mCreativeFactoryIterator;

    private List<CreativeModel> mCreativeModels;

    private WeakReference<Context> mContextReference;
    private Listener mListener;
    private OmAdSessionManager mOmAdSessionManager;
    private final InterstitialManager mInterstitialManager;

    private String mTransactionState;
    private String mLoaderIdentifier;

    private long mTransactionCreateTime;

    public interface Listener {

        void onTransactionSuccess(Transaction transaction);

        void onTransactionFailure(AdException e, String identifier);
    }

    private Transaction(Context context, List<CreativeModel> creativeModels,
                        String transactionState,
                        InterstitialManager interstitialManager,
                        Listener listener)
    throws AdException {
        if (context == null) {
            throw new AdException(AdException.INTERNAL_ERROR, "Transaction - Context is null");
        }

        if (creativeModels == null || creativeModels.isEmpty()) {
            throw new AdException(AdException.INTERNAL_ERROR, "Transaction - Creative models is empty");
        }

        if (listener == null) {
            throw new AdException(AdException.INTERNAL_ERROR, "Transaction - Listener is null");
        }

        mContextReference = new WeakReference<>(context);
        mCreativeModels = creativeModels;
        checkForBuiltInVideo();
        mTransactionState = transactionState;
        mListener = listener;
        mInterstitialManager = interstitialManager;

        mOmAdSessionManager = OmAdSessionManager.createNewInstance(JSLibraryManager.getInstance(context));

        mCreativeFactories = new ArrayList<>();
    }

    public static Transaction createTransaction(Context context, CreativeModelsMaker.Result result,
                                                InterstitialManager interstitialManager, Listener listener)
    throws AdException {
        Transaction transaction = new Transaction(
            context,
            result.creativeModels,
            result.transactionState,
            interstitialManager,
            listener);
        transaction.setTransactionCreateTime(System.currentTimeMillis());
        transaction.setLoaderIdentifier(result.loaderIdentifier);

        return transaction;
    }

    private void checkForBuiltInVideo() {
        try {
            if (mCreativeModels != null && mCreativeModels.size() > 1) {
                CreativeModel creativeModel = mCreativeModels.get(0);
                boolean isBannerVideo = creativeModel.getAdConfiguration().isBuiltInVideo();
                if (isBannerVideo) {
                    CreativeModel possibleEndCard = mCreativeModels.get(1);
                    possibleEndCard.getAdConfiguration().setBuiltInVideo(true);
                }
            }
        }
        catch (Exception e) {
            OXLog.error(TAG, "Failed to check for built in video override");
        }
    }

    public String getTransactionState() {
        return mTransactionState;
    }

    public void startCreativeFactories() {
        try {
            // Initialize list of CreativeFactories
            mCreativeFactories.clear();
            for (CreativeModel creativeModel : mCreativeModels) {
                CreativeFactory creativeFactory = new CreativeFactory(mContextReference.get(), creativeModel,
                                                                      new CreativeFactoryListener(this),
                                                                      mOmAdSessionManager,
                                                                      mInterstitialManager);
                mCreativeFactories.add(creativeFactory);
            }

            // Start first CreativeFactory, if any
            // On success, the CreativeFactoryListener will start the next CreativeFactory
            mCreativeFactoryIterator = mCreativeFactories.iterator();
            startNextCreativeFactory();
        }
        catch (AdException e) {
            mListener.onTransactionFailure(e, mLoaderIdentifier);
        }
    }

    public void destroy() {
        stopOmAdSession();

        for (CreativeFactory creativeFactory : mCreativeFactories) {
            creativeFactory.destroy();
        }
    }

    private boolean startNextCreativeFactory() {
        // No CreativeFactory to start
        if (mCreativeFactoryIterator == null || !mCreativeFactoryIterator.hasNext()) {
            return false;
        }

        CreativeFactory creativeFactory = mCreativeFactoryIterator.next();
        creativeFactory.start();
        return true;
    }

    private void stopOmAdSession() {
        if (mOmAdSessionManager == null) {
            OXLog.error(TAG, "Failed to stopOmAdSession. OmAdSessionManager is null");
            return;
        }

        mOmAdSessionManager.stopAdSession();
        mOmAdSessionManager = null;
    }

    public List<CreativeFactory> getCreativeFactories() {
        return mCreativeFactories;
    }

    public String getLoaderIdentifier() {
        return mLoaderIdentifier;
    }

    public void setLoaderIdentifier(String loaderIdentifier) {
        mLoaderIdentifier = loaderIdentifier;
    }

    public long getTransactionCreateTime() {
        return mTransactionCreateTime;
    }

    public void setTransactionCreateTime(long transactionCreateTime) {
        mTransactionCreateTime = transactionCreateTime;
    }

    /**
     * Listens for when CreativeFactory is done making a creative
     * When all CreativeFactory's are done, relays that back to Transaction's Listener
     */
    public static class CreativeFactoryListener implements CreativeFactory.Listener {

        private WeakReference<Transaction> mWeakTransaction;

        CreativeFactoryListener(Transaction transaction) {
            mWeakTransaction = new WeakReference<>(transaction);
        }

        @Override
        public void onSuccess() {
            Transaction transaction = mWeakTransaction.get();
            if (transaction == null) {
                OXLog.warn(TAG, "CreativeMaker is null");
                return;
            }

            // Start next CreativeFactory, if any
            if (transaction.startNextCreativeFactory()) {
                return;
            }

            // If all CreativeFactories succeeded, return success
            transaction.mListener.onTransactionSuccess(transaction);
        }

        @Override
        public void onFailure(AdException e) {
            Transaction transaction = mWeakTransaction.get();
            if (transaction == null) {
                OXLog.warn(TAG, "CreativeMaker is null");
                return;
            }

            transaction.mListener.onTransactionFailure(e, transaction.getLoaderIdentifier());
            transaction.destroy();
        }
    }
}
