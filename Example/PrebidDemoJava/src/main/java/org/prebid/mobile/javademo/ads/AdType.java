package org.prebid.mobile.javademo.ads;

import android.app.Activity;
import android.view.ViewGroup;

public class AdType {

    private final String name;
    private final OnAdTypeCreate onCreate;
    private final OnAdTypeDestroy onDestroy;

    public AdType(
        String name,
        OnAdTypeCreate onCreate,
        OnAdTypeDestroy onDestroy
    ) {
        this.name = name;
        this.onCreate = onCreate;
        this.onDestroy = onDestroy;
    }


    public String getName() {
        return name;
    }

    public OnAdTypeCreate getOnCreate() {
        return onCreate;
    }

    public OnAdTypeDestroy getOnDestroy() {
        return onDestroy;
    }


    public interface OnAdTypeCreate {

        void run(
            Activity activity,
            ViewGroup wrapper,
            int autoRefreshTime
        );

    }

    public interface OnAdTypeDestroy {

        void run();

    }

}
