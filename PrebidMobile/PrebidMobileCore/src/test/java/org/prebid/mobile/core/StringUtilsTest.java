package org.prebid.mobile.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Random;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21)
public class StringUtilsTest {

    @Test
    public void testRandomLowercaseAlphabeticFixedLength() {
        String str = StringUtils.randomLowercaseAlphabetic(8, new Random(1000));
        assertEquals("rxqoaxlj", str);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRandomLowercaseAlphabeticInvalidLength() {
        StringUtils.randomLowercaseAlphabetic(-1);
    }

    @Test
    public void testRandomLowercaseAlphabeticLengthAndCase() {
        for (int i = 0; i < 20; i++) {
            String str = StringUtils.randomLowercaseAlphabetic(i);
            assertEquals(i, str.length());
            for (int j = 0; j < i; j++) {
                assertTrue(Character.isLowerCase(str.charAt(j)));
            }
        }
    }

    @Test
    public void testEscapeEcmaScript() {
        // tests copied from apache commons text
        assertNull(StringUtils.escapeEcmaScript(null));

        assertEquals("He didn\\'t say, \\\"stop!\\\"", StringUtils.escapeEcmaScript("He didn't say, \"stop!\""));
        assertEquals("document.getElementById(\\\"test\\\").value = \\'<script>alert(\\'aaa\\');<\\/script>\\';",
                StringUtils.escapeEcmaScript("document.getElementById(\"test\").value = '<script>alert('aaa');</script>';"));

        // test unicode
        assertEquals("\\u03a5\\u03b4\\u03c1\\u03cc\\u03b3\\u03b5\\u03b9\\u03bf\\u03c2 is Globe",
                StringUtils.escapeEcmaScript("Υδρόγειος is Globe"));
    }
}
