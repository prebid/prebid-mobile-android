package com.openx.apollo.models;

import com.openx.apollo.parser.AdResponseParserBase;

import java.util.List;

public abstract class CreativeModelsMaker {

    public abstract void makeModels(AdConfiguration adConfiguration, AdResponseParserBase... parsers);

    public static class Result {
        public String transactionState;
        public List<CreativeModel> creativeModels;
        public String loaderIdentifier;
    }
}
