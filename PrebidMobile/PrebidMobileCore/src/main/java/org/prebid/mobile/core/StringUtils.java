package org.prebid.mobile.core;

import java.util.Random;

/**
 * Utility class to host small essentials string routines.
 */
final class StringUtils {

    private static final Random RANDOM = new Random();

    /**
     * Creates a random lowercase string whose length is the number
     * of characters specified.
     * <p>
     * Characters will be chosen from the set of Latin alphabetic
     * characters (a-z).
     *
     * @param count the length of random string to create
     * @return the random string
     */
    public static String randomLowercaseAlphabetic(int count) {
        return randomLowercaseAlphabetic(count, RANDOM);
    }

    static String randomLowercaseAlphabetic(int count, Random random) {
        if (count == 0) {
            return "";
        } else if (count < 0) {
            throw new IllegalArgumentException("Invalid count value: " + count + " is less than 0.");
        }

        int start = 'a';
        int end = 'z' + 1;

        StringBuilder builder = new StringBuilder(count);
        int gap = end - start;

        while (count-- != 0) {
            int codePoint = random.nextInt(gap) + start;
            builder.appendCodePoint(codePoint);
        }
        return builder.toString();
    }
}
