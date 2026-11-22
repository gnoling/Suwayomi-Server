package xyz.nulldev.androidcompat.res;

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import android.content.res.Resources;
import android.util.DisplayMetrics;

/**
 * The headless "screen" WebViews render to, and the {@link Resources} describing it.
 * <p>
 * The offscreen viewport, the metrics extensions read from {@code WebView.getResources()} and
 * the touch coordinates they send back all have to agree, otherwise taps land in the wrong place.
 */
public final class CompatResources {
    /*
     * A typical phone (1080x2400 @ 420dpi). Extensions' page scripts are written for phone-sized
     * viewports, e.g. Comix only recognizes its reader once the page scrolls, which a single-page
     * chapter never does in a huge desktop-sized viewport.
     */
    public static final int WIDTH_PIXELS = 1080;
    public static final int HEIGHT_PIXELS = 2400;
    public static final float DENSITY = 2.625f;
    public static final int DENSITY_DPI = 420;

    /** Size of the offscreen viewport WebViews render into, in CSS pixels (DIP): 411x914. */
    public static final int VIEW_WIDTH = Math.round(WIDTH_PIXELS / DENSITY);
    public static final int VIEW_HEIGHT = Math.round(HEIGHT_PIXELS / DENSITY);

    public static final DisplayMetrics METRICS;
    public static final Resources INSTANCE;

    static {
        METRICS = new DisplayMetrics();
        METRICS.widthPixels = WIDTH_PIXELS;
        METRICS.heightPixels = HEIGHT_PIXELS;
        METRICS.density = DENSITY;
        METRICS.densityDpi = DENSITY_DPI;
        METRICS.scaledDensity = DENSITY;
        METRICS.xdpi = DENSITY_DPI;
        METRICS.ydpi = DENSITY_DPI;
        INSTANCE = new Resources(null, METRICS, Resources.getSystem().getConfiguration());
    }

    private CompatResources() {
    }
}
