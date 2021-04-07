package com.openx.apollo.loading;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.openx.apollo.errors.AdException;
import com.openx.apollo.errors.VastParseError;
import com.openx.apollo.models.internal.VastExtractorResult;
import com.openx.apollo.networking.BaseNetworkTask;
import com.openx.apollo.networking.ResponseHandler;
import com.openx.apollo.networking.modelcontrollers.AsyncVastLoader;
import com.openx.apollo.parser.AdResponseParserBase;
import com.openx.apollo.parser.AdResponseParserVast;
import com.openx.apollo.utils.logger.OXLog;

import static com.openx.apollo.errors.AdException.INTERNAL_ERROR;
import static com.openx.apollo.video.vast.VASTErrorCodes.VAST_SCHEMA_ERROR;
import static com.openx.apollo.video.vast.VASTErrorCodes.WRAPPER_LIMIT_REACH_ERROR;

public class VastParserExtractor {
    private static final String TAG = VastParserExtractor.class.getSimpleName();

    public static final int WRAPPER_NESTING_LIMIT = 5;

    private final AsyncVastLoader mAsyncVastLoader = new AsyncVastLoader();
    @NonNull
    private final VastParserExtractor.Listener mListener;

    private AdResponseParserVast mRootVastParser;
    private AdResponseParserVast mLatestVastWrapperParser;

    private int mVastWrapperCount;

    private final ResponseHandler mResponseHandler = new ResponseHandler() {
        @Override
        public void onResponse(BaseNetworkTask.GetUrlResult response) {
            performVastUnwrap(response.responseString);
        }

        @Override
        public void onError(String msg, long responseTime) {
            failedToLoadAd(msg);
        }

        @Override
        public void onErrorWithException(Exception e, long responseTime) {
            failedToLoadAd(e.getMessage());
        }
    };

    public VastParserExtractor(
        @NonNull
            Listener listener) {
        mListener = listener;
    }

    public void cancel() {
        if (mAsyncVastLoader != null) {
            mAsyncVastLoader.cancelTask();
        }
    }

    public void extract(String vast) {
        performVastUnwrap(vast);
    }

    private void performVastUnwrap(String vast) {
        if (!vast.contains("VAST version")) {
            final AdException adException = new AdException(INTERNAL_ERROR, VAST_SCHEMA_ERROR.toString());
            mListener.onResult(createExtractorFailureResult(adException));
            return;
        }

        mVastWrapperCount++;

        // A new response has come back, either from the initial VAST request or a wrapper request.
        // Parse the response.
        AdResponseParserVast adResponseParserVast;
        try {
            adResponseParserVast = new AdResponseParserVast(vast);
        }
        catch (VastParseError e) {
            OXLog.error(TAG, "AdResponseParserVast creation failed: " + Log.getStackTraceString(e));

            final AdException adException = new AdException(INTERNAL_ERROR, e.getMessage());
            mListener.onResult(createExtractorFailureResult(adException));
            return;
        }

        // Check if this is the response from the initial request or from unwrapping a wrapper
        if (mRootVastParser == null) {
            // If mRootVastParser doesn't exist then it is the initial VAST request
            OXLog.debug(TAG, "Initial VAST Request");
            mRootVastParser = adResponseParserVast;
        }
        else {
            // Otherwise, this is the result of unwrapping a Wrapper.
            OXLog.debug(TAG, "Unwrapping VAST Wrapper");
            mLatestVastWrapperParser.setWrapper(adResponseParserVast);
        }

        mLatestVastWrapperParser = adResponseParserVast;

        // Check if this response is a wrapper
        String vastUrl = mLatestVastWrapperParser.getVastUrl();
        if (!TextUtils.isEmpty(vastUrl)) {
            if (mVastWrapperCount >= WRAPPER_NESTING_LIMIT) {
                final AdException adException = new AdException(INTERNAL_ERROR, WRAPPER_LIMIT_REACH_ERROR.toString());
                final VastExtractorResult extractorFailureResult = createExtractorFailureResult(adException);
                mListener.onResult(extractorFailureResult);
                mVastWrapperCount = 0;
                return;
            }

            mAsyncVastLoader.loadVast(vastUrl, mResponseHandler);
        }
        else {
            final AdResponseParserBase[] parserArray = {
                mRootVastParser,
                mLatestVastWrapperParser};
            mListener.onResult(new VastExtractorResult(parserArray));
        }
    }

    private void failedToLoadAd(String msg) {
        OXLog.error(TAG, "Invalid ad response: " + msg);

        final AdException adException = new AdException(INTERNAL_ERROR, "Invalid ad response: " + msg);
        mListener.onResult(createExtractorFailureResult(adException));
    }

    @VisibleForTesting
    VastExtractorResult createExtractorFailureResult(AdException adException) {
        return new VastExtractorResult(adException);
    }

    public interface Listener {
        void onResult(VastExtractorResult result);
    }
}
