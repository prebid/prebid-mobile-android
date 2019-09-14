package org.prebid.mobile.drprebid.util;

import android.content.Context;

import org.prebid.mobile.drprebid.R;
import org.prebid.mobile.drprebid.model.HelpScreen;

public class HelpScreenUtil {

    public static HelpScreen getAbout(Context context) {
        return new HelpScreen(context.getString(R.string.about), context.getString(R.string.about_file));
    }

    public static HelpScreen getGeneralInfo(Context context) {
        return new HelpScreen(context.getString(R.string.general_info), context.getString(R.string.general_info_file));
    }

    public static HelpScreen getAdServerInfo(Context context) {
        return new HelpScreen(context.getString(R.string.ad_server_info), context.getString(R.string.ad_server_info_file));
    }

    public static HelpScreen getPrebidServerInfo(Context context) {
        return new HelpScreen(context.getString(R.string.prebid_server_info), context.getString(R.string.prebid_server_info_file));
    }

    public static HelpScreen getAdServerTestInfo(Context context) {
        return new HelpScreen(context.getString(R.string.ad_server_test_info), context.getString(R.string.ad_server_test_info_file));
    }

    public static HelpScreen getRealTimeDemandTestInfo(Context context) {
        return new HelpScreen(context.getString(R.string.real_time_demand_test_info), context.getString(R.string.real_time_demand_test_info_file));
    }

    public static HelpScreen getSdkTestInfo(Context context) {
        return new HelpScreen(context.getString(R.string.sdk_test_info), context.getString(R.string.sdk_test_info_file));
    }
}
