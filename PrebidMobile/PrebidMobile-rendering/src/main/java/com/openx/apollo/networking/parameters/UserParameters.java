package com.openx.apollo.networking.parameters;

/**
 * Container class for advertisement call parameters.
 */
public class UserParameters {
    public static final String TAG = UserParameters.class.getSimpleName();

    public static final String GENDER_MALE = "M";
    public static final String GENDER_FEMALE = "F";
    public static final String GENDER_OTHER = "O";

    private UserParameters() {

    }

    /**
     * User gender.
     */
    public enum OXMGender {
        /**
         * User is male.
         */
        MALE,

        /**
         * User is female.
         */
        FEMALE,

        /**
         * Other.
         */
        OTHER
    }

    public static String getGenderDescription(OXMGender gender) {
        String desc = null;
        switch (gender) {
            case MALE:
                desc = GENDER_MALE;
                break;
            case FEMALE:
                desc = GENDER_FEMALE;
                break;
            case OTHER:
                desc = GENDER_OTHER;
                break;
        }
        return desc;
    }

    /**
     * Device connection type.
     */
    public enum OXMConnectionType {
        /**
         * Device is off-line.
         */
        OFFLINE,

        /**
         * Device connected via WiFi.
         */
        WIFI,

        /**
         * Device connected via mobile technology, such as 3G, GPRS, CDMA etc.
         */
        CELL
    }
}