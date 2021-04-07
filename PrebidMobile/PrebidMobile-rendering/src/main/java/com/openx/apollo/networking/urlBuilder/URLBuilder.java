package com.openx.apollo.networking.urlBuilder;

import com.openx.apollo.networking.parameters.AdRequestInput;
import com.openx.apollo.networking.parameters.ParameterBuilder;

import java.util.ArrayList;

public class URLBuilder {

    private final URLPathBuilder mPathBuilder;
    private final ArrayList<ParameterBuilder> mParamBuilders;
    private final AdRequestInput mAdRequestInput;

    public URLBuilder(URLPathBuilder pathBuilder, ArrayList<ParameterBuilder> parameterBuilders, AdRequestInput adRequestInput) {
        mPathBuilder = pathBuilder;
        mParamBuilders = parameterBuilders;
        mAdRequestInput = adRequestInput;
    }

    public BidUrlComponents buildUrl() {
        AdRequestInput adRequestInput = buildParameters(mParamBuilders, mAdRequestInput);
        String initialPath = mPathBuilder.buildURLPath("");
        return new BidUrlComponents(initialPath, adRequestInput);
    }

    static AdRequestInput buildParameters(ArrayList<ParameterBuilder> paramBuilders, AdRequestInput adRequestInput) {
        if (adRequestInput == null) {
            return new AdRequestInput();
        }

        AdRequestInput newAdRequestInput = adRequestInput.getDeepCopy();

        for (ParameterBuilder builder : paramBuilders) {
            builder.appendBuilderParameters(newAdRequestInput);
        }

        return newAdRequestInput;
    }
}
