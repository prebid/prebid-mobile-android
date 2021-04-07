package com.openx.apollo.networking.urlBuilder;

public class PathBuilderBase extends URLPathBuilder {

    private static final String API_VERSION = "1.0";
    private static final String PROTOCOL = "https";

    protected String mRoute = "ma";

    @Override
    public String buildURLPath(String domain) {
        return PROTOCOL + "://" + domain + "/" + mRoute + "/" + API_VERSION + "/";
    }

    public PathBuilderBase() {

    }
}
