package org.prebid.mobile;

/**
 * AdSize class defines the size of the ad slot to be made available for auction.
 */
public class AdSize {
    private int width;
    private int height;

    /**
     * Creates an ad size object with width and height as specified
     *
     * @param width  width of the ad container
     * @param height height of the ad container
     */
    AdSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Returns the width of the ad container
     *
     * @return width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of the ad container
     *
     * @return height
     */
    public int getHeight() {
        return height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AdSize adSize = (AdSize) o;

        if (width != adSize.width) return false;
        return height == adSize.height;
    }

    @Override
    public int hashCode() {
        String size = width + "x" + height;
        return size.hashCode();
    }
}
