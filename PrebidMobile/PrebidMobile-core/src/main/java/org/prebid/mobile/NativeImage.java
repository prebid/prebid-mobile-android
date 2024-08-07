package org.prebid.mobile;

import androidx.annotation.NonNull;

/**
 * Response object for native image asset.
 */
public class NativeImage {

    private final int typeNumber;
    private final String url;

    public NativeImage(int typeNumber, @NonNull String url) {
        this.typeNumber = typeNumber;
        this.url = url;
    }

    public NativeImage(@NonNull Type type, @NonNull String url) {
        if (type == Type.CUSTOM) {
            throw new IllegalArgumentException("For CUSTOM type use constructor with typeNumber parameter.");
        }
        this.typeNumber = Type.getNumberFromType(type);
        this.url = url;
    }

    public int getTypeNumber() {
        return typeNumber;
    }

    @NonNull
    public String getUrl() {
        return url;
    }

    @NonNull
    public Type getType() {
        return Type.getTypeFromNumber(typeNumber);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        NativeImage that = (NativeImage) object;
        return typeNumber == that.typeNumber && url.equals(that.url);
    }

    /**
     * Enum type of native image.
     */
    public enum Type {
        ICON,
        MAIN_IMAGE,
        CUSTOM;

        public static Type getTypeFromNumber(int typeNumber) {
            switch (typeNumber) {
                case 1:
                    return Type.ICON;
                case 3:
                    return Type.MAIN_IMAGE;
                default:
                    return Type.CUSTOM;
            }
        }

        public static int getNumberFromType(Type type) {
            switch (type) {
                case ICON:
                    return 1;
                case MAIN_IMAGE:
                    return 3;
                default:
                    return 0;
            }
        }
    }

}
