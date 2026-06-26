package org.prebid.mobile;

/**
 * Controls where Extended Identifiers (EIDs) are placed in the bid request.
 * <p>
 * Allows publishers to choose where EIDs are placed during migration from
 * OpenRTB 2.5 ({@code user.ext.eids}) to 2.6 ({@code user.eids}).
 */
public enum EidsPlacement {

    /**
     * EIDs placed only in {@code user.eids} (OpenRTB 2.6).
     */
    OPEN_RTB_2_6,

    /**
     * EIDs placed only in {@code user.ext.eids} (OpenRTB 2.5).
     */
    OPEN_RTB_2_5,

    /**
     * EIDs placed in both {@code user.eids} and {@code user.ext.eids}.
     * Default value for backwards compatibility during migration.
     */
    COMPATIBLE;

    public boolean inUser() {
        return this == OPEN_RTB_2_6 || this == COMPATIBLE;
    }

    public boolean inUserExt() {
        return this == OPEN_RTB_2_5 || this == COMPATIBLE;
    }
}
