package org.prebid.mobile;

import androidx.annotation.NonNull;

/**
 * Response object for native data asset.
 */
public class NativeData {

    private final int typeNumber;
    private final String value;

    public NativeData(int typeNumber, @NonNull String value) {
        this.typeNumber = typeNumber;
        this.value = value;
    }

    public NativeData(Type type, @NonNull String value) {
        if (type == Type.CUSTOM) {
            throw new IllegalArgumentException("For CUSTOM type use constructor with typeNumber parameter.");
        }
        this.typeNumber = Type.getNumberFromType(type);
        this.value = value;
    }

    public int getTypeNumber() {
        return typeNumber;
    }

    @NonNull
    public String getValue() {
        return value;
    }

    @NonNull
    public Type getType() {
        return Type.getFromTypeNumber(typeNumber);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        NativeData that = (NativeData) object;
        return typeNumber == that.typeNumber && value.equals(that.value);
    }

    /**
     * Enum type of native data.
     */
    public enum Type {
        SPONSORED_BY,
        DESCRIPTION,
        CALL_TO_ACTION,
        RATING,
        CUSTOM;

        public static Type getFromTypeNumber(int typeNumber) {
            switch (typeNumber) {
                case 1:
                    return SPONSORED_BY;
                case 2:
                    return DESCRIPTION;
                case 3:
                    return RATING;
                case 12:
                    return CALL_TO_ACTION;
                default:
                    return CUSTOM;
            }
        }

        public static int getNumberFromType(Type type) {
            switch (type) {
                case SPONSORED_BY:
                    return 1;
                case DESCRIPTION:
                    return 2;
                case RATING:
                    return 3;
                case CALL_TO_ACTION:
                    return 12;
                default:
                    return 0;
            }
        }
    }

}
