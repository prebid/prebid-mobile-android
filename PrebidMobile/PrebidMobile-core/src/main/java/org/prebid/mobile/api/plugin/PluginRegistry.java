/*
 *    Copyright 2018-2026 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile.api.plugin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import org.prebid.mobile.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe list of the plugins registered for one {@link PluginCapability}.
 * <p>
 * A plugin is identified by the {@link PrebidPluginInfo} that {@code identify} returns, and a plugin
 * whose info equals a registered one is ignored. A plugin becomes active once its {@code onRegister}
 * returns; {@link #getActive()} lists the active plugins in registration order. If {@code onRegister}
 * throws, the registration is rolled back. Plugin code ({@code identify}, {@code onRegister},
 * {@code onUnregister}) always runs outside the lock, so a plugin may call back into the registry.
 * <p>
 * Plugins are expected to be registered during SDK setup and rarely unregistered. {@link #remove}
 * takes a plugin out and calls its {@code onUnregister} right away, even if its {@code onRegister}
 * is still running. Adding and removing the same plugin concurrently, or from inside its own
 * callbacks, is not supported.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public final class PluginRegistry<P> {

    /**
     * Capacity of a registry that accepts any number of plugins.
     */
    public static final int UNLIMITED = Integer.MAX_VALUE;

    /**
     * Returns the identity of a plugin.
     */
    public interface Identify<P> {

        @NonNull
        PrebidPluginInfo identify(@NonNull P plugin);
    }

    /**
     * A plugin's {@code onRegister} or {@code onUnregister} callback.
     */
    public interface Lifecycle<P> {

        void call(@NonNull P plugin);
    }

    /**
     * A registered plugin.
     */
    public static final class Entry<P> {

        private final long sequence;
        @NonNull
        private final PrebidPluginInfo info;
        @NonNull
        private final P plugin;

        Entry(long sequence, @NonNull PrebidPluginInfo info, @NonNull P plugin) {
            this.sequence = sequence;
            this.info = info;
            this.plugin = plugin;
        }

        /**
         * Position of the registration in a process-wide counter shared by all capabilities, so
         * plugins registered for different capabilities can be ordered against each other.
         */
        public long getSequence() {
            return sequence;
        }

        @NonNull
        public PrebidPluginInfo getInfo() {
            return info;
        }

        @NonNull
        public P getPlugin() {
            return plugin;
        }
    }

    private static final class Registration<P> {

        @NonNull
        final Entry<P> entry;
        /**
         * Set once {@code onRegister} returns. Guarded by the registry's lock.
         */
        boolean active;

        Registration(@NonNull Entry<P> entry) {
            this.entry = entry;
        }
    }

    private static final String TAG = "PluginRegistry";

    /**
     * Process-wide registration counter shared by all capabilities.
     */
    private static final AtomicLong SEQUENCE = new AtomicLong();

    @NonNull
    private final String name;
    @NonNull
    private final Identify<P> identify;
    private final int capacity;
    @Nullable
    private final Lifecycle<P> onRegister;
    @Nullable
    private final Lifecycle<P> onUnregister;

    private final Object lock = new Object();
    private final ArrayList<Registration<P>> registrations = new ArrayList<>();

    /**
     * @param name     the capability name, used in logs.
     * @param capacity the maximum number of registered plugins, or {@link #UNLIMITED}.
     */
    PluginRegistry(
            @NonNull String name,
            @NonNull Identify<P> identify,
            int capacity,
            @Nullable Lifecycle<P> onRegister,
            @Nullable Lifecycle<P> onUnregister
    ) {
        this.name = name;
        this.identify = identify;
        this.capacity = capacity;
        this.onRegister = onRegister;
        this.onUnregister = onUnregister;
    }

    /**
     * The capability name, used in logs.
     */
    @NonNull
    public String getName() {
        return name;
    }

    /**
     * The active plugins in registration order (a snapshot).
     */
    @NonNull
    public List<P> getActive() {
        synchronized (lock) {
            List<P> result = new ArrayList<>(registrations.size());
            for (Registration<P> registration : registrations) {
                if (registration.active) {
                    result.add(registration.entry.plugin);
                }
            }
            return result;
        }
    }

    /**
     * The entries of the active plugins in registration order (a snapshot).
     */
    @NonNull
    public List<Entry<P>> getActiveEntries() {
        synchronized (lock) {
            List<Entry<P>> result = new ArrayList<>(registrations.size());
            for (Registration<P> registration : registrations) {
                if (registration.active) {
                    result.add(registration.entry);
                }
            }
            return result;
        }
    }

    /**
     * Registers the plugin and calls its {@code onRegister}. It's ignored if a plugin with equal info
     * is registered, or if the registry is full. If {@code onRegister} throws an exception, checked
     * or not, the plugin is not registered and the exception is logged. If it throws an
     * {@link Error}, the plugin is not registered either, and the error propagates.
     */
    public void add(@NonNull P plugin) {
        PrebidPluginInfo info = identify.identify(plugin);

        Registration<P> registration = null;
        boolean isFull = false;
        synchronized (lock) {
            if (indexOf(info) < 0) {
                if (registrations.size() >= capacity) {
                    isFull = true;
                } else {
                    registration = new Registration<>(new Entry<>(SEQUENCE.incrementAndGet(), info, plugin));
                    registrations.add(registration);
                }
            }
        }

        if (registration == null) {
            String reason = isFull
                    ? "the capability accepts at most " + capacity
                    : "a plugin with the same identity is already registered";
            LogUtil.warning(TAG, "[" + name + "] Plugin " + info + " is not registered: " + reason + ".");
            return;
        }

        boolean registered = false;
        Exception failure = null;
        try {
            if (onRegister != null) {
                onRegister.call(plugin);
            }
            registered = true;
        } catch (Exception exception) {
            // Also catches checked exceptions, which Kotlin code can throw without declaring them.
            failure = exception;
        } finally {
            // Runs for an Error too, so the identity is free again before the Error propagates.
            if (!registered) {
                synchronized (lock) {
                    registrations.remove(registration);
                }
            }
        }

        if (!registered) {
            LogUtil.error(TAG, "[" + name + "] Plugin " + info + " is not registered: its onRegister threw " + failure);
            return;
        }

        synchronized (lock) {
            registration.active = true;
        }

        LogUtil.debug(TAG, "[" + name + "] Plugin " + info + " registered.");
    }

    /**
     * Unregisters the plugin whose info equals this plugin's, and calls that plugin's
     * {@code onUnregister}. No-op if no such plugin is registered. An exception that
     * {@code onUnregister} throws, checked or not, is logged and not propagated.
     */
    public void remove(@NonNull P plugin) {
        PrebidPluginInfo info = identify.identify(plugin);

        Registration<P> removed;
        synchronized (lock) {
            int index = indexOf(info);
            if (index < 0) {
                return;
            }
            removed = registrations.remove(index);
        }

        callOnUnregister(removed.entry);
        LogUtil.debug(TAG, "[" + name + "] Plugin " + info + " unregistered.");
    }

    /**
     * Unregisters every plugin, in registration order, and calls their {@code onUnregister}.
     * An exception from one {@code onUnregister} is logged and doesn't stop the others.
     */
    public void removeAll() {
        List<Registration<P>> removed;
        synchronized (lock) {
            removed = new ArrayList<>(registrations);
            registrations.clear();
        }

        for (Registration<P> registration : removed) {
            callOnUnregister(registration.entry);
        }
    }

    /**
     * Returns whether an active plugin's info equals this plugin's.
     */
    public boolean contains(@NonNull P plugin) {
        PrebidPluginInfo info = identify.identify(plugin);

        synchronized (lock) {
            int index = indexOf(info);
            return index >= 0 && registrations.get(index).active;
        }
    }

    private int indexOf(@NonNull PrebidPluginInfo info) {
        for (int i = 0; i < registrations.size(); i++) {
            if (registrations.get(i).entry.info.equals(info)) {
                return i;
            }
        }
        return -1;
    }

    private void callOnUnregister(@NonNull Entry<P> entry) {
        if (onUnregister == null) {
            return;
        }

        try {
            onUnregister.call(entry.plugin);
        } catch (Exception exception) {
            // Also catches checked exceptions, which Kotlin code can throw without declaring them.
            LogUtil.error(TAG, "[" + name + "] Plugin " + entry.info + " is unregistered, but its onUnregister threw " + exception);
        }
    }
}
