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

package org.prebid.mobile.rendering.utils.url.action;

import android.content.Context;
import android.net.Uri;
import org.prebid.mobile.rendering.utils.url.ActionNotResolvedException;
import org.prebid.mobile.rendering.utils.url.UrlHandler;

public interface UrlAction {

    /**
     * Determines if the uri can be handled by specific action.
     * NOTE: In order to make all actions work properly - each action should handle unique URIs.
     *
     * @param uri which should be handled.
     * @return true if given action can process such URI. False otherwise.
     */
    boolean shouldOverrideUrlLoading(Uri uri);

    /**
     * Executes specific action on the given uri.
     *
     * @param context    activity context.
     * @param urlHandler holder of action.
     * @param uri        which should be handled.
     * @throws ActionNotResolvedException when action can be performed (shouldOverrideUrlLoading: true)
     *                                    and for some reason it's not handled
     *                                    NOTE: Pass reason error message into {@link ActionNotResolvedException}
     */
    void performAction(Context context, UrlHandler urlHandler, Uri uri) throws
                                                                        ActionNotResolvedException;

    /**
     * @return true - if this action should be triggered by user in order to proceed.
     * False - if performing this action doesn't require it to be triggered by user interaction.
     */
    boolean shouldBeTriggeredByUserAction();
}
