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

package org.prebid.mobile.rendering.session.manager;

import com.iab.omid.library.prebidorg.adsession.FriendlyObstructionPurpose;
import com.iab.omid.library.prebidorg.adsession.media.PlayerState;
import org.prebid.mobile.rendering.models.internal.InternalFriendlyObstruction;
import org.prebid.mobile.rendering.models.internal.InternalPlayerState;

/**
 * Helper class to map models from internal SDK to OM SDK.
 */
class OmModelMapper {
    private OmModelMapper() {

    }

    static PlayerState mapToPlayerState(InternalPlayerState videoPlayerState) {
        switch (videoPlayerState) {
            case NORMAL:
                return PlayerState.NORMAL;
            case EXPANDED:
                return PlayerState.EXPANDED;
            case FULLSCREEN:
                return PlayerState.FULLSCREEN;
            default:
                throw new IllegalArgumentException("Case is not defined!");
        }
    }

    static FriendlyObstructionPurpose mapToFriendlyObstructionPurpose(InternalFriendlyObstruction.Purpose purpose) {
        switch (purpose) {
            case CLOSE_AD:
                return FriendlyObstructionPurpose.CLOSE_AD;
            case VIDEO_CONTROLS:
                return FriendlyObstructionPurpose.VIDEO_CONTROLS;
            case OTHER:
                return FriendlyObstructionPurpose.OTHER;
            default:
                throw new IllegalArgumentException("Case is not defined!");
        }
    }
}
