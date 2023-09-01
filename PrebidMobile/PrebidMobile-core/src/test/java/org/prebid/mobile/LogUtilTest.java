package org.prebid.mobile;

import static org.prebid.mobile.LogUtil.DEBUG;
import static org.prebid.mobile.LogUtil.ERROR;
import static org.prebid.mobile.LogUtil.INFO;
import static org.prebid.mobile.LogUtil.VERBOSE;

import org.junit.Before;
import org.junit.Test;
import org.prebid.mobile.testutils.TestLogger;

public class LogUtilTest {

    private final String DEFAULT_TAG = "PrebidMobile";
    private final TestLogger testLogger = new TestLogger();

    @Before
    public void setup() {
        LogUtil.setLogLevel(VERBOSE);
        LogUtil.setLogger(testLogger);
    }

    @Test
    public void testErrorLogWithMessage() {
        String testMessage = "Test error message";

        LogUtil.error(testMessage);

        testLogger.assertLogPrinted(ERROR, DEFAULT_TAG, testMessage);
    }

    @Test
    public void testErrorLogWithMessageAndTag() {
        String testTag = "Test tag";
        String testMessage = "Test error message";

        LogUtil.error(testTag, testMessage);

        testLogger.assertLogPrinted(ERROR, testTag, testMessage);
    }

    @Test
    public void testErrorLogWithMessageTagAndThrowable() {
        String testTag = "Test tag";
        String testMessage = "Test error message";
        Throwable testThrowable = new Throwable("Test throwable");

        LogUtil.error(testTag, testMessage, testThrowable);

        testLogger.assertErrorLogged(testTag, testMessage, testThrowable);
    }

    @Test
    public void testVerboseLogWithMessage() {
        String testMessage = "Test error message";

        LogUtil.verbose(testMessage);

        testLogger.assertLogPrinted(VERBOSE, DEFAULT_TAG, testMessage);
    }

    @Test
    public void testVerboseLogWithMessageAndTag() {
        String testTag = "Test tag";
        String testMessage = "Test error message";

        LogUtil.verbose(testTag, testMessage);

        testLogger.assertLogPrinted(VERBOSE, testTag, testMessage);
    }

    @Test
    public void testInfoLogWithMessage() {
        String testMessage = "Test error message";

        LogUtil.info(testMessage);

        testLogger.assertLogPrinted(INFO, DEFAULT_TAG, testMessage);
    }

    @Test
    public void testInfoLogWithMessageAndTag() {
        String testTag = "Test tag";
        String testMessage = "Test error message";

        LogUtil.info(testTag, testMessage);

        testLogger.assertLogPrinted(INFO, testTag, testMessage);
    }

    @Test
    public void testWarningLogWithMessage() {
        String testMessage = "Test error message";

        LogUtil.warning(testMessage);

        testLogger.assertLogPrinted(INFO, DEFAULT_TAG, testMessage);
    }

    @Test
    public void testWarningLogWithMessageAndTag() {
        String testTag = "Test tag";
        String testMessage = "Test error message";

        LogUtil.warning(testTag, testMessage);

        testLogger.assertLogPrinted(INFO, testTag, testMessage);
    }

    @Test
    public void testDebugLogWithMessage() {
        String testMessage = "Test error message";

        LogUtil.warning(testMessage);

        testLogger.assertLogPrinted(DEBUG, DEFAULT_TAG, testMessage);
    }

    @Test
    public void testDebugLogWithMessageAndTag() {
        String testTag = "Test tag";
        String testMessage = "Test error message";

        LogUtil.warning(testTag, testMessage);

        testLogger.assertLogPrinted(DEBUG, testTag, testMessage);
    }
}

