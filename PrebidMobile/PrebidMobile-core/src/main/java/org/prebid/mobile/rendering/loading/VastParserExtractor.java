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

import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.rendering.errors.VastParseError;
import org.prebid.mobile.rendering.models.internal.VastExtractorResult;
import org.prebid.mobile.rendering.networking.BaseNetworkTask;
import org.prebid.mobile.rendering.networking.ResponseHandler;
import org.prebid.mobile.rendering.networking.modelcontrollers.AsyncVastLoader;
import org.prebid.mobile.rendering.parser.AdResponseParserBase;
import org.prebid.mobile.rendering.parser.AdResponseParserVast;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.prebid.mobile.rendering.video.vast.VASTErrorCodes;

public class VastParserExtractor {

    private static final String TAG = VastParserExtractor.class.getSimpleName();

    public static final int WRAPPER_NESTING_LIMIT = 5;

    private final AsyncVastLoader asyncVastLoader = new AsyncVastLoader();
    @NonNull private final VastParserExtractor.Listener listener;

    private AdResponseParserVast rootVastParser;
    private AdResponseParserVast latestVastWrapperParser;

    private int vastWrapperCount;

    private final ResponseHandler responseHandler = new ResponseHandler() {
        @Override
        public void onResponse(BaseNetworkTask.GetUrlResult response) {
            performVastUnwrap(response.responseString);
        }

        @Override
        public void onError(
                String msg,
                long responseTime
        ) {
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
        this.listener = listener;
    }

    public void cancel() {
        if (asyncVastLoader != null) {
            asyncVastLoader.cancelTask();
        }
    }

    public void extract(String vast) {
        performVastUnwrap(vast);
    }

    private void performVastUnwrap(String vast) {
        if (!Utils.isVast(vast)) {
            final AdException adException = new AdException(AdException.INTERNAL_ERROR, VASTErrorCodes.VAST_SCHEMA_ERROR.toString());
            listener.onResult(createExtractorFailureResult(adException));
            return;
        }

        vastWrapperCount++;

        // A new response has come back, either from the initial VAST request or a wrapper request.
        // Parse the response.
        AdResponseParserVast adResponseParserVast;
        try {
            adResponseParserVast = new AdResponseParserVast(vast);
        } catch (VastParseError e) {
            LogUtil.error(TAG, "AdResponseParserVast creation failed: " + Log.getStackTraceString(e));

            final AdException adException = new AdException(AdException.INTERNAL_ERROR, e.getMessage());
            listener.onResult(createExtractorFailureResult(adException));
            return;
        }

        // Check if this is the response from the initial request or from unwrapping a wrapper
        if (rootVastParser == null) {
            // If rootVastParser doesn't exist then it is the initial VAST request
            LogUtil.debug(TAG, "Initial VAST Request");
            rootVastParser = adResponseParserVast;
        } else {
            // Otherwise, this is the result of unwrapping a Wrapper.
            LogUtil.debug(TAG, "Unwrapping VAST Wrapper");
            latestVastWrapperParser.setWrapper(adResponseParserVast);
        }

        latestVastWrapperParser = adResponseParserVast;

        // Check if this response is a wrapper
        String vastUrl = latestVastWrapperParser.getVastUrl();
        if (!TextUtils.isEmpty(vastUrl)) {
            if (vastWrapperCount >= WRAPPER_NESTING_LIMIT) {
                final AdException adException = new AdException(
                        AdException.INTERNAL_ERROR,
                        VASTErrorCodes.WRAPPER_LIMIT_REACH_ERROR.toString()
                );
                final VastExtractorResult extractorFailureResult = createExtractorFailureResult(adException);
                listener.onResult(extractorFailureResult);
                vastWrapperCount = 0;
                return;
            }

            asyncVastLoader.loadVast(vastUrl, responseHandler);
        }
        else {
            final AdResponseParserBase[] parserArray = {rootVastParser, latestVastWrapperParser};
            listener.onResult(new VastExtractorResult(parserArray));
        }
    }

    private void failedToLoadAd(String msg) {
        LogUtil.error(TAG, "Invalid ad response: " + msg);

        final AdException adException = new AdException(AdException.INTERNAL_ERROR, "Invalid ad response: " + msg);
        listener.onResult(createExtractorFailureResult(adException));
    }

    @VisibleForTesting
    VastExtractorResult createExtractorFailureResult(AdException adException) {
        return new VastExtractorResult(adException);
    }

    public interface Listener {
        void onResult(VastExtractorResult result);
    }
}
