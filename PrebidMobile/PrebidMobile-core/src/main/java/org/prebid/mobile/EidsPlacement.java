package org.prebid.mobile;

/**
 * Where the SDK places Extended Identifiers (EIDs) in the bid request.
 * <p>
 * OpenRTB 2.6 moved EIDs from {@code user.ext.eids} to {@code user.eids}. Prebid Server reads
 * {@code user.eids} and ignores {@code user.ext.eids} whenever {@code user.eids} is present.
 */
public enum EidsPlacement {

    /**
     * EIDs are sent only in {@code user.eids} (OpenRTB 2.6).
     */
    OPEN_RTB_2_6,

    /**
     * EIDs are sent only in {@code user.ext.eids} (OpenRTB 2.5).
     */
    OPEN_RTB_2_5,

    /**
     * The same EIDs are sent in both {@code user.eids} and {@code user.ext.eids}. Default.
     */
    COMPATIBLE
}
