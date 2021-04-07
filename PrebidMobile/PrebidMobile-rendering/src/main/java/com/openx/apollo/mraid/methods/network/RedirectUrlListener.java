package com.openx.apollo.mraid.methods.network;

public interface RedirectUrlListener {

    void onSuccess(String url, String contentType);

    void onFailed();
}
