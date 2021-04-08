package org.prebid.mobile.rendering.loading;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.errors.VastParseError;
import org.prebid.mobile.rendering.models.internal.VastExtractorResult;
import org.prebid.mobile.rendering.networking.BaseNetworkTask;
import org.prebid.mobile.rendering.networking.ResponseHandler;
import org.prebid.mobile.rendering.networking.modelcontrollers.AsyncVastLoader;
import org.prebid.mobile.rendering.parser.AdResponseParserBase;
import org.prebid.mobile.rendering.parser.AdResponseParserVast;
import org.prebid.mobile.rendering.utils.logger.OXLog;
import org.prebid.mobile.rendering.video.vast.VASTErrorCodes;

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
            final AdException adException = new AdException(AdException.INTERNAL_ERROR, VASTErrorCodes.VAST_SCHEMA_ERROR.toString());
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

            final AdException adException = new AdException(AdException.INTERNAL_ERROR, e.getMessage());
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
                final AdException adException = new AdException(AdException.INTERNAL_ERROR, VASTErrorCodes.WRAPPER_LIMIT_REACH_ERROR.toString());
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

        final AdException adException = new AdException(AdException.INTERNAL_ERROR, "Invalid ad response: " + msg);
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
