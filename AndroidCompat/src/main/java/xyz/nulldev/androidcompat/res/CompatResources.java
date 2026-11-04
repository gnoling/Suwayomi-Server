package xyz.nulldev.androidcompat.res;

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.lang.reflect.Field;

/**
 * Minimal {@link Resources} describing the headless "screen" WebViews render to.
 * <p>
 * Only {@link #getDisplayMetrics()} is implemented. Every constructor in the android.jar
 * stubs throws, so both this class and its {@link DisplayMetrics} are allocated without
 * running one.
 */
public final class CompatResources extends Resources {
    /** Size of the offscreen viewport WebViews render into, in pixels. */
    public static final int WIDTH_PIXELS = 1280;
    public static final int HEIGHT_PIXELS = 2856;

    /** 1 CSS px == 1 device px, so coordinates from page JS can be used as-is. */
    public static final float DENSITY = 1f;
    public static final int DENSITY_DPI = 160;

    private static final DisplayMetrics METRICS;
    public static final CompatResources INSTANCE;

    static {
        METRICS = allocate(DisplayMetrics.class);
        METRICS.widthPixels = WIDTH_PIXELS;
        METRICS.heightPixels = HEIGHT_PIXELS;
        METRICS.density = DENSITY;
        METRICS.densityDpi = DENSITY_DPI;
        METRICS.scaledDensity = DENSITY;
        METRICS.xdpi = DENSITY_DPI;
        METRICS.ydpi = DENSITY_DPI;
        INSTANCE = allocate(CompatResources.class);
    }

    @SuppressWarnings("deprecation")
    private CompatResources() {
        // Never invoked, see allocate()
        super(null, null, null);
    }

    @Override
    public DisplayMetrics getDisplayMetrics() {
        return METRICS;
    }

    @SuppressWarnings("removal")
    private static <T> T allocate(Class<T> clazz) {
        try {
            Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            sun.misc.Unsafe unsafe = (sun.misc.Unsafe) f.get(null);
            return clazz.cast(unsafe.allocateInstance(clazz));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to allocate " + clazz.getName(), e);
        }
    }
}
