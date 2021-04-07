package com.openx.apollo.session.manager;

import com.iab.omid.library.openx.adsession.FriendlyObstructionPurpose;
import com.iab.omid.library.openx.adsession.media.PlayerState;
import com.openx.apollo.models.internal.InternalFriendlyObstruction;
import com.openx.apollo.models.internal.InternalPlayerState;

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
