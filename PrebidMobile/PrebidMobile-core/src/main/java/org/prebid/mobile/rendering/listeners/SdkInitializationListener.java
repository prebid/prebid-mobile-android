package org.prebid.mobile.rendering.listeners;

public interface SdkInitializationListener {

    void onSdkInit();

    void onSdkFailedToInit(InitError error);

    class InitError {

        private String error;

        public InitError(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }

    }

}
