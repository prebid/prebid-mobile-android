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

package org.prebid.mobile.rendering.models.internal;

import android.view.View;

import java.lang.ref.WeakReference;

public class InternalFriendlyObstruction {

    private WeakReference<View> viewWeakReference;
    private InternalFriendlyObstruction.Purpose friendlyObstructionPurpose;
    private String detailedDescription;

    public InternalFriendlyObstruction(
            View view,
            InternalFriendlyObstruction.Purpose friendlyObstructionPurpose,
            String detailedDescription
    ) {
        viewWeakReference = new WeakReference<>(view);
        this.friendlyObstructionPurpose = friendlyObstructionPurpose;
        this.detailedDescription = detailedDescription;
    }

    public View getView() {
        return viewWeakReference.get();
    }

    public InternalFriendlyObstruction.Purpose getPurpose() {
        return friendlyObstructionPurpose;
    }

    public String getDetailedDescription() {
        return detailedDescription;
    }

    public enum Purpose {
        CLOSE_AD,
        OTHER,
        VIDEO_CONTROLS
    }
}
