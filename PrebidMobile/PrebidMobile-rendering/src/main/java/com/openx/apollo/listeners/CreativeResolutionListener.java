package com.openx.apollo.listeners;

import com.openx.apollo.errors.AdException;
import com.openx.apollo.models.AbstractCreative;

public interface CreativeResolutionListener {
    void creativeReady(AbstractCreative creative);
    void creativeFailed(AdException error);
}
