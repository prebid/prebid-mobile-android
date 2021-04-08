package org.prebid.mobile.rendering.listeners;

import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.models.AbstractCreative;

public interface CreativeResolutionListener {
    void creativeReady(AbstractCreative creative);
    void creativeFailed(AdException error);
}
